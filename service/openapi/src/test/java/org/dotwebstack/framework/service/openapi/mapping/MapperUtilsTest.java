package org.dotwebstack.framework.service.openapi.mapping;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.collectExactlyOne;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.getObjectField;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.getSuccessResponse;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.isPageableField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.service.openapi.exception.InvalidOpenApiConfigurationException;
import org.dotwebstack.framework.service.openapi.helper.OasConstants;
import org.junit.jupiter.api.Test;

class MapperUtilsTest {

  @Test
  void collectExactlyOne_returnsOptionalWithValue_whenExactlyOne() {
    var input = Stream.of("foo");
    var result = input.collect(collectExactlyOne());

    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(equalTo("foo")));
  }

  @Test
  void collectExactlyOne_returnsEmptyOptional_whenEmpty() {
    var input = Stream.empty();
    var result = input.collect(collectExactlyOne());

    assertThat(result.isEmpty(), is(true));
  }

  @Test
  void collectExactlyOne_returnsEmptyOptional_whenMoreThanOne() {
    var input = Stream.of("foo", "bar");
    var result = input.collect(collectExactlyOne());

    assertThat(result.isEmpty(), is(true));
  }

  @Test
  void getSuccessResponse_returnsSuccessResponse_whenExactlyOne() {
    var response200 = new ApiResponse();
    var operation = createOperation(new ApiResponses().addApiResponse("200", response200));

    var successResponse = getSuccessResponse(operation);

    assertThat(successResponse, is(response200));
  }

  @Test
  void getSuccessResponse_throwsException_whenMoeThanOne() {
    var operation = createOperation(new ApiResponses().addApiResponse("200", new ApiResponse())
        .addApiResponse("201", new ApiResponse()));

    assertThrows(InvalidOpenApiConfigurationException.class, () -> getSuccessResponse(operation));
  }

  private static Operation createOperation(ApiResponses apiResponses) {
    var operation = new Operation();
    operation.setResponses(apiResponses);
    return operation;
  }

  @Test
  void isEnvelope_returnsTrue_whenExtensionSetToTrue() {
    var schema = new ObjectSchema();
    schema.addExtension(OasConstants.X_DWS_ENVELOPE, true);

    assertThat(MapperUtils.isEnvelope(schema), is(true));
  }

  @Test
  void isEnvelope_returnsFalse_whenExtensionSetToFalse() {
    var schema = new ObjectSchema();
    schema.addExtension(OasConstants.X_DWS_ENVELOPE, false);

    assertThat(MapperUtils.isEnvelope(schema), is(false));
  }

  @Test
  void isEnvelope_returnsFalse_whenExtensionNotSet() {
    var schema = new ObjectSchema();

    assertThat(MapperUtils.isEnvelope(schema), is(false));
  }

  @Test
  void getObjectField_returnsField_whenPresent() {
    var objectType = createObjectType();

    var result = getObjectField(objectType, "name");

    assertThat(result, is(notNullValue()));
    assertThat(result.getName(), is("name"));
  }

  @Test
  void getObjectField_throwsException_whenAbsent() {
    var objectType = createObjectType();

    assertThrows(InvalidConfigurationException.class, () -> getObjectField(objectType, "foo"));
  }

  @Test
  void isPageableField_returnsFalse_forNonPageableField() {
    var fieldDefinition = createObjectType().getFieldDefinition("name");

    var pageable = isPageableField(fieldDefinition);

    assertThat(pageable, is(false));
  }

  @Test
  void isPageableField_returnsTrue_forPageableField() {
    var nodesFieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name("nodes")
        .type(Scalars.GraphQLString)
        .build();

    var fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name("pageable")
        .argument(GraphQLArgument.newArgument(GraphQLArgument.newArgument()
            .name(FIRST_ARGUMENT_NAME)
            .type(Scalars.GraphQLInt)
            .build())
            .build())
        .type(GraphQLObjectType.newObject()
            .name("Foo")
            .field(nodesFieldDefinition)
            .build())
        .build();

    var pageable = isPageableField(fieldDefinition);

    assertThat(pageable, is(true));
  }

  private static GraphQLObjectType createObjectType() {
    var fieldDefinition = GraphQLFieldDefinition.newFieldDefinition()
        .name("name")
        .type(Scalars.GraphQLString)
        .build();

    return GraphQLObjectType.newObject()
        .name("Brewery")
        .field(fieldDefinition)
        .build();
  }
}
