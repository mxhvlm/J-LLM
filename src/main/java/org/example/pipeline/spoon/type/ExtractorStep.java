package org.example.pipeline.spoon.type;

import org.example.CodeModel;
import org.example.pipeline.IPipelineStep;
import org.slf4j.Logger;
import spoon.reflect.declaration.CtType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ExtractorStep implements IPipelineStep<CodeModel, Stream<CtType<?>>> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ExtractorStep.class);

    private List<CtType<?>> collectAllTypesIncludingNested(Collection<CtType<?>> topLevelTypes) {
        List<CtType<?>> allTypes = new ArrayList<>(topLevelTypes.size());
        for (CtType<?> type : topLevelTypes) {
            collectRecursive(type, allTypes);
        }
        return allTypes;
    }

    private void collectRecursive(CtType<?> type, Collection<CtType<?>> collected) {
        collected.add(type);
        for (CtType<?> nested : type.getNestedTypes()) {
            collectRecursive(nested, collected);
        }
    }

    @Override
    public Stream<CtType<?>> process(CodeModel input) {
        LOGGER.info("ModelExtractor: Extracting types...");

        Collection<CtType<?>> types = input.getCtModel().getAllTypes();
        LOGGER.info("ModelExtractor: Found {} top-level types", types.size());
        types.addAll(collectAllTypesIncludingNested(types));

        return types.stream().distinct();
    }
}
