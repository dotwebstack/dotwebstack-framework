package org.dotwebstack.framework.ext.rml.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.formatMessage;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.model.TriplesMap;
import io.swagger.v3.oas.models.Operation;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.response.BodyMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.util.ModelCollector;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

@Slf4j
abstract class AbstractRmlBodyMapper implements BodyMapper {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  final RdfRmlMapper rmlMapper;

  final Map<Operation, Set<TriplesMap>> actionableMappingsPerOperation;

  final Set<Namespace> namespaces;

  AbstractRmlBodyMapper(RdfRmlMapper rmlMapper, Map<Operation, Set<TriplesMap>> mappingsPerOperation,
      Set<Namespace> namespaces) {
    this.rmlMapper = rmlMapper;
    this.actionableMappingsPerOperation = mappingsPerOperation;
    this.namespaces = namespaces;
  }

  abstract MediaType supportedMediaType();

  abstract RDFFormat rdfFormat();

  WriterConfig getWriterConfig() {
    return new WriterConfig();
  }

  @Override
  public boolean supports(MediaType mediaType, OperationContext operationContext) {
    var schema = operationContext.getSuccessResponse()
        .getContent()
        .get(mediaType.toString())
        .getSchema();

    return schema == null && mediaType.equals(supportedMediaType());
  }

  @Override
  public Mono<Object> map(OperationRequest operationRequest, Object result) {
    var operation = operationRequest.getContext()
        .getOperation();

    if (result instanceof Map) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Mapping response data:");
        try {
          LOG.debug("{}", OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
              .writeValueAsString(result));
        } catch (JsonProcessingException jsonProcessingException) {
          throw new RmlBodyMapperException(formatMessage("Error processing: {}", result), jsonProcessingException);
        }
      }

      return rmlMapper.mapItem(result, actionableMappingsPerOperation.get(operation))
          .collect(ModelCollector.toModel())
          .map(model -> {
            namespaces.forEach(model::setNamespace);
            return modelToString(model);
          });
    } else {
      throw illegalArgumentException("Input can only be of type Map, but was {}", result.getClass()
          .getCanonicalName());
    }
  }

  String modelToString(Model model) {
    StringWriter stringWriter = new StringWriter();
    Rio.write(model, stringWriter, rdfFormat(), getWriterConfig());

    return stringWriter.toString();
  }
}
