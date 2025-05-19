package org.example.data.neo4j;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record Neo4jMethodSummaryContextResult(
    String methodId,
    String name,
    String sourceCode,
    String ownerType,
    String module,
    @SerializedName("package")
    String _package,
    String superClass,
    List<String> implementsInterface,
    List<String> otherTypeInterfaces,
    List<Parameter> parameters,
    ReturnType returnType,
    List<String> calledMethods,
    List<String> otherTypeMethods,
    List<OverrideInfo> overrides,
    List<String> dependencies,
    List<String> usedFields,
    List<String> otherTypeFields
) {
  public record Parameter(
      String name,
      String type
  ) {}

  public record ReturnType(
      String type
  ) {}

  public record OverrideInfo(
      String name,
      String ownerType,
      List<Parameter> parameters,
      String returnType
  ) {}

  public String toStringWithoutSourceCode() {
    return "Neo4jMethodSummaryContextResult{" +
        "name='" + name + '\'' +
        ", ownerType='" + ownerType + '\'' +
        ", module='" + module + '\'' +
        ", package='" + _package + '\'' +
        ", superClass='" + superClass + '\'' +
        ", implementsInterface=" + implementsInterface +
        ", otherTypeInterfaces=" + otherTypeInterfaces +
        ", parameters=" + parameters +
        ", returnType=" + returnType +
        ", calledMethods=" + calledMethods +
        ", otherTypeMethods=" + otherTypeMethods +
        ", overrides=" + overrides +
        ", dependencies=" + dependencies +
        ", usedFields=" + usedFields +
        ", otherTypeFields=" + otherTypeFields +
        '}';
  }
}
