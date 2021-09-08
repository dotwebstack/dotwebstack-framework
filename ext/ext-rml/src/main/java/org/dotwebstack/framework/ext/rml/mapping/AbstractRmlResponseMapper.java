package org.dotwebstack.framework.ext.rml.mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.model.TriplesMap;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
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

@Slf4j
abstract class AbstractRmlResponseMapper implements ResponseMapper {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  final RdfRmlMapper rmlMapper;

  final Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation;

  final Set<Namespace> namespaces;

  AbstractRmlResponseMapper(RdfRmlMapper rmlMapper, Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation,
      Set<Namespace> namespaces) {
    this.rmlMapper = rmlMapper;
    this.mappingsPerOperation = mappingsPerOperation;
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
  public String toResponse(@NonNull Object input, Object context) {
    if (input instanceof Map) {
      HttpMethodOperation operation;
      if (context instanceof HttpMethodOperation) {
        operation = (HttpMethodOperation) context;
      } else {
        throw new IllegalArgumentException(
            String.format("Context can only be of type HttpMethodOperation, but was %s", context.getClass()
                .getCanonicalName()));
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("Mapping response data:");
        try {
          LOG.debug("{}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
              .writeValueAsString(input));
        } catch (JsonProcessingException jsonProcessingException) {
          throw new RmlResponseMapperException(String.format("Error processing: %s", input), jsonProcessingException);
        }
      }

      Model model = rmlMapper.mapItemToRdf4jModel(input, mappingsPerOperation.get(operation));
      namespaces.forEach(model::setNamespace);

      return modelToString(model);
    } else {
      throw new IllegalArgumentException(String.format("Input can only be of type Map, but was %s", input.getClass()
          .getCanonicalName()));
    }
  }

  String modelToString(Model model) {
    StringWriter stringWriter = new StringWriter();
    Rio.write(model, stringWriter, rdfFormat(), getWriterConfig());

    return stringWriter.toString();
  }
}
