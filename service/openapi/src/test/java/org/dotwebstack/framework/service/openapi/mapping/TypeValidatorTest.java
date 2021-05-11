package org.dotwebstack.framework.service.openapi.mapping;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TypeValidatorTest {

  @Test
  void validateTypesOpenApiToGraphQ_throwsException_forUnsupportedType() {
    var typeValidator = new TypeValidator();
    assertThrows(InvalidConfigurationException.class,
        () -> typeValidator.validateTypesOpenApiToGraphQ("not " + "supported", "float", "1"));
  }

  @Test
  void validateTypesGraphQlToOpenApi_throwsException_forUnsupportedType() {
    var typeValidator = new TypeValidator();
    assertThrows(InvalidConfigurationException.class,
        () -> typeValidator.validateTypesGraphQlToOpenApi("not " + "supported", "float", "1"));

  }

  @ParameterizedTest(name = "validateTypesOpenApiToGraphQ is valid with [{arguments}]")
  @MethodSource("getValidOasToGraphQl")
  void validateTypesOpenApiToGraphQ_isValid(String oasType, String graphQlType, String identifier) {
    new TypeValidator().validateTypesOpenApiToGraphQ(oasType, graphQlType, identifier);
  }

  @ParameterizedTest(name = "validateTypesOpenApiToGraphQ is invalid with [{arguments}]")
  @MethodSource("getInvalidOasToGraphQl")
  void validateTypesOpenApiToGraphQ_throwsException_forInvalidMapping(String oasType, String graphQlType,
      String identifier) {
    var typeValidator = new TypeValidator();
    assertThrows(InvalidConfigurationException.class,
        () -> typeValidator.validateTypesOpenApiToGraphQ(oasType, graphQlType, identifier));
  }

  @ParameterizedTest(name = "validateTypesGraphQlToOpenApi is valid with [{arguments}]")
  @MethodSource("getValidGraphQlToOas")
  void validateTypesGraphQlToOpenApi_isValid(String oasType, String graphQlType, String identifier) {
    new TypeValidator().validateTypesGraphQlToOpenApi(oasType, graphQlType, identifier);
  }

  @ParameterizedTest(name = "validateTypesGraphQlToOpenApi is invalid with [{arguments}]")
  @MethodSource("getInvalidGraphQlToOas")
  void validateTypesGraphQlToOpenApithrowsException_forInvalidMapping(String oasType, String graphQlType,
      String identifier) {
    var typeValidator = new TypeValidator();
    assertThrows(InvalidConfigurationException.class,
        () -> typeValidator.validateTypesGraphQlToOpenApi(oasType, graphQlType, identifier));
  }

  private static Stream<Arguments> getValidOasToGraphQl() {
    return Stream.of(Arguments.of("string", GraphQLID.getName(), "1"),
        Arguments.of("string", GraphQLString.getName(), "1"), Arguments.of("number", GraphQLFloat.getName(), "1"),
        Arguments.of("number", GraphQLInt.getName(), "1"), Arguments.of("number", GraphQLString.getName(), "1"),
        Arguments.of("integer", GraphQLInt.getName(), "1"), Arguments.of("integer", GraphQLString.getName(), "1"),
        Arguments.of("boolean", GraphQLBoolean.getName(), "1"));
  }

  private static Stream<Arguments> getInvalidOasToGraphQl() {
    return Stream.of(Arguments.of("string", GraphQLInt.getName(), "1"),
        Arguments.of("number", GraphQLBoolean.getName(), "1"), Arguments.of("integer", GraphQLBoolean.getName(), "1"),
        Arguments.of("boolean", GraphQLFloat.getName(), "1"));
  }

  private static Stream<Arguments> getValidGraphQlToOas() {
    return Stream.of(Arguments.of("string", GraphQLID.getName(), "1"),
        Arguments.of("string", GraphQLString.getName(), "1"), Arguments.of("string", GraphQLFloat.getName(), "1"),
        Arguments.of("string", GraphQLInt.getName(), "1"), Arguments.of("number", GraphQLFloat.getName(), "1"),
        Arguments.of("number", GraphQLInt.getName(), "1"), Arguments.of("integer", GraphQLInt.getName(), "1"),
        Arguments.of("boolean", GraphQLBoolean.getName(), "1"));
  }

  private static Stream<Arguments> getInvalidGraphQlToOas() {
    return Stream.of(Arguments.of("number", GraphQLString.getName(), "1"),
        Arguments.of("integer", GraphQLBoolean.getName(), "1"), Arguments.of("boolean", GraphQLInt.getName(), "1"));
  }
}
