package org.example.impl.pipeline.code.callGraph;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.example.impl.datamodel.impl.code.CodeModel;
import org.example.impl.pipeline.IPipelineStep;
import spoon.reflect.reference.CtExecutableReference;

import java.util.stream.Stream;

public class StaticExtractorStep implements IPipelineStep<CodeModel, Pair<CodeModel, Stream<? extends CtExecutableReference<?>>>> {
    @Override
    public Pair<CodeModel, Stream<? extends CtExecutableReference<?>>> process(CodeModel codeModel) {
        throw new NotImplementedException("Needs to be re-implemented using the wrapper");
//      return Pair.of(codeModel, codeModel.getCtModel()
//          .getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
//          .stream()
//          .map(CtInvocation::getExecutable)
//          .distinct());
    }
}
