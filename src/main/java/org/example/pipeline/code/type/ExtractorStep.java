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

    private List<IType> collectAllTypesIncludingNested(Collection<IType> topLevelTypes) {
        List<IType> allTypes = new ArrayList<>(topLevelTypes.size());
        for (IType type : topLevelTypes) {
            collectRecursive(type, allTypes);
        }
        return allTypes;
    }

    private void collectRecursive(IType type, Collection<IType> collected) {
        collected.add(type);
        for (IType nested : type.getInnerTypes()) {
            collectRecursive(nested, collected);
        }
    }

    @Override
    public Stream<IType> process(CodeModel input) {
        LOGGER.info("ModelExtractor: Extracting types...");

        Collection<IType> types = input.getTypes();
        LOGGER.info("ModelExtractor: Found {} top-level types", types.size());
        types.addAll(collectAllTypesIncludingNested(types));

        return types.stream().distinct();
    }
}
