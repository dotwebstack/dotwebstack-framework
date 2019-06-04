package org.dotwebstack.framework.backend.rdf4j.directives;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLDirectiveContainer;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.dotwebstack.framework.core.directives.DirectiveValidationException;
import org.springframework.stereotype.Component;

@Component
public class SparqlFilterValidator {

  private final NodeShapeRegistry nodeShapeRegistry;

  public SparqlFilterValidator(NodeShapeRegistry nodeShapeRegistry) {
    this.nodeShapeRegistry = nodeShapeRegistry;
  }

  void validate(GraphQLDirectiveContainer container, String typeName) {
    GraphQLDirective directive = container.getDirective(Rdf4jDirectives.SPARQL_FILTER_NAME);
    this.validateDirective(directive, directive.getName());
    directive.getArguments()
        .forEach(directiveArgument -> this.validateArgument(directiveArgument, directive.getName(), typeName));
  }

  private void validateDirective(GraphQLDirective directive, String fieldName) {
    GraphQLArgument expr = directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR);
    GraphQLArgument operator = directive.getArgument(Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR);

    if (expr.getValue() != null && operator.getValue() != null) {
      throw new DirectiveValidationException(
          "Found both an expression and an operator argument on sparql filter directive on " + "field '{}'", fieldName);
    }
  }


  private void validateArgument(GraphQLArgument argument, String name, String typeName) {
    if (argument.getValue() != null) {
      switch (argument.getName()) {
        case Rdf4jDirectives.SPARQL_FILTER_ARG_EXPR:
          break;
        case Rdf4jDirectives.SPARQL_FILTER_ARG_FIELD:
          checkField(argument, name, typeName);
          break;
        case Rdf4jDirectives.SPARQL_FILTER_ARG_OPERATOR:
          checkOperator(argument, name);
          break;
        default:
          throw new DirectiveValidationException("Unsupported filter argument with name '{}'", argument.getName());
      }
    }
  }

  private void checkField(GraphQLArgument argument, String name, String typeName) {
    if (nodeShapeRegistry.get(typeName)
        .getPropertyShape((String) argument.getValue()) == null) {
      throw new DirectiveValidationException(
          "SparqlFilter 'field' [{}] on field '{}' is invalid. It does not exist on type '{}'", argument.getValue(),
          name, typeName);
    }
  }

  private void checkOperator(GraphQLArgument argument, String name) {
    if (!argument.getValue()
        .toString()
        .matches("^(=|!=|<|<=|>|>=)$")) {
      throw new DirectiveValidationException(
          "SparqlFilter 'operator' [{}] on field '{}' is invalid. It should be one of: '=', '!=', '<', '<=', '>',"
              + " '>='",
          argument.getValue(), name);
    }
  }

}
