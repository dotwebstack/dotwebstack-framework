package org.dotwebstack.framework.backend.rdf4j.query;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLObjectType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.MapContext;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.context.SelectVerticeFactory;
import org.dotwebstack.framework.backend.rdf4j.serializers.LocalDateSerializer;
import org.dotwebstack.framework.backend.rdf4j.serializers.Serializer;
import org.dotwebstack.framework.backend.rdf4j.serializers.SerializerRouter;
import org.dotwebstack.framework.backend.rdf4j.serializers.ZonedDateTimeSerializer;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShapeRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubjectQueryBuilderTest {

  private final JexlEngine jexlEngine = new JexlBuilder().silent(false)
      .strict(true)
      .create();

  @Mock
  private QueryEnvironment environmentMock;

  @Mock
  private NodeShapeRegistry registryMock;

  @Mock
  private NodeShape nodeShapeMock;

  @Mock
  private GraphQLObjectType objectTypeMock;

  private SubjectQueryBuilder subjectQueryBuilder;

  List<Serializer> serializers = ImmutableList.of(new LocalDateSerializer(), new ZonedDateTimeSerializer());

  @BeforeEach
  void setUp() {
    when(this.environmentMock.getNodeShapeRegistry()).thenReturn(this.registryMock);
    when(this.environmentMock.getObjectType()).thenReturn(this.objectTypeMock);
    when(this.environmentMock.getNodeShapeRegistry()
        .get(any(GraphQLObjectType.class))).thenReturn(this.nodeShapeMock);
    this.subjectQueryBuilder = SubjectQueryBuilder.create(this.environmentMock, this.jexlEngine,
        new SelectVerticeFactory(new SerializerRouter(serializers)));
  }

  private GraphQLDirective getValidPagingDirective() {
    return GraphQLDirective.newDirective()
        .name("sparql")
        .argument(GraphQLArgument.newArgument()
            .name(Rdf4jDirectives.SPARQL_ARG_OFFSET)
            .type(Scalars.GraphQLString)
            .value("(page - 1) * pageSize")
            .build())
        .argument(GraphQLArgument.newArgument()
            .name(Rdf4jDirectives.SPARQL_ARG_LIMIT)
            .type(Scalars.GraphQLString)
            .value("pageSize")
            .build())
        .build();
  }

  private GraphQLDirective getInvalidPagingDirective() {
    return GraphQLDirective.newDirective()
        .name("sparql")
        .argument(GraphQLArgument.newArgument()
            .name(Rdf4jDirectives.SPARQL_ARG_OFFSET)
            .type(Scalars.GraphQLString)
            .value("this is an invalid expression")
            .build())
        .argument(GraphQLArgument.newArgument()
            .name(Rdf4jDirectives.SPARQL_ARG_LIMIT)
            .type(Scalars.GraphQLString)
            .value("this is an invalid expression")
            .build())
        .build();
  }

  @Test
  void test_pagingParameters_whenNoExpressionsSet() {
    // Arrange
    GraphQLDirective emptySparqlDirective = GraphQLDirective.newDirective()
        .name("sparql")
        .build();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("page", 1);
    arguments.put("pageSize", 12);

    MapContext context = new MapContext(arguments);

    // Act / Assert
    assertThat(this.subjectQueryBuilder.getLimitFromContext(context, emptySparqlDirective),
        is(equalTo(Optional.empty())));
    assertThat(this.subjectQueryBuilder.getOffsetFromContext(context, emptySparqlDirective),
        is(equalTo(Optional.empty())));
  }

  @Test
  void test_pagingParameters_whenNoValuesSet() {
    // Arrange
    GraphQLDirective validSparqlDirective = getValidPagingDirective();
    Map<String, Object> arguments = new HashMap<>();

    MapContext context = new MapContext(arguments);

    // Act / Assert
    assertThrows(org.apache.commons.jexl3.JexlException.Variable.class,
        () -> this.subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective));
    assertThrows(org.apache.commons.jexl3.JexlException.Variable.class,
        () -> this.subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
  }

  @Test
  void test_pagingParameters_withValidPageAndPageSize() {
    // Arrange
    GraphQLDirective validSparqlDirective = getValidPagingDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("page", 5);
    arguments.put("pageSize", 12);

    MapContext context = new MapContext(arguments);

    // Act / Assert
    assertThat(this.subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective),
        is(equalTo(Optional.of(12))));
    assertThat(this.subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective),
        is(equalTo(Optional.of(48))));
  }

  @Test
  void test_pagingParameters_withInvalidPage() {
    // Arrange
    GraphQLDirective validSparqlDirective = getValidPagingDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("page", -1);
    arguments.put("pageSize", 12);

    MapContext context = new MapContext(arguments);

    // Act / Assert
    assertThat(this.subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective),
        is(equalTo(Optional.of(12))));
    assertThrows(IllegalArgumentException.class,
        () -> this.subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
  }

  @Test
  void test_pagingParameters_withInvalidPageType() {
    // Arrange
    GraphQLDirective validSparqlDirective = getValidPagingDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("page", "test");
    arguments.put("pageSize", 12);

    MapContext context = new MapContext(arguments);

    // Act / Assert
    assertThat(this.subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective),
        is(equalTo(Optional.of(12))));
    assertThrows(IllegalArgumentException.class,
        () -> this.subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
  }

  @Test
  void test_pagingParameters_withInvalidPageSize() {
    // Arrange
    GraphQLDirective validSparqlDirective = getValidPagingDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("page", 13);
    arguments.put("pageSize", -1);

    MapContext context = new MapContext(arguments);

    // Act / Assert
    assertThrows(IllegalArgumentException.class,
        () -> this.subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective));
    assertThrows(IllegalArgumentException.class,
        () -> this.subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
  }

  @Test
  void test_pagingParameters_withInvalidPageSizeType() {
    // Arrange
    GraphQLDirective validSparqlDirective = getValidPagingDirective();
    Map<String, Object> arguments = new HashMap<>();
    arguments.put("page", 13);
    arguments.put("pageSize", "test");

    MapContext context = new MapContext(arguments);

    // Act / Assert
    assertThrows(IllegalArgumentException.class,
        () -> this.subjectQueryBuilder.getLimitFromContext(context, validSparqlDirective));
    assertThrows(IllegalArgumentException.class,
        () -> this.subjectQueryBuilder.getOffsetFromContext(context, validSparqlDirective));
  }

  @Test
  void test_pagingParameters_withInvalidLimitAndOffsetExpressions() {
    // Arrange
    GraphQLDirective invalidSparqlDirective = getInvalidPagingDirective();

    Map<String, Object> arguments = new HashMap<>();
    arguments.put("page", 1);
    arguments.put("pageSize", 5);

    MapContext context = new MapContext(arguments);

    // Act / Assert
    assertThrows(org.apache.commons.jexl3.JexlException.Parsing.class,
        () -> this.subjectQueryBuilder.getLimitFromContext(context, invalidSparqlDirective));
    assertThrows(org.apache.commons.jexl3.JexlException.Parsing.class,
        () -> this.subjectQueryBuilder.getOffsetFromContext(context, invalidSparqlDirective));
  }
}
