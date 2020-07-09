package org.dotwebstack.framework.core.validators;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.dotwebstack.framework.core.directives.CoreDirectives;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ConstraintValidatorTest {

  private JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  private ConstraintValidator validator =
      new ConstraintValidator(new CoreTraverser(new TypeDefinitionRegistry()), jexlEngine);

  @Mock
  private GraphQLFieldDefinition fieldDefinitionMock;

  @Test
  void validate_throwsException_ForGivenUnknownArgument() {
    assertThrows(DirectiveValidationException.class, () -> validator.validate(GraphQLArgument.newArgument()
        .name("unknownArg")
        .value(1L)
        .type(GraphQLInt)
        .build(), "name", 1, fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_ForGivenNullArgument() {
    validator.validate(GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_MIN)
        .type(GraphQLInt)
        .build(), "name", 1, fieldDefinitionMock, null);
  }

  @Test
  void validate_returnsNull_ForGivenMinArgument() {
    validator.validate(minArgument(1), "name", 1, fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForInvalidMinArgument() {
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(minArgument(2), "name", 1, fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_ForGivenMaxArgument() {
    validator.validate(maxArgument(20), "name", 20, fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForInvalidMaxArgument() {
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(maxArgument(21), "name", 22, fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_ForGivenOneOfArgument() {
    validator.validate(oneOfArgument(Arrays.asList("foo", "bar")), "name", "foo", fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForOneOfArgument() {
    assertThrows(DirectiveValidationException.class, () -> validator
        .validate(oneOfArgument(Arrays.asList("foo", "bar")), "name", "boom!", fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_ForGivenOneOfIntArgument() {
    validator.validate(oneOfIntArgument(Arrays.asList(1, 2)), "name", 2, fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForOneOfIntArgument() {
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(oneOfIntArgument(Arrays.asList(1, 2)), "name", 3, fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_patternArgument() {
    validator.validate(stringArgument("^[a-z][0-9][A-Z]$"), "pattern", "a4P", fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForValuesInArgument() {
    assertThrows(DirectiveValidationException.class, () -> validator
        .validate(valuesInArgument(Arrays.asList("foo", "bar")), "name", List.of("boom!"), fieldDefinitionMock, null));
  }

  @Test
  void validate_throwsNothing_ForValidValuesInArgument() {
    assertDoesNotThrow(() -> validator.validate(valuesInArgument(Arrays.asList("foo", "bar", "tic", "tac", "toe")),
        "name", List.of("foo", "tac"), fieldDefinitionMock, null));
  }

  @Test
  void validate_throwsException_patternArgument() {
    assertThrows(DirectiveValidationException.class, () -> validator.validate(stringArgument("^[a-z][0-9]$"), "pattern",
        "Alfa Brouwerij", fieldDefinitionMock, null));
  }

  @Test
  void validate_throwsException_exprArgument() {
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(expressionArgument("args.page > 10 && args.pageSize == 99"), "expr", 15,
            fieldDefinitionMock, Map.of("page", 15, "pageSize", 100)));
  }

  @Test
  void validate_throwsNothing_forValidArgumentInExprArgument() {
    assertDoesNotThrow(() -> validator.validate(expressionArgument("args.page / args.pageSize == 12"), "expr", 15,
        fieldDefinitionMock, Map.of("page", 36, "pageSize", 3)));
  }

  private GraphQLArgument minArgument(Object value) {
    return GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_MIN)
        .type(GraphQLInt)
        .value(value)
        .build();
  }

  private GraphQLArgument maxArgument(Object value) {
    return GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_MAX)
        .type(GraphQLInt)
        .value(value)
        .build();
  }

  private GraphQLArgument oneOfArgument(List<String> values) {
    return GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_ONEOF)
        .type(GraphQLList.list(GraphQLString))
        .value(values)
        .build();
  }

  private GraphQLArgument valuesInArgument(List<String> values) {
    return GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_VALUESIN)
        .type(GraphQLList.list(GraphQLString))
        .value(values)
        .build();
  }

  private GraphQLArgument oneOfIntArgument(List<Integer> values) {
    return GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_ONEOF_INT)
        .type(GraphQLList.list(GraphQLInt))
        .value(values)
        .build();
  }

  private GraphQLArgument stringArgument(String value) {
    return GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_PATTERN)
        .type(GraphQLString)
        .value(value)
        .build();
  }

  private GraphQLArgument expressionArgument(String value) {
    return GraphQLArgument.newArgument()
        .name(CoreDirectives.CONSTRAINT_ARG_EXPR)
        .type(GraphQLString)
        .value(value)
        .build();
  }

}
