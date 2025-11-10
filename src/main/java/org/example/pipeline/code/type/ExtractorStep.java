package org.example.pipeline.code.type;

import org.example.datamodel.code.CodeModel;
import org.example.datamodel.code.wrapper.IType;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<CodeModel, Stream<IType>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ExtractorStep.class);

    @Override
    public Stream<IType> process(CodeModel input) {
        LOGGER.info("ModelExtractor: Extracting types...");

        Collection<IType> types = input.getTypes();

        return types.stream().distinct();
    }
}
