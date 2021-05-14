package org.dotwebstack.framework.core.validators;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    var graphQlArgument = GraphQLArgument.newArgument()
        .name("unknownArg")
        .value(1L)
        .type(GraphQLInt)
        .build();

    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(graphQlArgument, "name", 1, fieldDefinitionMock, null));
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
    var graphQlArgument = minArgument(2);
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(graphQlArgument, "name", 1, fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_ForGivenMaxArgument() {
    validator.validate(maxArgument(20), "name", 20, fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForInvalidMaxArgument() {
    var graphQlArgument = maxArgument(21);
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(graphQlArgument, "name", 22, fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_ForGivenOneOfArgument() {
    validator.validate(oneOfArgument(Arrays.asList("foo", "bar")), "name", "foo", fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForOneOfArgument() {
    var graphQlArgument = oneOfArgument(Arrays.asList("foo", "bar"));
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(graphQlArgument, "name", "boom!", fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_ForGivenOneOfIntArgument() {
    validator.validate(oneOfIntArgument(Arrays.asList(1, 2)), "name", 2, fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForOneOfIntArgument() {
    var graphQlArgument = oneOfIntArgument(Arrays.asList(1, 2));
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(graphQlArgument, "name", 3, fieldDefinitionMock, null));
  }

  @Test
  void validate_returnsNull_patternArgument() {
    validator.validate(stringArgument("^[a-z][0-9][A-Z]$"), "pattern", "a4P", fieldDefinitionMock, null);
  }

  @Test
  void validate_throwsException_ForValuesInArgument() {
    var graphQlArgument = valuesInArgument(Arrays.asList("foo", "bar"));
    var value = List.of("boom!");
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(graphQlArgument, "name", value, fieldDefinitionMock, null));
  }

  @Test
  void validate_throwsNothing_ForValidValuesInArgument() {
    assertDoesNotThrow(() -> validator.validate(valuesInArgument(Arrays.asList("foo", "bar", "tic", "tac", "toe")),
        "name", List.of("foo", "tac"), fieldDefinitionMock, null));
  }

  @Test
  void validate_throwsException_patternArgument() {
    var graphQlArgument = stringArgument("^[a-z][0-9]$");
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(graphQlArgument, "pattern", "Alfa Brouwerij", fieldDefinitionMock, null));
  }

  @Test
  void validate_throwsException_exprArgument() {
    var graphQlArgument = expressionArgument("args.page > 10 && args.pageSize == 99");
    Map<String, Object> requestArguments = Map.of("page", 15, "pageSize", 100);
    assertThrows(DirectiveValidationException.class,
        () -> validator.validate(graphQlArgument, "expr", 15, fieldDefinitionMock, requestArguments));
  }

  @Test
  void validate_throwsNothing_forValidArgumentInExprArgument() {
    assertDoesNotThrow(() -> validator.validate(expressionArgument("args.page / args.pageSize == 12"), "expr", 15,
        fieldDefinitionMock, Map.of("page", 36, "pageSize", 3)));
  }

  @Test
  void validate_works_withoutAlternateRequestArguments() {
    when(fieldDefinitionMock.getArguments()).thenReturn(List.of(GraphQLArgument.newArgument()
        .name("page")
        .value(1)
        .type(GraphQLInt)
        .build(),
        GraphQLArgument.newArgument()
            .name("pageSize")
            .value(25)
            .type(GraphQLInt)
            .build()));

    assertDoesNotThrow(
        () -> validator.validate(expressionArgument("args.page > 0 && args.page <= ( 1000 / args.pageSize )"), "expr",
            1, fieldDefinitionMock, null));
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
