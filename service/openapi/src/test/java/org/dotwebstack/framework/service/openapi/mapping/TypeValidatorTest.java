package org.dotwebstack.framework.service.openapi.mapping;

import static graphql.Scalars.GraphQLBigDecimal;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLByte;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLLong;
import static graphql.Scalars.GraphQLShort;
import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TypeValidatorTest {

  @Test
  public void validateTypesOpenApiToGraphQ_throwsException_forUnsupportedType() {
    assertThrows(InvalidConfigurationException.class,
        () -> new TypeValidator().validateTypesOpenApiToGraphQ("not " + "supported", "float", "1"));
  }

  @Test
  public void validateTypesGraphQlToOpenApi_throwsException_forUnsupportedType() {
    assertThrows(InvalidConfigurationException.class,
        () -> new TypeValidator().validateTypesGraphQlToOpenApi("not " + "supported", "float", "1"));

  }

  @ParameterizedTest(name = "validateTypeGraphqlToOpenApi is valid with [{arguments}]")
  @MethodSource("getValidOasToGraphQl")
  public void validateTypesOpenApiToGraphQ_isValid(String oasType, String graphQlType, String identifier) {
    new TypeValidator().validateTypesOpenApiToGraphQ(oasType, graphQlType, identifier);
  }

  @ParameterizedTest(name = "validateTypesGraphQlToOpenApi is valid with [{arguments}]")
  @MethodSource("getValidGraphQlToOas")
  public void validateTypesGraphQlToOpenApi_isValid(String oasType, String graphQlType, String identifier) {
    new TypeValidator().validateTypesGraphQlToOpenApi(oasType, graphQlType, identifier);
  }

  private static Stream<Arguments> getValidOasToGraphQl() {
    return Stream.of(Arguments.of("string", GraphQLID.getName(), "1"),
        Arguments.of("string", GraphQLString.getName(), "1"), Arguments.of("number", GraphQLFloat.getName(), "1"),
        Arguments.of("number", GraphQLInt.getName(), "1"), Arguments.of("number", GraphQLLong.getName(), "1"),
        Arguments.of("number", GraphQLByte.getName(), "1"), Arguments.of("number", GraphQLShort.getName(), "1"),
        Arguments.of("number", GraphQLBigDecimal.getName(), "1"), Arguments.of("number", GraphQLString.getName(), "1"),
        Arguments.of("integer", GraphQLInt.getName(), "1"), Arguments.of("integer", GraphQLByte.getName(), "1"),
        Arguments.of("integer", GraphQLShort.getName(), "1"), Arguments.of("integer", GraphQLString.getName(), "1"),
        Arguments.of("boolean", GraphQLBoolean.getName(), "1"));
  }

  private static Stream<Arguments> getValidGraphQlToOas() {
    return Stream.of(Arguments.of("string", GraphQLID.getName(), "1"),
        Arguments.of("string", GraphQLString.getName(), "1"), Arguments.of("string", GraphQLFloat.getName(), "1"),
        Arguments.of("string", GraphQLInt.getName(), "1"), Arguments.of("string", GraphQLLong.getName(), "1"),
        Arguments.of("string", GraphQLByte.getName(), "1"), Arguments.of("string", GraphQLShort.getName(), "1"),
        Arguments.of("string", GraphQLBigDecimal.getName(), "1"), Arguments.of("number", GraphQLFloat.getName(), "1"),
        Arguments.of("number", GraphQLInt.getName(), "1"), Arguments.of("number", GraphQLLong.getName(), "1"),
        Arguments.of("number", GraphQLByte.getName(), "1"), Arguments.of("number", GraphQLShort.getName(), "1"),
        Arguments.of("number", GraphQLBigDecimal.getName(), "1"), Arguments.of("integer", GraphQLInt.getName(), "1"),
        Arguments.of("integer", GraphQLByte.getName(), "1"), Arguments.of("integer", GraphQLShort.getName(), "1"),
        Arguments.of("boolean", GraphQLBoolean.getName(), "1"));
  }
}
