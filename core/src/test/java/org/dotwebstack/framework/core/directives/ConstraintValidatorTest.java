package org.dotwebstack.framework.core.directives;

import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;


class ConstraintValidatorTest {

  private ConstraintValidator validator = new ConstraintValidator();

  @Test
  void validate_throwsException_ForGivenUnknownArgument() {
    assertThrows(DirectiveValidationException.class,() -> validator.validate(
            GraphQLArgument.newArgument()
                    .name("unknownArg")
                    .value(1L)
                    .type(GraphQLInt).build(),"name",1));
  }

  @Test
  void validate_returnsNull_ForGivenNullArgument() {
    validator.validate(GraphQLArgument.newArgument().name(
            CoreDirectives.CONSTRAINT_ARG_MIN).type(GraphQLInt).build(),"name",1);
  }

  @Test
  void validate_returnsNull_ForGivenMinArgument() {
    validator.validate(minArgument(1),"name",1);
  }

  @Test
  void validate_throwsException_ForInvalidMinArgument() {
    assertThrows(DirectiveValidationException.class,() ->
            validator.validate(minArgument(2),"name",1));
  }

  @Test
  void validate_returnsNull_ForGivenMaxArgument() {
    validator.validate(maxArgument(20),"name",20);
  }

  @Test
  void validate_throwsException_ForInvalidMaxArgument() {
    assertThrows(DirectiveValidationException.class,() ->
            validator.validate(maxArgument(21),"name",22));
  }

  @Test
  void validate_returnsNull_ForGivenOneOfArgument() {
    validator.validate(oneOfArgument(Arrays.asList("foo","bar")),"name","foo");
  }

  @Test
  void validate_throwsException_ForOneOfArgument() {
    assertThrows(DirectiveValidationException.class,() ->
            validator.validate(oneOfArgument(Arrays.asList("foo","bar")),"name","boom!"));
  }

  @Test
  void validate_returnsNull_ForGivenOneOfIntArgument() {
    validator.validate(oneOfIntArgument(Arrays.asList(1,2)),"name",2);
  }

  @Test
  void validate_throwsException_ForOneOfIntArgument() {
    assertThrows(DirectiveValidationException.class,() ->
            validator.validate(oneOfIntArgument(Arrays.asList(1,2)),"name",3));

  }

  private GraphQLArgument minArgument(Object value) {
    return GraphQLArgument.newArgument()
            .name(CoreDirectives.CONSTRAINT_ARG_MIN)
            .type(GraphQLInt)
            .value(value)
            .build();
  }

  private GraphQLArgument maxArgument(Object value) {
    return GraphQLArgument.newArgument().name(CoreDirectives.CONSTRAINT_ARG_MAX)
            .type(GraphQLInt).value(value).build();
  }

  private GraphQLArgument oneOfArgument(List<String> values) {
    return GraphQLArgument.newArgument().name(CoreDirectives.CONSTRAINT_ARG_ONEOF)
            .type(GraphQLList.list(GraphQLString)).value(values).build();
  }

  private GraphQLArgument oneOfIntArgument(List<Integer> values) {
    return GraphQLArgument.newArgument().name(CoreDirectives.CONSTRAINT_ARG_ONEOF_INT)
            .type(GraphQLList.list(GraphQLInt)).value(values).build();
  }
}