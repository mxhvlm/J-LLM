package org.example.pipeline.code.callGraph.dynamic;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.example.datamodel.impl.code.CodeModel;
import org.example.pipeline.IPipelineStep;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.Set;
import java.util.stream.Stream;

public class InstantiatedTypeExtractionStep
    implements IPipelineStep<
        Pair<CodeModel, Stream<? extends CtExecutableReference<?>>>,
        Pair<Stream<CtTypeReference<?>>, Stream<? extends CtExecutableReference<?>>>> {

  private final Config _config;

  public InstantiatedTypeExtractionStep(Config config) {
    _config = config;
  }

  public Set<CtTypeReference<?>> getInstantiatedTypeReferences(CodeModel codeModel) {
    throw new NotImplementedException("Needs to be re-implemented using the wrapper");
//    return codeModel.getCtModel()
//        .getElements(new TypeFilter<CtConstructorCall<?>>(CtConstructorCall.class)).stream()
//        .map(CtConstructorCall::getType)
//        .filter(ref -> isInScope(ref.getQualifiedName()))
//        .collect(Collectors.toSet());
  }

  private boolean isInScope(String qualifiedName) {
    return _config.scope().stream().anyMatch(qualifiedName::startsWith);
  }

  @Override
  public Pair<Stream<CtTypeReference<?>>, Stream<? extends CtExecutableReference<?>>> process(
      Pair<CodeModel, Stream<? extends CtExecutableReference<?>>> input) {
    return Pair.of(getInstantiatedTypeReferences(input.getLeft()).stream(), input.getRight());
  }
}
