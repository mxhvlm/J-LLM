package org.example.impl.pipeline.code.method;

import org.example.impl.datamodel.api.code.wrapper.IMethod;
import org.example.impl.datamodel.api.code.wrapper.IType;
import org.example.impl.pipeline.IPipelineStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<Stream<IType>, Stream<IMethod>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractorStep.class);

    public Stream<IMethod> extractMethods(Stream<IType> types) {
        LOGGER.info("ModelExtractor: Extracting methods...");
        return types
                .map(IType::getMethods)
                .flatMap(Collection::stream)
                .distinct();
    }

    @Override
    public Stream<IMethod> process(Stream<IType> input) {
        return extractMethods(input);
    }
}
