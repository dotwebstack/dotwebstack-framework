package org.dotwebstack.framework.frontend.ld;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.http.MediaTypes;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SupportedWriterMediaTypesScanner {

  private List<MediaType> graphMediaTypes;

  private List<MediaType> tupleMediaTypes;

  private List<MediaType> htmlMediaTypes = Collections.singletonList(MediaTypes.TEXT_HTML_TYPE);

  private List<MessageBodyWriter<GraphEntity>> graphEntityWriters;

  private List<MessageBodyWriter<TupleEntity>> tupleEntityWriters;


  @Autowired
  public SupportedWriterMediaTypesScanner(
      @NonNull List<MessageBodyWriter<GraphEntity>> graphEntityWriters,
      @NonNull List<MessageBodyWriter<TupleEntity>> tupleEntityWriters) {
    this.graphEntityWriters = registerSupportedWriters(graphEntityWriters);
    this.tupleEntityWriters = registerSupportedWriters(tupleEntityWriters);
    this.graphMediaTypes = loadMediatypes(graphEntityWriters);
    this.tupleMediaTypes = loadMediatypes(tupleEntityWriters);
  }

  private <T> List<MessageBodyWriter<T>> registerSupportedWriters(
      List<MessageBodyWriter<T>> entityWriters) { //
    return entityWriters.stream() //
        .filter(this::validateAnnotations) //
        .peek(writer -> LOG.info("Registering {} writer for mediatypes.", writer.getClass())) //
        .collect(Collectors.toList());
  }

  private <T> boolean validateAnnotations(MessageBodyWriter<T> writer) {
    if (!writer.getClass().isAnnotationPresent(EntityWriter.class)) {
      LOG.warn("Found writer that did not specify the {} annotation correctly. Skipping {}",
          EntityWriter.class, writer.getClass());
      return false;
    }

    if (!writer.getClass().isAnnotationPresent(Produces.class)) {
      LOG.warn("Found writer that did not specify the {} annotation correctly. Skipping {}",
          Produces.class, writer.getClass());
      return false;
    }
    return true;
  }

  private <T> List<MediaType> loadMediatypes(List<MessageBodyWriter<T>> entityWriters) {
    return entityWriters.stream()//
        .filter(this::validateAnnotations)//
        .map(writer -> writer.getClass().getAnnotation(Produces.class).value())//
        .flatMap(Arrays::stream)//
        .map(MediaType::valueOf)//
        .collect(Collectors.toList());
  }

  public Collection<MediaType> getAllSupportedMediaTypes() {
    return Stream.of(graphMediaTypes, tupleMediaTypes, htmlMediaTypes)//
        .flatMap(Collection::stream)//
        .collect(Collectors.toList());
  }

  public MediaType[] getMediaTypes(ResultType type) {
    switch (type) {
      case GRAPH:
        return Stream.of(graphMediaTypes, htmlMediaTypes)//
            .flatMap(Collection::stream)//
            .toArray(MediaType[]::new);
      case TUPLE:
        return Stream.of(tupleMediaTypes, htmlMediaTypes)//
            .flatMap(Collection::stream)//
            .toArray(MediaType[]::new);
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
