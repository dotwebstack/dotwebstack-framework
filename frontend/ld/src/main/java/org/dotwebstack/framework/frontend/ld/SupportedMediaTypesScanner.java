package org.dotwebstack.framework.frontend.ld;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

@Service
public class SupportedMediaTypesScanner {
  private static final Logger LOG = LoggerFactory.getLogger(SupportedMediaTypesScanner.class);

  private List<MediaType> graphMediaTypes = new ArrayList<>();

  private List<MediaType> tupleMediaTypes = new ArrayList<>();

  @PostConstruct
  void loadSupportedMediaTypes() {
    ClassPathScanningCandidateComponentProvider scanner =
        new ClassPathScanningCandidateComponentProvider(false);

    scanner.addIncludeFilter(new AnnotationTypeFilter(SparqlProvider.class));

    Set<BeanDefinition> sparqlProviderBeans = scanner.findCandidateComponents("org.dotwebstack");
    for (BeanDefinition sparlProviderBean : sparqlProviderBeans) {
      try {
        Class<?> sparqlProviderClass = Class.forName(sparlProviderBean.getBeanClassName());
        SparqlProvider providerAnnotation = sparqlProviderClass.getAnnotation(SparqlProvider.class);
        Produces produceAnnotation = sparqlProviderClass.getAnnotation(Produces.class);

        if (providerAnnotation.resultType() == ResultType.GRAPH) {
          addMediaTypes(graphMediaTypes, produceAnnotation);
          LOG.info("Registered %s provider for graph results.");
        } else if (providerAnnotation.resultType() == ResultType.TUPLE) {
          addMediaTypes(tupleMediaTypes, produceAnnotation);
          LOG.info("Registered %s provider for tuple results.");
        }

      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }

    }
  }

  private void addMediaTypes(List<MediaType> graphMediaTypes, Produces produceAnnotation) {
    for (String mediaType : produceAnnotation.value()) {
      graphMediaTypes.add(MediaType.valueOf(mediaType));
    }
  }

  public MediaType[] getMediaTypes(ResultType type) {
    switch (type) {
      case GRAPH:
        return graphMediaTypes.stream().toArray(MediaType[]::new);
      case TUPLE:
        return tupleMediaTypes.stream().toArray(MediaType[]::new);
      default:
        throw new NotSupportedException(
            String.format("ResultType %s has no supported media types", type));
    }
  }
}
