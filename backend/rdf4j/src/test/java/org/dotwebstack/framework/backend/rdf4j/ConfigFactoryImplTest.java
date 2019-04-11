package org.dotwebstack.framework.backend.rdf4j;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.dotwebstack.framework.backend.rdf4j.ConfigFactory.ConfigCreator;
import org.eclipse.rdf4j.repository.config.RepositoryImplConfig;
import org.eclipse.rdf4j.repository.sparql.config.SPARQLRepositoryConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConfigFactoryImplTest {

  private final ConfigFactoryImpl configFactory = new ConfigFactoryImpl();

  @Test
  void create_CreatesRepositoryConfig_ForSparqlType() {
    // Arrange
    String endpointUrl = "http://foo";
    Map<String, Object> args = ImmutableMap
        .of(ConfigFactoryImpl.SPARQL_REPOSITORY_ARG_ENDPOINT_URL, endpointUrl);

    // Act
    RepositoryImplConfig result = configFactory
        .create(ConfigFactoryImpl.SPARQL_REPOSITORY_TYPE, args);

    // Assert
    assertThat(result, is(instanceOf(SPARQLRepositoryConfig.class)));
    assertThat(((SPARQLRepositoryConfig) result).getQueryEndpointUrl(), is(equalTo(endpointUrl)));
  }

  @Test
  void create_CreatesRepositoryConfig_ForCustomType() {
    // Arrange
    RepositoryImplConfig config = Mockito.mock(RepositoryImplConfig.class);
    ConfigCreator creator = Mockito.mock(ConfigCreator.class);
    Map<String, Object> creatorArgs = ImmutableMap.of();
    when(creator.create(creatorArgs)).thenReturn(config);

    // Act
    configFactory.registerRepositoryType("custom", args -> config);
    RepositoryImplConfig result = configFactory.create("custom", creatorArgs);

    // Assert
    assertThat(result, is(sameInstance(config)));
  }

}
