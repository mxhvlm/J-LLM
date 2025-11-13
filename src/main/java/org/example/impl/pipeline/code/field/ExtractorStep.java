package org.example.impl.pipeline.code.field;

import org.example.impl.datamodel.api.code.wrapper.IField;
import org.example.impl.datamodel.api.code.wrapper.IType;
import org.example.impl.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<Stream<IType>, Stream<IField>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorStep.class);

    public Stream<IField> extractFields(Stream<IType> types) {
        LOGGER.info("ModelExtractor: Extracting fields...");
        return types
                .map(IType::getFields)
                .flatMap(List::stream);
    }

    @Override
    public Stream<IField> process(Stream<IType> input) {
        return extractFields(input);
    }
}
