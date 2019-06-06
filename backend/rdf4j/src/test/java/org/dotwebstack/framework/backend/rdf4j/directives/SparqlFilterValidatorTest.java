package org.dotwebstack.framework.backend.rdf4j.directives;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Collections;
import java.util.Optional;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
class SparqlFilterValidatorTest {

  private static SparqlFilterValidator validator = new SparqlFilterValidator();

  @Mock
  private ObjectTypeDefinition typeDefinition;

  @Mock
  private TypeDefinitionRegistry registry;

  @Mock
  private GraphQLDirective directive;

  @Test
  void validate_sparqlFilter_validField() {
    // Arrange
    when(registry.getType("Brewery", ObjectTypeDefinition.class)).thenReturn(Optional.of(typeDefinition));
    when(typeDefinition.getFieldDefinitions())
        .thenReturn(Collections.singletonList(new FieldDefinition("founded", null)));

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("field")
        .value("founded")
        .type(Scalars.GraphQLString)
        .build();

    // Act
    validator.checkField(argument, registry, "founded", "Brewery");

    // The purpose of the "Assert true" below, is to make sure this test is regarded as a valid test.
    // There is no output to assert, so when "assert true" is reached, it means the preceding code
    // didn't throw any Exceptions.
    assert true;
  }

  @Test
  void validate_sparqlFilter_invalidField() {
    // Arrange
    when(registry.getType("Brewery", ObjectTypeDefinition.class)).thenReturn(Optional.of(typeDefinition));
    when(typeDefinition.getFieldDefinitions())
        .thenReturn(Collections.singletonList(new FieldDefinition("founded", null)));

    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("field")
        .value("foundedFake")
        .type(Scalars.GraphQLString)
        .build();

    // Act && Assert that exception is thrown
    assertThrows(DirectiveValidationException.class,
        () -> validator.checkField(argument, registry, "founded", "Brewery"));
  }

  @Test
  void validate_sparqlFilter_validOperator() {
    // Arrange
    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("operator")
        .value("=")
        .type(Scalars.GraphQLString)
        .build();

    // Act
    validator.checkOperator(argument, "founded");

    // The purpose of the "Assert true" below, is to make sure this test is regarded as a valid test.
    // There is no output to assert, so when "assert true" is reached, it means the preceding code
    // didn't throw any Exceptions.
    assert true;
  }

  @Test
  void validate_sparqlFilter_invalidOperator() {
    // Arrange
    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("operator")
        .value("?")
        .type(Scalars.GraphQLString)
        .build();

    // Act && Assert that exception is thrown
    assertThrows(DirectiveValidationException.class, () -> validator.checkOperator(argument, "operator"));
  }

  @Test
  void validate_sparqlFilter_noOperator() {
    // Arrange
    GraphQLArgument argument = GraphQLArgument.newArgument()
        .name("operator")
        .type(Scalars.GraphQLString)
        .build();

    // Act
    validator.checkOperator(argument, "operator");

    // The purpose of the "Assert true" below, is to make sure this test is regarded as a valid test.
    // There is no output to assert, so when "assert true" is reached, it means the preceding code
    // didn't throw any Exceptions.
    assert true;
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  void validate_directive() {
    // Arrange
    GraphQLArgument expression = GraphQLArgument.newArgument()
        .name(Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR)
        .type(Scalars.GraphQLString)
        .value("test")
        .build();

    GraphQLArgument operator = GraphQLArgument.newArgument()
        .name(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR)
        .type(Scalars.GraphQLString)
        .value(null)
        .build();

    when(directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR)).thenReturn(expression);
    when(directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR)).thenReturn(operator);

    // Act
    validator.validateDirective(directive, "brewery");

    // The purpose of the "Assert true" below, is to make sure this test is regarded as a valid test.
    // There is no output to assert, so when "assert true" is reached, it means the preceding code
    // didn't throw any Exceptions.
    assert true;
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  void fail_when_both_operator_and_expr_are_provided() {
    // Arrange
    GraphQLArgument expression = GraphQLArgument.newArgument()
        .name(Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR)
        .type(Scalars.GraphQLString)
        .value("test")
        .build();

    GraphQLArgument operator = GraphQLArgument.newArgument()
        .name(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR)
        .type(Scalars.GraphQLString)
        .value("test")
        .build();

    when(directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR)).thenReturn(expression);
    when(directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR)).thenReturn(operator);

    // Act && Assert that exception is thrown
    assertThrows(DirectiveValidationException.class, () -> validator.validateDirective(directive, "brewery"));

  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  void pass_when_no_operator_and_expr_are_provided() {
    // Arrange
    GraphQLArgument expression = GraphQLArgument.newArgument()
        .name(Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR)
        .type(Scalars.GraphQLString)
        .value(null)
        .build();

    GraphQLArgument operator = GraphQLArgument.newArgument()
        .name(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR)
        .type(Scalars.GraphQLString)
        .value(null)
        .build();

    when(directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR)).thenReturn(expression);
    when(directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR)).thenReturn(operator);

    // The purpose of the "Assert true" below, is to make sure this test is regarded as a valid test.
    // There is no output to assert, so when "assert true" is reached, it means the preceding code
    // didn't throw any Exceptions.
    assert true;
  }

}
