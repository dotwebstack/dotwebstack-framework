package org.dotwebstack.framework.backend.rdf4j.query;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.RepositoryAdapter;
import org.dotwebstack.framework.backend.rdf4j.converters.BooleanConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.ByteConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.DateConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.DecimalConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.DoubleConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.FloatConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.IntConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.IntegerConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.IriConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.LongConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.Rdf4jConverterRouter;
import org.dotwebstack.framework.backend.rdf4j.converters.ShortConverter;
import org.dotwebstack.framework.backend.rdf4j.converters.StringConverter;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.scalars.Rdf4jScalars;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.converters.CoreConverter;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class StaticQueryFetcherTest {

  private static final String SELECT_QUERY = "SELECT DISTINCT ?breweries_with_query_ref_as_iri WHERE {\n"
      + "   ?subject <https://github.com/dotwebstack/beer/def#brewery> ?breweries_with_query_ref_as_iri\n" + "}";

  @Mock
  private RepositoryAdapter mockRepositoryAdapterMock;

  @Mock
  private DataFetchingEnvironment dataFetchingEnvironmentMock;

  @Mock
  private GraphQLFieldDefinition graphQlFieldDefinitionMock;

  @Mock
  private TupleQuery tupleQueryMock;

  @Mock
  private TupleQueryResult tupleQueryResultMock;

  private StaticQueryFetcher staticQueryFetcherUnderTest;

  @BeforeEach
  void setUp() {
    initMocks(this);

    List<CoreConverter<Value, ?>> converters =
        List.of(new ByteConverter(), new StringConverter(), new BooleanConverter(), new ByteConverter(),
            new DecimalConverter(), new DateConverter(), new DoubleConverter(), new FloatConverter(),
            new IntConverter(), new IntegerConverter(), new IriConverter(), new LongConverter(), new ShortConverter());

    Rdf4jConverterRouter converterRouter = new Rdf4jConverterRouter(converters);

    staticQueryFetcherUnderTest =
        new StaticQueryFetcher(mockRepositoryAdapterMock, Collections.emptyList(), converterRouter, SELECT_QUERY);
  }

  @Test
  void getForIriTest() {
    testGetSuccessful("IRI", "\"https://github.com/dotwebstack/beer/id/beer/6\"");
  }

  @Test
  void getForIdTest() {
    testGetSuccessful("ID", "\"https://github.com/dotwebstack/beer/id/beer/6\"");
  }

  @Test
  void getForStringTest() {
    testGetSuccessful("String", "\"https://github.com/dotwebstack/beer/id/beer/6\"");
  }

  @Test
  void getForBooleanTest() {
    testGetSuccessful("Boolean", true);
  }

  @Test
  void getForByteTest() {
    testGetSuccessful("Byte", Byte.parseByte("2"));
  }

  @Test
  void getForIntTest() {
    testGetSuccessful("Int", 1);
  }

  @Test
  void getForBigIntegerTest() {
    testGetSuccessful("BigInteger", BigInteger.TEN);
  }

  @Test
  void getForShortTest() {
    testGetSuccessful("Short", (short) 1);
  }

  @Test
  void getForFloatTest() {
    testGetSuccessful("Float", 1.0f);
  }

  @Test
  void getForDoubleTest() {
    testGetSuccessful("Double", 1.0);
  }

  @Test
  void getForDateTest() {
    testGetSuccessful("Date", new Date(System.currentTimeMillis()));
  }

  @Test
  void getForBigDecimalTest() {
    testGetSuccessful("BigDecimal", BigDecimal.ONE);
  }

  @Test
  void getForLongTest() {
    testGetSuccessful("Long", Long.MAX_VALUE);
  }

  @Test
  void getUnsupportedTest() {
    testGetUnsuccessful("UnsupportedType", "value");
  }

  private void testGetSuccessful(String type, Object value) {
    // Arrange
    arrange(type, value);

    // Act
    assertDoesNotThrow(() -> staticQueryFetcherUnderTest.get(dataFetchingEnvironmentMock));
  }

  private void testGetUnsuccessful(String type, Object value) {
    // Arrange
    arrange(type, value);

    // Act
    assertThrows(InvalidConfigurationException.class,
        () -> staticQueryFetcherUnderTest.get(dataFetchingEnvironmentMock));
  }

  private void arrange(String type, Object value) {
    List<GraphQLArgument> arguments = new ArrayList<>();
    GraphQLInputObjectType iriType = GraphQLInputObjectType.newInputObject()
        .name(type)
        .build();

    GraphQLArgument graphQlArgument = GraphQLArgument.newArgument()
        .name("subject")
        .type(iriType)
        .build();

    arguments.add(graphQlArgument);

    GraphQLInputObjectType stringType = GraphQLInputObjectType.newInputObject()
        .name("String")
        .build();

    GraphQLDirective directive = GraphQLDirective.newDirective()
        .name("sparql")
        .argument(GraphQLArgument.newArgument()
            .name("repository")
            .value("local")
            .type(stringType)
            .build())
        .argument(GraphQLArgument.newArgument()
            .name("queryRef")
            .value("test-iri")
            .type(stringType)
            .build())
        .build();

    Map<String, Object> environmentArguments = Collections.singletonMap("subject", value);

    when(graphQlFieldDefinitionMock.getName()).thenReturn("breweries_with_query_ref_as_iri");
    when(graphQlFieldDefinitionMock.getType()).thenReturn(Rdf4jScalars.IRI);
    when(graphQlFieldDefinitionMock.getDirective(Rdf4jDirectives.SPARQL_NAME)).thenReturn(directive);
    when(graphQlFieldDefinitionMock.getArguments()).thenReturn(arguments);

    when(dataFetchingEnvironmentMock.getFieldDefinition()).thenReturn(graphQlFieldDefinitionMock);
    when(dataFetchingEnvironmentMock.getArguments()).thenReturn(environmentArguments);

    when(tupleQueryResultMock.hasNext()).thenReturn(false);
    when(tupleQueryResultMock.next()).thenReturn(EmptyBindingSet.getInstance());

    when(tupleQueryMock.evaluate()).thenReturn(tupleQueryResultMock);

    when(mockRepositoryAdapterMock.prepareTupleQuery(eq("local"), any(DataFetchingEnvironment.class), eq(SELECT_QUERY)))
        .thenReturn(tupleQueryMock);
  }

  @Test
  void testSupports() {
    // Arrange
    when(graphQlFieldDefinitionMock.getType()).thenReturn(Rdf4jScalars.IRI);

    // Act
    boolean result = StaticQueryFetcher.supports(graphQlFieldDefinitionMock);

    // Assert
    assertTrue(result);
  }
}
