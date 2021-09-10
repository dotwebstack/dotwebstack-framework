package org.dotwebstack.framework.ext.rml.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RmlConfigurationTest {

  @Mock
  private RmlMapperConfigurer rmlMapperConfigurer;

  @Test
  void namespaces_areDefault_whenNotConfigured() {
    var rmlConfiguration = new RmlConfiguration();

    Set<Namespace> namespaces = rmlConfiguration.namespaces();

    assertThat(namespaces, hasItems(RDF.NS, RDFS.NS, OWL.NS, XSD.NS, DCTERMS.NS));
  }

  @Test
  void namespaces_mergedCorrectly_forConfig() {
    var rmlConfiguration = new RmlConfiguration();
    Map<String, String> configuredNamespaces = new HashMap<>();
    configuredNamespaces.put("ex", "http://example.org/");
    configuredNamespaces.put("owl", "http://example.com/owl/");

    rmlConfiguration.setNamespaces(configuredNamespaces);

    Set<Namespace> namespaces = rmlConfiguration.namespaces();

    assertThat(namespaces, hasItems(RDF.NS, RDFS.NS, XSD.NS, DCTERMS.NS,
        new SimpleNamespace("ex", "http://example.org/"), new SimpleNamespace("owl", "http://example.com/owl/")));
  }

  @Test
  void rmlMapper_configuredCorrectly_forConfig() {
    var openApi = TestResources.openApi("config/openapi.yaml");
    var rmlOpenApiConfiguration = new RmlOpenApiConfiguration();
    var mappingsPerOperation = rmlOpenApiConfiguration.mappingsPerOperation(openApi);
    var rmlMapperConfiguration = new RmlConfiguration();

    var rmlMapper = rmlMapperConfiguration.rmlMapper(mappingsPerOperation, List.of(rmlMapperConfigurer));

    assertNotNull(rmlMapper);
    verify(rmlMapperConfigurer, times(1)).configureMapper(any());
  }
}
