package com.mxhvlm.jllm.core.pipeline.step.code.callGraph;

import com.mxhvlm.jllm.core.datamodel.impl.code.CodeModel;
import com.mxhvlm.jllm.core.pipeline.IPipelineStep;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
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
