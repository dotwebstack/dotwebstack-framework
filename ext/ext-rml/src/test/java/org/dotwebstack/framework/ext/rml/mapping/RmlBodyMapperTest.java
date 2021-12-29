package org.dotwebstack.framework.ext.rml.mapping;

import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.NODES_FIELD_NAME;
import static org.dotwebstack.framework.ext.rml.mapping.TurtleRmlBodyMapper.TURTLE_MEDIA_TYPE;
import static org.dotwebstack.framework.service.openapi.mapping.MapperUtils.getHandleableResponseEntry;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taxonic.carml.engine.rdf.RdfRmlMapper;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.model.impl.CarmlTriplesMap;
import io.swagger.v3.oas.models.Operation;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.dotwebstack.framework.service.openapi.handler.OperationContext;
import org.dotwebstack.framework.service.openapi.handler.OperationRequest;
import org.dotwebstack.framework.service.openapi.query.QueryProperties;
import org.dotwebstack.framework.service.openapi.response.BodyMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RmlBodyMapperTest {

  private static final Operation OPERATION = TestResources.openApi("config/openapi.yaml")
      .getPaths()
      .get("/path-complete")
      .getGet();

  private static final OperationRequest OPERATION_REQUEST = OperationRequest.builder()
      .context(OperationContext.builder()
          .operation(OPERATION)
          .queryProperties(QueryProperties.fromOperation(OPERATION))
          .responseEntry(getHandleableResponseEntry(OPERATION))
          .build())
      .preferredMediaType(TURTLE_MEDIA_TYPE)
      .serverRequest(mock(ServerRequest.class))
      .build();

  @ParameterizedTest
  @MethodSource("createBodyMappersWithOutputMimeType")
  void supports_returnsBoolean_forMediaType(AbstractRmlBodyMapper bodyMapper, MediaType outputMediaType) {
    var mediaType = bodyMapper.supports(outputMediaType, OPERATION_REQUEST.getContext());

    assertThat(mediaType, is(true));
  }

  private static Stream<Arguments> createBodyMappersWithOutputMimeType() {
    var graphQl = TestResources.graphQl();

    return Stream.of(
        Arguments.of(new Notation3RmlBodyMapper(graphQl, null, Map.of(), Set.of()),
            Notation3RmlBodyMapper.N3_MEDIA_TYPE),
        Arguments.of(new TurtleRmlBodyMapper(graphQl, null, Map.of(), Set.of()), TURTLE_MEDIA_TYPE),
        Arguments.of(new RdfXmlRmlBodyMapper(graphQl, null, Map.of(), Set.of()),
            RdfXmlRmlBodyMapper.RDF_XML_MEDIA_TYPE),
        Arguments.of(new JsonLdRmlBodyMapper(graphQl, null, Map.of(), Set.of()),
            JsonLdRmlBodyMapper.JSON_LD_MEDIA_TYPE),
        Arguments.of(new TrigRmlBodyMapper(graphQl, null, Map.of(), Set.of()), TrigRmlBodyMapper.TRIG_MEDIA_TYPE),
        Arguments.of(new NQuadsRmlBodyMapper(graphQl, null, Map.of(), Set.of()),
            NQuadsRmlBodyMapper.N_QUADS_MEDIA_TYPE),
        Arguments.of(new NTriplesRmlBodyMapper(graphQl, null, Map.of(), Set.of()),
            NTriplesRmlBodyMapper.N_TRIPLES_MEDIA_TYPE));
  }

  @ParameterizedTest
  @MethodSource("createBodyMappersWithExpectedResultFileName")
  void map_withUnPagedResult_returnsCorrectResultForModel(BodyMapper bodyMapper, String expectedResultFileName)
      throws IOException {
    String actualResult = bodyMapper.map(OPERATION_REQUEST, Map.of(NODES_FIELD_NAME, List.of("foo", Map.of())))
        .block()
        .toString();

    String expectedResult = new String(getFileInputStream(expectedResultFileName).readAllBytes());

    assertThat(actualResult, equalToCompressingWhiteSpace(expectedResult));
  }

  private static Stream<Arguments> createBodyMappersWithExpectedResultFileName() throws Exception {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    Model model = Rio.parse(getFileInputStream("beer.trig"), "", RDFFormat.TRIG);
    when(rdfRmlMapper.mapItem(any(), any())).thenReturn(Flux.fromIterable(model.getStatements(null, null, null)));
    Map<Operation, Set<TriplesMap>> mappingsPerOperation = Map.of(OPERATION, Set.of());
    Set<Namespace> namespaces = Set.of(new SimpleNamespace("beer", "http://dotwebstack.org/def/beer#"));
    var graphQl = TestResources.graphQl();

    return Stream.of(
        Arguments.of(new Notation3RmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.n3"),
        Arguments.of(new TurtleRmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.ttl"),
        Arguments.of(new RdfXmlRmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.xml"),
        Arguments.of(new JsonLdRmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.ld.json"),
        Arguments.of(new TrigRmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.trig"),
        Arguments.of(new NQuadsRmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.nq"),
        Arguments.of(new NTriplesRmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.nt"));
  }

  private static InputStream getFileInputStream(String filename) throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("files")
        .resolve(filename));
  }

  @Test
  void map_selectsCorrectMapping_forOperationRequest() throws IOException {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    Model model = Rio.parse(getFileInputStream("beer.trig"), "", RDFFormat.TRIG);
    when(rdfRmlMapper.mapItem(any(), any())).thenReturn(Flux.fromIterable(model.getStatements(null, null, null)));
    Operation otherOperation = new Operation();
    otherOperation.operationId("other");
    var graphQl = TestResources.graphQl();

    TriplesMap triplesMap = CarmlTriplesMap.builder()
        .id("test")
        .build();
    TriplesMap otherTriplesMap = CarmlTriplesMap.builder()
        .id("other")
        .build();

    Map<Operation, Set<TriplesMap>> mappingsPerOperation =
        Map.of(OPERATION, Set.of(triplesMap), otherOperation, Set.of(otherTriplesMap));

    BodyMapper bodyMapper = new Notation3RmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, Set.of());

    bodyMapper.map(OPERATION_REQUEST, Map.of(NODES_FIELD_NAME, List.of("foo", Map.of())))
        .block();

    verify(rdfRmlMapper, times(1)).mapItem(Map.of(NODES_FIELD_NAME, List.of("foo", Map.of())), Set.of(triplesMap));
    verify(rdfRmlMapper, times(0)).mapItem(Map.of(NODES_FIELD_NAME, List.of("foo", Map.of())), Set.of(otherTriplesMap));
  }

  @Test
  void map_appliesNamespacesToModel_givenNamespaces() {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    when(rdfRmlMapper.mapItem(any(), any())).thenReturn(Flux.empty());
    Map<Operation, Set<TriplesMap>> mappingsPerOperation = Map.of(OPERATION, Set.of());
    Set<Namespace> namespaces = Set.of(RDFS.NS, OWL.NS);
    var graphQl = TestResources.graphQl();
    BodyMapper bodyMapper = new Notation3RmlBodyMapper(graphQl, rdfRmlMapper, mappingsPerOperation, namespaces);

    var response = bodyMapper.map(OPERATION_REQUEST, Map.of(NODES_FIELD_NAME, List.of("foo", Map.of())))
        .block();

    assertThat(response.toString(), containsString("@prefix owl: <http://www.w3.org/2002/07/owl#>"));
    assertThat(response.toString(), containsString("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"));
  }

  @Test
  void map_throwsException_forUnsupportedInputObject() {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    var graphQl = TestResources.graphQl();
    BodyMapper bodyMapper = new Notation3RmlBodyMapper(graphQl, rdfRmlMapper, Map.of(), Set.of());
    Object input = Set.of();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> bodyMapper.map(OPERATION_REQUEST, input));

    assertThat(exception.getMessage(), startsWith("Input can only be of type Map, but was"));
  }

  @Test
  void map_returnsErrorMono_forEmptyPagedResult() {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    var graphQl = TestResources.graphQl();
    BodyMapper bodyMapper = new Notation3RmlBodyMapper(graphQl, rdfRmlMapper, Map.of(), Set.of());
    Object input = Map.of(NODES_FIELD_NAME, List.of());

    Mono<Object> response = bodyMapper.map(OPERATION_REQUEST, input);

    StepVerifier.create(response)
        .expectErrorMessage("Did not find data for your response.")
        .verify();
  }
}
