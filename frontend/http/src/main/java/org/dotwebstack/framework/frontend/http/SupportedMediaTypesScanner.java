package org.dotwebstack.framework.frontend.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.provider.SparqlProvider;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportedMediaTypesScanner {

  private static final Logger LOG = LoggerFactory.getLogger(SupportedMediaTypesScanner.class);

  private List<MediaType> graphMediaTypes = new ArrayList<>();

  private List<MediaType> tupleMediaTypes = new ArrayList<>();

  private List<MessageBodyWriter<GraphQueryResult>> graphQueryWriters = new ArrayList<>();

  private List<MessageBodyWriter<TupleQueryResult>> tupleQueryWriters = new ArrayList<>();

  @Autowired
  public SupportedMediaTypesScanner(List<MessageBodyWriter<GraphQueryResult>> graphQueryWriters,
      List<MessageBodyWriter<TupleQueryResult>> tupleQueryWriters) {
    loadSupportedMediaTypes(Objects.requireNonNull(graphQueryWriters), graphMediaTypes,
        this.graphQueryWriters);
    loadSupportedMediaTypes(Objects.requireNonNull(tupleQueryWriters), tupleMediaTypes,
        this.tupleQueryWriters);
  }

  private <T> void loadSupportedMediaTypes(List<MessageBodyWriter<T>> sparqlProviders,
      List<MediaType> list, List<MessageBodyWriter<T>> resultingList) {

    sparqlProviders.forEach(writer -> {
      Class<?> sparqlProviderClass = writer.getClass();
      SparqlProvider providerAnnotation = sparqlProviderClass.getAnnotation(SparqlProvider.class);
      Produces produceAnnotation = sparqlProviderClass.getAnnotation(Produces.class);

      if (providerAnnotation == null) {
        LOG.warn(String.format(
            "Found writer that did not specify the SparqlProvider annotation correctly."
                + " Skipping %s",
            writer.getClass()));
        return;
      }

      if (produceAnnotation == null) {
        LOG.warn(String.format(
            "Found writer that did not specify the Produce annotation correctly. Skipping %s",
            writer.getClass()));
        return;
      }

      addMediaTypes(list, produceAnnotation);
      resultingList.add(writer);
      LOG.info("Registered %s provider for results.");
    });
  }

  private void addMediaTypes(List<MediaType> graphMediaTypes, Produces produceAnnotation) {
    for (String mediaType : produceAnnotation.value()) {
      graphMediaTypes.add(MediaType.valueOf(mediaType));
    }
  }

  public MediaType[] getMediaTypes(ResultType type) {
    switch (type) {
      case GRAPH:
        return graphMediaTypes.toArray(new MediaType[0]);
      case TUPLE:
        return tupleMediaTypes.toArray(new MediaType[0]);
      default:
        throw new NotSupportedException(
            String.format("ResultType %s has no supported media types", type));
    }
  }

  public List<MessageBodyWriter<?>> getSparqlProviders() {
    return Stream.concat(graphQueryWriters.stream(), tupleQueryWriters.stream()).collect(
        Collectors.toList());
  }

}
