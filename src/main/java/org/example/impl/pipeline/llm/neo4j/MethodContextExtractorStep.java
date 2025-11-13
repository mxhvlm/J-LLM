package org.example.impl.pipeline.llm.neo4j;

import com.google.gson.Gson;
import org.example.impl.datamodel.impl.neo4j.Neo4jMethodSummaryContextResult;
import org.example.impl.integration.api.neo4j.INeo4jProvider;
import org.example.impl.pipeline.IPipelineStep;
import org.neo4j.driver.Values;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Stream;

public class MethodContextExtractorStep implements IPipelineStep<Integer, Stream<Neo4jMethodSummaryContextResult>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MethodContextExtractorStep.class);
    private static final String CYPHER_QUERY =
            """
                    // Replace $methodName with the method you want to analyze
                    MATCH (m:Method) WHERE NOT (m)-[:HAS_EXPLANATION]->()
                    // 1. Owning type
                    OPTIONAL MATCH (ownerType:Type)-[:HAS_METHOD]->(m)
                    // 2. Module → package → type hierarchy
                    OPTIONAL MATCH (ownerModule:Module)-[:CONTAINS_PACKAGE]->(pkg:Package)-[:CONTAINS_TYPE]->(ownerType)
                    // 3. Superclass
                    OPTIONAL MATCH (ownerType)-[:EXTENDS]->(superType:Type)
                    // 4a. All implemented interfaces
                    OPTIONAL MATCH (ownerType)-[:IMPLEMENTS]->(interface:Type)
                    WITH m, ownerType, ownerModule, pkg, superType, interface,
                         COLLECT(DISTINCT interface.name) AS implementedInterfaces
                    // 4b. Interfaces that define a method with the same name
                    OPTIONAL MATCH (interface)-[:HAS_METHOD]->(im:Method)
                      WHERE im.name = m.name
                    WITH m, ownerType, ownerModule, pkg, superType, implementedInterfaces,
                         COLLECT(DISTINCT interface.name) AS definedInInterface
                    // 5. Called methods via CALLS_STATIC
                    OPTIONAL MATCH (m)-[:CALLS_STATIC]->(called:Method)
                    WITH m, ownerType, ownerModule, pkg, superType, implementedInterfaces, definedInInterface,
                         COLLECT(DISTINCT called.signature) AS calledMethods
                    // 6. Other methods in the same class, not called
                    OPTIONAL MATCH (ownerType)-[:HAS_METHOD]->(other:Method)
                    WHERE other <> m AND NOT (m)-[:CALLS_STATIC]->(other)
                    WITH m, ownerType, ownerModule, pkg, superType, implementedInterfaces, definedInInterface, calledMethods,
                         COLLECT(DISTINCT other.name) AS otherMethods
                    // 7. Overrides (methods this one overrides)
                    OPTIONAL MATCH (m)-[:OVERRIDES]->(overridden:Method)<-[:HAS_METHOD]-(overriddenType:Type)
                    OPTIONAL MATCH (overridden)-[:HAS_PARAMETER]->(op:Parameter)-[:OF_TYPE]->(opt:Type)
                    OPTIONAL MATCH (overridden)-[:RETURNS]->(ort:Type)
                    WITH m, ownerType, ownerModule, pkg, superType, implementedInterfaces, definedInInterface, calledMethods, otherMethods,
                         overridden, overriddenType, ort,
                         [p IN COLLECT(DISTINCT { name: op.name, type: opt.name }) WHERE p.name IS NOT NULL OR p.type IS NOT NULL] AS overriddenParams
                    WITH m, ownerType, ownerModule, pkg, superType, implementedInterfaces, definedInInterface, calledMethods, otherMethods,
                         COLLECT(DISTINCT CASE
                             WHEN overridden.name IS NOT NULL THEN {
                               name: overridden.name,
                               ownerType: overriddenType.name,
                               parameters: overriddenParams,
                               returnType: ort.name
                             }
                         END) AS overrides
                    // 8a. Used fields (via DEPENDS_ON)
                    OPTIONAL MATCH (m)-[:DEPENDS_ON]->(usedField:Field)
                    OPTIONAL MATCH (usedField)-[:OF_TYPE]->(uft:Type)
                    WITH m, ownerType, ownerModule, pkg, superType, implementedInterfaces, definedInInterface,
                         calledMethods, otherMethods, overrides,
                         COLLECT(DISTINCT\s
                           coalesce(apoc.text.regexGroups(uft.name, '([^.]+)$')[0][0], uft.name) + ' ' + usedField.name
                         ) AS usedFields
                    // 8b. Declared fields on the owning type
                    OPTIONAL MATCH (ownerType)-[:HAS_FIELD]->(declaredField:Field)
                    WITH m, ownerType, ownerModule, pkg, superType, implementedInterfaces, definedInInterface,
                         calledMethods, otherMethods, overrides, usedFields,
                         COLLECT(DISTINCT declaredField.name) AS declaredFields
                    // 9. Final return
                    RETURN {
                      name:               m.name,
                      sourceCode:         m.sourceCode,
                      ownerType:          ownerType.name,
                      module:             ownerModule.name,
                      package:            pkg.name,
                      superClass:         superType.name,
                      implementsInterface: implementedInterfaces,
                      otherTypeInterfaces: definedInInterface,
                      parameters: [
                        (m)-[:HAS_PARAMETER]->(param:Parameter)-[:OF_TYPE]->(pt:Type) |\s
                          { name: param.name, type: pt.name }
                      ],
                      returnType: head([
                        (m)-[:RETURNS]->(rt:Type) | { type: rt.name }
                      ]),
                      calledMethods: calledMethods,
                      otherTypeMethods: otherMethods,
                      overrides: [o IN overrides WHERE o IS NOT NULL],
                      dependencies: [(m)-[:DEPENDS_ON]->(d:Type) | d.name],
                      usedFields: usedFields,
                      otherTypeFields: declaredFields,
                      methodId: m.id
                    } AS methodInfo
                    """;
    private final INeo4jProvider _neo4JProvider;
    private final Gson _gson;

    public MethodContextExtractorStep(INeo4jProvider neo4JProvider) {
        _neo4JProvider = neo4JProvider;
        _gson = new Gson();
    }

    @Override
    public Stream<Neo4jMethodSummaryContextResult> process(Integer batchSize) {
        _neo4JProvider.beginTransaction();

        // Need to materialize the results so we can ensure all records are processed by the db
        // in order to close the tx properly before we start inserting the results
        List<Neo4jMethodSummaryContextResult> result = _neo4JProvider.runCypher(CYPHER_QUERY + " LIMIT $batchSize",
                        Values.parameters("batchSize", batchSize))
                .stream()
                .map(r -> r.get("methodInfo").toString())
                .map(s -> _gson.fromJson(s, Neo4jMethodSummaryContextResult.class))
                .toList();
        _neo4JProvider.commitTransactionIfPresent();
        return result.stream();
    }
}
