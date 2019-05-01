package org.dotwebstack.framework.backend.rdf4j.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectQueryBuilderTest {

  private final JexlEngine jexlEngine = new JexlBuilder()
            .silent(false)
            .strict(true)
            .create();

  @Mock
  private QueryEnvironment environmentMock = mock(QueryEnvironment.class);

  @Mock
  private NodeShapeRegistry registryMock = mock(NodeShapeRegistry.class);

  @Mock
  private NodeShape nodeShapeMock = mock(NodeShape.class);

  @Mock
  GraphQLObjectType objectTypeMock = mock(GraphQLObjectType.class);

  private SubjectQueryBuilder subjectQueryBuilder;

  @BeforeEach
  void setUp() {
    when(environmentMock.getNodeShapeRegistry()).thenReturn(registryMock);
    when(environmentMock.getObjectType()).thenReturn(objectTypeMock);
    when(environmentMock.getNodeShapeRegistry().get(any(GraphQLObjectType.class)))
            .thenReturn(nodeShapeMock);
    subjectQueryBuilder = SubjectQueryBuilder.create(environmentMock, jexlEngine);
  }

  private GraphQLDirective getValidSparqlDirective() {
    return GraphQLDirective
            .newDirective()
            .name("sparql")
            .argument(GraphQLArgument
                    .newArgument()
                    .name("offset")
                    .type(Scalars.GraphQLString)
                    .value("(page - 1) * pageSize")
                    .build())
            .argument(GraphQLArgument
                    .newArgument()
                    .name("limit")
                    .type(Scalars.GraphQLString)
                    .value("pageSize")
                    .build())
            .build();
  }

  private GraphQLDirective getInvalidSparqlDirective() {
    return GraphQLDirective
            .newDirective()
            .name("sparql")
            .argument(GraphQLArgument
                    .newArgument()
                    .name("offset")
                    .type(Scalars.GraphQLString)
                    .value("this is an invalid expression")
                    .build())
            .argument(GraphQLArgument
                    .newArgument()
                    .name("limit")
                    .type(Scalars.GraphQLString)
                    .value("this is an invalid expression")
                    .build())
            .build();
  }

  @Test
  void test_pagingParameters_whenNoValuesSet() {
    GraphQLDirective validSparqlDirective = getValidSparqlDirective();
    Map<String, Object> arguments = new HashMap<>();

    MapContext context = new MapContext(arguments);

    assertThrows(org.apache.commons.jexl3.JexlException.Variable.class, () ->
            subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective));
    assertThrows(org.apache.commons.jexl3.JexlException.Variable.class, () ->
            subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
  }

  @Test
  void test_pagingParameters_withValidPageAndPageSize() {
    GraphQLDirective validSparqlDirective = getValidSparqlDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.putIfAbsent("page", 5);
    arguments.putIfAbsent("pageSize", 12);

    MapContext context = new MapContext(arguments);

    assertThat(subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective),
            is(equalTo(12)));
    assertThat(subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective),
            is(equalTo(48)));
  }

  @Test
  void test_pagingParameters_withInvalidPage() {
    GraphQLDirective validSparqlDirective = getValidSparqlDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.putIfAbsent("page", -1);
    arguments.putIfAbsent("pageSize", 12);

    MapContext context = new MapContext(arguments);

    assertThat(subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective),
            is(equalTo(12)));
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));

    assertThat(exception.toString(), equalTo(
            "java.lang.IllegalArgumentException: The given page is invalid"));
  }

  @Test
  void test_pagingParameters_withInvalidPageType() {
    GraphQLDirective validSparqlDirective = getValidSparqlDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.putIfAbsent("page", "test");
    arguments.putIfAbsent("pageSize", 12);

    MapContext context = new MapContext(arguments);

    assertThat(subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective),
            is(equalTo(12)));
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
    assertThat(exception.toString(), equalTo(
            "java.lang.NumberFormatException: For input string: \"test\""));
  }

  @Test
  void test_pagingParameters_withInvalidPageSize() {
    GraphQLDirective validSparqlDirective = getValidSparqlDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.putIfAbsent("page", 13);
    arguments.putIfAbsent("pageSize", -1);

    MapContext context = new MapContext(arguments);

    IllegalArgumentException limitException = assertThrows(IllegalArgumentException.class, () ->
            subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective));
    assertThat(limitException.toString(), equalTo(
            "java.lang.IllegalArgumentException: The given pageSize is invalid"));
    IllegalArgumentException offsetException = assertThrows(IllegalArgumentException.class, () ->
            subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
    assertThat(offsetException.toString(), equalTo(
            "java.lang.IllegalArgumentException: The given page is invalid"));
  }

  @Test
  void test_pagingParameters_withInvalidPageSizeType() {
    GraphQLDirective validSparqlDirective = getValidSparqlDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.putIfAbsent("page", 13);
    arguments.putIfAbsent("pageSize", "test");

    MapContext context = new MapContext(arguments);

    IllegalArgumentException limitException = assertThrows(IllegalArgumentException.class, () ->
            subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective));
    assertThat(limitException.toString(), equalTo(
            "java.lang.IllegalArgumentException: The given limit expression is invalid"));
    IllegalArgumentException offsetException = assertThrows(IllegalArgumentException.class, () ->
            subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
    assertThat(offsetException.toString(), equalTo(
            "java.lang.NumberFormatException: For input string: \"test\""));
  }

  @Test
  void test_pagingParameters_withInvalidLimitAndOffsetExpressions() {
    GraphQLDirective invalidSparqlDirective = getInvalidSparqlDirective();

    Map<String, Object> arguments = new HashMap<>();
    arguments.putIfAbsent("page", 1);
    arguments.putIfAbsent("pageSize", 5);

    MapContext context = new MapContext(arguments);

    JexlException.Parsing limitException = assertThrows(JexlException.Parsing.class, () ->
            subjectQueryBuilder.getLimitFromContext(context, invalidSparqlDirective));
    assertThat(limitException.toString(), equalTo(
            "org.apache.commons.jexl3.JexlException$Parsing: org.dotwebstack.framework.backend."
                    + "rdf4j.query.SubjectQueryBuilder.getLimitFromContext@1:6 parsing error in "
                    + "'is'"));
    JexlException.Parsing offsetException = assertThrows(JexlException.Parsing.class, () ->
            subjectQueryBuilder.getOffsetFromContext(context, invalidSparqlDirective));
    assertThat(offsetException.toString(), equalTo(
            "org.apache.commons.jexl3.JexlException$Parsing: org.dotwebstack.framework.backend."
                    + "rdf4j.query.SubjectQueryBuilder.getOffsetFromContext@1:6 parsing error in "
                    + "'is'"));
  }
}
