package org.dotwebstack.framework.ext.rml.mapping;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.formatMessage;
import static org.dotwebstack.framework.service.openapi.exception.OpenApiExceptionHelper.notFoundException;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.getObjectField;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.carml.engine.rdf.RdfRmlMapper;
import io.carml.model.TriplesMap;
import io.swagger.v3.oas.models.Operation;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.datafetchers.paging.PagingConstants;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.mapping.MapperUtils;
import org.dotwebstack.framework.service.openapi.response.BodyMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.util.ModelCollector;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.WriterConfig;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import reactor.core.publisher.Mono;

@Slf4j
abstract class AbstractRmlBodyMapper implements BodyMapper {

  final ObjectMapper objectMapper;

  final GraphQLSchema graphQlSchema;

  final RdfRmlMapper rmlMapper;

  final Map<Operation, Set<TriplesMap>> mappingsPerOperation;

  final Set<Namespace> namespaces;

  AbstractRmlBodyMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder, GraphQL graphQL,
      RdfRmlMapper rmlMapper, Map<Operation, Set<TriplesMap>> mappingsPerOperation, Set<Namespace> namespaces) {
    this.objectMapper = jackson2ObjectMapperBuilder.build();
    this.graphQlSchema = graphQL.getGraphQLSchema();
    this.rmlMapper = rmlMapper;
    this.mappingsPerOperation = mappingsPerOperation;
    this.namespaces = namespaces;
  }

  abstract MediaType supportedMediaType();

  abstract RDFFormat rdfFormat();

  WriterConfig getWriterConfig() {
    return new WriterConfig();
  }

  @Override
  public boolean supports(MediaType mediaType, OperationContext operationContext) {
    var schema = operationContext.getResponse()
        .getContent()
        .get(mediaType.toString())
        .getSchema();

    return schema == null && mediaType.equals(supportedMediaType());
  }

  @Override
  public Mono<Object> map(OperationRequest operationRequest, Object result) {
    var operationContext = operationRequest.getContext();
    var operation = operationContext.getOperation();

    var data = objectMapper.valueToTree(result);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Mapping response data:");
      try {
        LOG.debug("{}", objectMapper.writerWithDefaultPrettyPrinter()
            .writeValueAsString(data));
      } catch (JsonProcessingException jsonProcessingException) {
        throw new RmlBodyMapperException(formatMessage("Error processing: {}", data), jsonProcessingException);
      }
    }

    if (isPaged(operationContext)) {
      var nodes = data.get(PagingConstants.NODES_FIELD_NAME);

      if (!(nodes instanceof ArrayNode)) {
        throw new InvalidConfigurationException("Expected pageable field is not pageable.");
      }

      if (nodes.isEmpty()) {
        return Mono.error(notFoundException("Did not find data for your response."));
      }
    }

    return rmlMapper.mapRecord(data, JsonNode.class, mappingsPerOperation.get(operation))
        .collect(ModelCollector.toModel())
        .map(model -> {
          namespaces.forEach(model::setNamespace);
          return modelToString(model);
        });
  }

  String modelToString(Model model) {
    StringWriter stringWriter = new StringWriter();
    Rio.write(model, stringWriter, rdfFormat(), getWriterConfig());

    return stringWriter.toString();
  }

  private boolean isPaged(OperationContext operationContext) {
    var queryType = graphQlSchema.getQueryType();
    var fieldDefinition = getObjectField(queryType, operationContext.getQueryProperties()
        .getField());

    return MapperUtils.isPageableField(fieldDefinition);
  }
}
