package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.config.Rdf4jTypeConfiguration;
import org.dotwebstack.framework.backend.rdf4j.query.QueryBuilder;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.eclipse.rdf4j.repository.manager.LocalRepositoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Rdf4jDataLoaderTest {

  @Mock
  private LocalRepositoryManager localRepositoryManager;

  @Mock
  private DotWebStackConfiguration dotWebStackConfiguration;

  @Mock
  private QueryBuilder queryBuilder;

  private Rdf4jDataLoader rdf4jDataLoader;

  @BeforeEach
  void init() {
    rdf4jDataLoader = new Rdf4jDataLoader(dotWebStackConfiguration, queryBuilder, localRepositoryManager);
  }

  @Test
  void supports_True_ForRdf4jTypeConfiguration() {
    // Arrange
    Rdf4jTypeConfiguration rdf4jTypeConfiguration = new Rdf4jTypeConfiguration();

    // Act / Assert
    assertThat(rdf4jDataLoader.supports(rdf4jTypeConfiguration), is(true));
  }

  @Test
  void supports_False_ForUnsupportedConfiguration() {
    // Arrange
    UnsupportedTypeConfiguration unsupportedTypeConfiguration = new UnsupportedTypeConfiguration();

    // Act / Assert
    assertThat(rdf4jDataLoader.supports(unsupportedTypeConfiguration), is(false));
  }

  @Test
  void batchLoadSingle_ThrowsException_ForEveryCall() {
    assertThrows(UnsupportedOperationException.class, () -> rdf4jDataLoader.batchLoadSingle(null, null));
  }

  private static class UnsupportedTypeConfiguration extends AbstractTypeConfiguration<UnsupportedFieldConfiguration> {
    @Override
    public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
      return null;
    }

    @Override
    public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
      return null;
    }

    @Override
    public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
      return null;
    }
  }

  private static class UnsupportedFieldConfiguration extends AbstractFieldConfiguration {
  }
}
