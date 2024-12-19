package org.example.dbOutput;

import org.example.sourceImport.ModelExtractor;
import org.slf4j.Logger;

public abstract class Exporter implements IExporter {
    protected static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Exporter.class);
    protected final ModelExtractor _extractor;
    protected final Neo4jService _neo4jService;

    public Exporter(ModelExtractor extractor, Neo4jService neo4jService) {
        this._extractor = extractor;
        this._neo4jService = neo4jService;
    }
}
