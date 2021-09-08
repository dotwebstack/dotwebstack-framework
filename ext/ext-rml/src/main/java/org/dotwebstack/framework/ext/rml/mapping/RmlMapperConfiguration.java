package org.dotwebstack.framework.ext.rml.mapping;

import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.logicalsourceresolver.JaywayJacksonJsonPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.vocab.Rdf;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RmlMapperConfiguration {

  @Bean
  RdfRmlMapper rmlMapper(Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation,
      List<RmlMapperConfigurer> rmlMapperConfigurers) {
    RdfRmlMapper.Builder mapperBuilder = RdfRmlMapper.builder()
        .setLogicalSourceResolver(Rdf.Ql.JsonPath, JaywayJacksonJsonPathResolver::getInstance)
        .triplesMaps(mappingsPerOperation.values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toUnmodifiableSet()));

    rmlMapperConfigurers.forEach(rmlMapperConfigurer -> rmlMapperConfigurer.configureMapper(mapperBuilder));

    return mapperBuilder.build();
  }

  @Bean
  Set<Namespace> namespaces() {
    return Set.of(RDF.NS, RDFS.NS, OWL.NS, SKOS.NS, PROV.NS, FOAF.NS, XSD.NS, DCTERMS.NS);
  }
}
