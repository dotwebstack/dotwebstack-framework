package org.dotwebstack.framework.frontend.ld;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupportedWriterMediaTypesScanner {

  private static final Logger LOG = LoggerFactory.getLogger(SupportedWriterMediaTypesScanner.class);

  private List<MediaType> graphMediaTypes = new ArrayList<>();

  private List<MediaType> tupleMediaTypes = new ArrayList<>();

  private List<MessageBodyWriter<GraphEntity>> graphEntityWriters = new ArrayList<>();

  private List<MessageBodyWriter<TupleEntity>> tupleEntityWriters = new ArrayList<>();

  @Autowired
  public SupportedWriterMediaTypesScanner(
      @NonNull List<MessageBodyWriter<GraphEntity>> graphEntityWriters,
      @NonNull List<MessageBodyWriter<TupleEntity>> tupleEntityWriters) {
    loadSupportedMediaTypes(graphEntityWriters, graphMediaTypes, this.graphEntityWriters);
    loadSupportedMediaTypes(tupleEntityWriters, tupleMediaTypes, this.tupleEntityWriters);
  }

  public Collection<MediaType> getAllSupportedMediaTypes() {
    return Stream.concat(graphMediaTypes.stream(), tupleMediaTypes.stream()).collect(
        Collectors.toList());
  }

  private <T> void loadSupportedMediaTypes(List<MessageBodyWriter<T>> entityWriters,
      List<MediaType> list, List<MessageBodyWriter<T>> resultingList) {

    entityWriters.forEach(writer -> {
      Class<?> entityWriterClass = writer.getClass();
      EntityWriter providerAnnotation = entityWriterClass.getAnnotation(EntityWriter.class);
      Produces produceAnnotation = entityWriterClass.getAnnotation(Produces.class);

      if (providerAnnotation == null) {
        LOG.warn(
            String.format("Found writer that did not specify the EntityWriter annotation correctly."
                + " Skipping %s", writer.getClass()));
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
      LOG.info(String.format("Registered %s writer for results.", writer.getClass()));
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
        throw new IllegalArgumentException(
            String.format("ResultType %s has no supported media types", type));
    }
  }

  List<MessageBodyWriter<GraphEntity>> getGraphEntityWriters() {
    return ImmutableList.copyOf(graphEntityWriters);
  }

  List<MessageBodyWriter<TupleEntity>> getTupleEntityWriters() {
    return ImmutableList.copyOf(tupleEntityWriters);
  }

}
