package org.example;

import org.example.dbOutput.*;
import org.example.sourceImport.ModelBuilder;
import org.example.sourceImport.ModelExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class JLLM implements Runnable {
    public static final Logger LOGGER = LoggerFactory.getLogger(JLLM.class);
    private CodeModel _model;
    private ModelExtractor _extractor;
    private final JLLMConfig _config;

    private final List<IExporter> _exporters;

    public JLLM() {
        LOGGER.info("JLLM: Initializing...");
        _config = new JLLMConfig();
        _exporters = new LinkedList<>();
    }

    private void registerExporters(Neo4jService neo4jService) {
        LOGGER.info("Registering exporters...");
        _exporters.add(new PackageExporter(_extractor, neo4jService));
        _exporters.add(new TypeExporter(_extractor, neo4jService));
        _exporters.add(new FieldExporter(_extractor, neo4jService));
        _exporters.add(new MethodExporter(_extractor, neo4jService));

        LOGGER.info("Registered " + _exporters.size() + " exporters.");
    }

    private void runExporters() {
        LOGGER.info("Running exporters...");
        for (IExporter exporter : _exporters) {
            exporter.export();
        }
        LOGGER.info("Exporters finished.");
    }

    @Override
    public void run() {
        try (Neo4jService neo4jService = new Neo4jService(
                _config.getNeo4jUri(),
                _config.getNeo4jUser(),
                _config.getNeo4jPassword())) {
            _model = new ModelBuilder(_config.getInputPath()).buildModel();
            _model.printStatistics();

            _extractor = new ModelExtractor(_model);

            neo4jService.purgeDatabase();

            registerExporters(neo4jService);
            runExporters();

            System.out.println("Modules and packages imported successfully.");
        }
    }
}
