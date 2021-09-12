package org.dotwebstack.framework.ext.rml.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.formatMessage;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.Mapping;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
abstract class AbstractRmlResponseMapper implements ResponseMapper {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  final RdfRmlMapper rmlMapper;

  final Map<HttpMethodOperation, Set<TriplesMap>> actionableMappingsPerOperation;

  final Set<Namespace> namespaces;

  AbstractRmlResponseMapper(RdfRmlMapper rmlMapper, Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation,
      Set<Namespace> namespaces) {
    this.rmlMapper = rmlMapper;
    this.actionableMappingsPerOperation = mappingsPerOperation.entrySet()
        .stream()
        .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, entry -> Mapping.filterMappable(entry.getValue())));
    this.namespaces = namespaces;
  }

  abstract MimeType supportedMimeType();

  abstract RDFFormat rdfFormat();

  WriterConfig getWriterConfig() {
    return new WriterConfig();
  }

  @Override
  public boolean supportsOutputMimeType(@NonNull MimeType mimeType) {
    return supportedMimeType().equals(mimeType);
  }

  @Override
  public boolean supportsInputObjectClass(@NonNull Class<?> clazz) {
    return Map.class.isAssignableFrom(clazz);
  }

  @Override
  public Mono<String> toResponse(@NonNull Object input, Object context) {
    if (input instanceof Map) {
      HttpMethodOperation operation;
      if (context instanceof HttpMethodOperation) {
        operation = (HttpMethodOperation) context;
      } else {
        throw illegalArgumentException("Context can only be of type HttpMethodOperation, but was {}", context.getClass()
            .getCanonicalName());
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("Mapping response data:");
        try {
          LOG.debug("{}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
              .writeValueAsString(input));
        } catch (JsonProcessingException jsonProcessingException) {
          throw new RmlResponseMapperException(formatMessage("Error processing: {}", input), jsonProcessingException);
        }
      }

      // TODO: CARML should return a Mono directly
      Mono<Model> response =
          Mono.fromCallable(() -> rmlMapper.mapItemToRdf4jModel(input, actionableMappingsPerOperation.get(operation)))
              .publishOn(Schedulers.boundedElastic());

      return response.map(model -> {
        namespaces.forEach(model::setNamespace);
        return modelToString(model);
      });
    } else {
      throw illegalArgumentException("Input can only be of type Map, but was {}", input.getClass()
          .getCanonicalName());
    }
  }

  String modelToString(Model model) {
    StringWriter stringWriter = new StringWriter();
    Rio.write(model, stringWriter, rdfFormat(), getWriterConfig());

    return stringWriter.toString();
  }
}
