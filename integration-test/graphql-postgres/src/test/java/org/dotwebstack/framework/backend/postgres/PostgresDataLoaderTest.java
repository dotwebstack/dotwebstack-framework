package org.dotwebstack.framework.backend.postgres;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.ObjectTypeDefinition.newObjectTypeDefinition;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.Scalars;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.SelectedField;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyArgument;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockConnection;
import org.jooq.tools.jdbc.MockDataProvider;
import org.jooq.tools.jdbc.MockExecuteContext;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PostgresDataLoaderTest {

  private static final String FIELD_IDENTIFIER = "identifier";

  private static final String FIELD_NAME = "name";

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  private DatabaseClient databaseClient;

  private DatabaseClient.GenericExecuteSpec genericExecuteSpec;

  private FetchSpec<Map<String, Object>> fetchSpec;

  private PostgresDataLoader postgresDataLoader;

  @BeforeEach
  void beforeAll() {
    DSLContext dslContext = createDslContext();

    postgresDataLoader = new PostgresDataLoader(dotWebStackConfiguration, databaseClient, dslContext);
  }

  @Test
  void supports_returnsTrue_withPostgresTypeConfiguration() {
    // Arrange & Act
    boolean supported = postgresDataLoader.supports(new PostgresTypeConfiguration());

    // Assert
    assertThat(supported, is(Boolean.TRUE));
  }

  @Test
  void supports_returnsFalse_withNonPostgresTypeConfiguration() {
    // Arrange & Act
    boolean supported = postgresDataLoader.supports(new AbstractTypeConfiguration<>() {});

    // Assert
    assertThat(supported, is(Boolean.FALSE));
  }

  @Test
  void loadSingle() {
    // Arrange
    mockQueryContext();

    String identifier = "d3654375-95fa-46b4-8529-08b0f777bd6b";
    String name = "Brewery X";

    when(fetchSpec.one()).thenReturn(Mono.just(Map.of("x1", identifier, "x2", name)));

    when(genericExecuteSpec.bind(any(String.class), any(String.class))).thenReturn(genericExecuteSpec);

    LoadEnvironment loadEnvironment = createLoadEnvironment(identifier);

    // Act
    Map<String, Object> result = postgresDataLoader.loadSingle("identifier", loadEnvironment)
        .block(Duration.ofSeconds(5));

    // Assert
    assertThat(result, notNullValue());
    assertThat(result.entrySet(), equalTo(Map.of("identifier", identifier, "name", name)
        .entrySet()));

    verify(databaseClient)
        .sql("select identifier as \"x1\", name as \"x2\" from db.brewery as \"t1\" where t1.identifier = :1");
  }

  @Test
  void loadMany() {
    // Arrange
    mockQueryContext();

    List<Map<String, Object>> data = List.of(Map.of("x1", "d3654375-95fa-46b4-8529-08b0f777bd6b", "x2", "Brewery X"),
        Map.of("x1", "d3654375-95fa-46b4-8529-08b0f777bd6c", "x2", "Brewery Y"));

    when(fetchSpec.all()).thenReturn(Flux.fromIterable(data));

    LoadEnvironment loadEnvironment = createLoadEnvironment(null);

    // Act
    Map<String, Object> result = postgresDataLoader.loadMany(null, loadEnvironment)
        .blockLast(Duration.ofSeconds(5));

    // Assert
    assertThat(result, notNullValue());
    assertThat(result.entrySet(),
        equalTo(Map.of("identifier", "d3654375-95fa-46b4-8529-08b0f777bd6c", "name", "Brewery Y")
            .entrySet()));

    verify(databaseClient).sql("select identifier as \"x1\", name as \"x2\" from db.brewery as \"t1\"");
  }

  @SuppressWarnings("unchecked")
  private void mockQueryContext() {
    genericExecuteSpec = mock(DatabaseClient.GenericExecuteSpec.class);

    fetchSpec = mock(FetchSpec.class);

    when(genericExecuteSpec.fetch()).thenReturn(fetchSpec);

    when(databaseClient.sql(any(String.class))).thenReturn(genericExecuteSpec);
  }

  private LoadEnvironment createLoadEnvironment(String identifier) {
    PostgresTypeConfiguration typeConfiguration = createTypeConfiguration();

    LoadEnvironment.LoadEnvironmentBuilder loadEnvironmentBuilder = LoadEnvironment.builder()
        .typeConfiguration(typeConfiguration)
        .selectedFields(List.of(createSelectedField(FIELD_IDENTIFIER), createSelectedField(FIELD_NAME)));

    if (identifier != null) {
      KeyArgument keyArgument = KeyArgument.builder()
          .name(FIELD_IDENTIFIER)
          .value(identifier)
          .build();
      loadEnvironmentBuilder.keyArguments(List.of(keyArgument));
    }

    return loadEnvironmentBuilder.build();
  }

  private SelectedField createSelectedField(String name) {
    SelectedField selectedField = mock(SelectedField.class);
    when(selectedField.getName()).thenReturn(name);
    when(selectedField.getResultKey()).thenReturn(name);

    when(selectedField.getFieldDefinition()).thenReturn(GraphQLFieldDefinition.newFieldDefinition()
        .name(name)
        .type(Scalars.GraphQLString)
        .build());

    return selectedField;
  }

  private PostgresTypeConfiguration createTypeConfiguration() {
    PostgresTypeConfiguration typeConfiguration = new PostgresTypeConfiguration();
    KeyConfiguration keyConfiguration = new KeyConfiguration();
    keyConfiguration.setField(FIELD_IDENTIFIER);
    typeConfiguration.setKeys(List.of(keyConfiguration));
    typeConfiguration.setFields(new HashMap<>(Map.of(FIELD_IDENTIFIER, new PostgresFieldConfiguration())));
    typeConfiguration.setTable("db.brewery");

    typeConfiguration.init(newObjectTypeDefinition().name("Brewery")
        .fieldDefinition(newFieldDefinition().name(FIELD_IDENTIFIER)
            .build())
        .fieldDefinition(newFieldDefinition().name(FIELD_NAME)
            .build())
        .build());

    return typeConfiguration;
  }

  private DSLContext createDslContext() {
    MockConnection connection = new MockConnection(new TestDataProvider());

    return DSL.using(connection, SQLDialect.POSTGRES);
  }

  private static class TestDataProvider implements MockDataProvider {

    @Override
    public MockResult[] execute(MockExecuteContext mockExecuteContext) {
      return null;
    }
  }
}
