package org.dotwebstack.framework.core.helpers;

import static graphql.execution.ExecutionStepInfo.newExecutionStepInfo;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.GraphQLFieldDefinition;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.model.Query;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.model.Subscription;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

class ContextCriteriaHelperTest {

  @Test
  void create_returnCriteria_forQueryWithNoFields() {
    var schema = new Schema();

    var query = new Query();
    query.setContext("myContext");

    schema.getQueries()
        .put("myQuery", query);
    schema.getContexts()
        .put("myContext", new Context());

    var requestStepInfo = mock(ExecutionStepInfo.class);
    var fieldDefinition = mock(GraphQLFieldDefinition.class);

    when(fieldDefinition.getName()).thenReturn("myQuery");
    when(requestStepInfo.getFieldDefinition()).thenReturn(fieldDefinition);

    var container = createExecutionStepInfo("Query");

    when(requestStepInfo.getParent()).thenReturn(container);

    var criteria = ContextCriteriaHelper.createContextCriteria(schema, requestStepInfo);

    assertThat(criteria, notNullValue());
    assertThat(criteria.getName(), CoreMatchers.equalTo("myContext"));
    assertThat(criteria.getValues()
        .size(), CoreMatchers.equalTo(0));
  }

  @Test
  void create_throwsException_forNonRequestStepInfo() {
    var schema = new Schema();

    var requestStepInfo = mock(ExecutionStepInfo.class);
    var fieldDefinition = mock(GraphQLFieldDefinition.class);

    when(fieldDefinition.getName()).thenReturn("myQuery");
    when(requestStepInfo.getFieldDefinition()).thenReturn(fieldDefinition);

    var container = createExecutionStepInfo("Foo");

    when(requestStepInfo.getParent()).thenReturn(container);

    var thrown =
        assertThrows(IllegalArgumentException.class, () -> ContextCriteriaHelper.createContextCriteria(schema, requestStepInfo));
    assertThat(thrown.getMessage(),
        equalTo("The parent type of the given requestStepInfo is not of type 'Query' or 'Subscription"));
  }

  @Test
  void create_returnCriteria_forSubscriptionWithNoFields() {
    var schema = new Schema();

    var subscription = new Subscription();
    subscription.setContext("myContext");

    schema.getSubscriptions()
        .put("mySubscription", subscription);
    schema.getContexts()
        .put("myContext", new Context());

    var requestStepInfo = mock(ExecutionStepInfo.class);
    var fieldDefinition = mock(GraphQLFieldDefinition.class);

    when(fieldDefinition.getName()).thenReturn("mySubscription");
    when(requestStepInfo.getFieldDefinition()).thenReturn(fieldDefinition);

    var container = createExecutionStepInfo("Subscription");

    when(requestStepInfo.getParent()).thenReturn(container);

    var criteria = ContextCriteriaHelper.createContextCriteria(schema, requestStepInfo);

    assertThat(criteria, notNullValue());
    assertThat(criteria.getName(), CoreMatchers.equalTo("myContext"));
    assertThat(criteria.getValues()
        .size(), CoreMatchers.equalTo(0));
  }

  private ExecutionStepInfo createExecutionStepInfo(String typeName) {
    return newExecutionStepInfo().type(newObject().name(typeName)
        .build())
        .build();
  }
}
