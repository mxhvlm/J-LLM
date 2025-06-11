package org.example.pipeline.spoon.callGraph;

import org.apache.commons.lang3.tuple.Pair;
import org.example.codeModel.CodeModel;
import org.example.pipeline.IPipelineStep;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.stream.Stream;

public class StaticExtractorStep implements IPipelineStep<CodeModel, Pair<CodeModel, Stream<? extends CtExecutableReference<?>>>> {
  @Override
  public Pair<CodeModel, Stream<? extends CtExecutableReference<?>>> process(CodeModel codeModel) {
      return Pair.of(codeModel, codeModel.getCtModel()
          .getElements(new TypeFilter<CtInvocation<?>>(CtInvocation.class))
          .stream()
          .map(CtInvocation::getExecutable)
          .distinct());
  }
}
