package org.dotwebstack.framework.ext.rml.mapping;

import io.carml.engine.rdf.RdfRmlMapper;
import io.carml.logicalsourceresolver.JsonPathResolver;
import io.carml.model.TriplesMap;
import io.carml.vocab.Rdf;
import io.swagger.v3.oas.models.Operation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.Setter;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "dotwebstack.rml")
@Setter
public class RmlConfiguration {

  private static final Set<Namespace> DEFAULT_NAMESPACES = Set.of(RDF.NS, RDFS.NS, OWL.NS, XSD.NS, DCTERMS.NS);

  private Map<String, String> namespacePrefixes = new HashMap<>();

  @Bean
  Set<Namespace> namespaces() {
    if (namespacePrefixes.isEmpty()) {
      return DEFAULT_NAMESPACES;
    }

    DEFAULT_NAMESPACES.forEach(
        ns -> namespacePrefixes.merge(ns.getPrefix(), ns.getName(), (configuredNs, defaultNs) -> configuredNs));

    return namespacePrefixes.entrySet()
        .stream()
        .map(entry -> new SimpleNamespace(entry.getKey(), entry.getValue()))
        .collect(Collectors.toUnmodifiableSet());
  }

  @Bean
  RdfRmlMapper rmlMapper(@NonNull Map<Operation, Set<TriplesMap>> mappingsPerOperation,
      @NonNull List<RmlMapperConfigurer> rmlMapperConfigurers) {

    RdfRmlMapper.Builder mapperBuilder = RdfRmlMapper.builder()
        .setLogicalSourceResolver(Rdf.Ql.JsonPath, JsonPathResolver::getInstance)
        .triplesMaps(mappingsPerOperation.values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet()));

    rmlMapperConfigurers.forEach(rmlMapperConfigurer -> rmlMapperConfigurer.configureMapper(mapperBuilder));

    return mapperBuilder.build();
  }
}
