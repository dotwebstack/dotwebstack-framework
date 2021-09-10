package org.dotwebstack.framework.ext.rml.mapping;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.dotwebstack.framework.service.openapi.HttpMethodOperation;
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
import org.springframework.util.MimeType;

@ExtendWith(MockitoExtension.class)
class RmlResponseMapperTest {

  private static final HttpMethodOperation OPERATION = HttpMethodOperation.builder()
      .name("test")
      .build();

  @ParameterizedTest
  @MethodSource("createResponseMappersWithInputObjectClass")
  void responseMapper_returnsBoolean_forInputObjectClass(ResponseMapper responseMapper, Class<?> inputObjectClass) {
    boolean actualResult = responseMapper.supportsInputObjectClass(inputObjectClass);

    assertThat(actualResult, is(true));
  }

  private static Stream<Arguments> createResponseMappersWithInputObjectClass() {
    return Stream.of(Arguments.of(new Notation3RmlResponseMapper(null, Map.of(), Set.of()), Map.class),
        Arguments.of(new TurtleRmlResponseMapper(null, Map.of(), Set.of()), Map.class),
        Arguments.of(new RdfXmlRmlResponseMapper(null, Map.of(), Set.of()), Map.class),
        Arguments.of(new JsonLdRmlResponseMapper(null, Map.of(), Set.of()), Map.class),
        Arguments.of(new TrigRmlResponseMapper(null, Map.of(), Set.of()), Map.class),
        Arguments.of(new NQuadsRmlResponseMapper(null, Map.of(), Set.of()), Map.class),
        Arguments.of(new NTriplesRmlResponseMapper(null, Map.of(), Set.of()), Map.class));
  }

  @ParameterizedTest
  @MethodSource("createResponseMappersWithOutputMimeType")
  void responseMapper_returnsBoolean_forMimeType(ResponseMapper responseMapper, MimeType outputMimeType) {
    boolean actualResult = responseMapper.supportsOutputMimeType(outputMimeType);

    assertThat(actualResult, is(true));
  }

  private static Stream<Arguments> createResponseMappersWithOutputMimeType() {
    return Stream.of(
        Arguments.of(new Notation3RmlResponseMapper(null, Map.of(), Set.of()),
            Notation3RmlResponseMapper.N3_MEDIA_TYPE),
        Arguments.of(new TurtleRmlResponseMapper(null, Map.of(), Set.of()), TurtleRmlResponseMapper.TURTLE_MEDIA_TYPE),
        Arguments.of(new RdfXmlRmlResponseMapper(null, Map.of(), Set.of()), RdfXmlRmlResponseMapper.RDF_XML_MEDIA_TYPE),
        Arguments.of(new JsonLdRmlResponseMapper(null, Map.of(), Set.of()), JsonLdRmlResponseMapper.JSON_LD_MEDIA_TYPE),
        Arguments.of(new TrigRmlResponseMapper(null, Map.of(), Set.of()), TrigRmlResponseMapper.TRIG_MEDIA_TYPE),
        Arguments.of(new NQuadsRmlResponseMapper(null, Map.of(), Set.of()), NQuadsRmlResponseMapper.N_QUADS_MEDIA_TYPE),
        Arguments.of(new NTriplesRmlResponseMapper(null, Map.of(), Set.of()),
            NTriplesRmlResponseMapper.N_TRIPLES_MEDIA_TYPE));
  }

  @ParameterizedTest
  @MethodSource("createResponseMappersWithExpectedResultFileName")
  void responseMapper_returnsCorrectResult_forModel(ResponseMapper responseMapper, String expectedResultFileName)
      throws IOException {
    String actualResult = responseMapper.toResponse(Map.of(), OPERATION);
    String expectedResult = new String(getFileInputStream(expectedResultFileName).readAllBytes());

    assertThat(actualResult, equalToCompressingWhiteSpace(expectedResult));
  }

  private static Stream<Arguments> createResponseMappersWithExpectedResultFileName() throws Exception {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    Model model = Rio.parse(getFileInputStream("beer.trig"), "", RDFFormat.TRIG);
    when(rdfRmlMapper.mapItemToRdf4jModel(any(), any())).thenReturn(model);
    Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation = Map.of(OPERATION, Set.of());
    Set<Namespace> namespaces = Set.of(new SimpleNamespace("beer", "http://dotwebstack.org/def/beer#"));

    return Stream.of(
        Arguments.of(new Notation3RmlResponseMapper(rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.n3"),
        Arguments.of(new TurtleRmlResponseMapper(rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.ttl"),
        Arguments.of(new RdfXmlRmlResponseMapper(rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.xml"),
        Arguments.of(new JsonLdRmlResponseMapper(rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.ld.json"),
        Arguments.of(new TrigRmlResponseMapper(rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.trig"),
        Arguments.of(new NQuadsRmlResponseMapper(rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.nq"),
        Arguments.of(new NTriplesRmlResponseMapper(rdfRmlMapper, mappingsPerOperation, namespaces),
            "output-response-mapper.nt"));
  }

  private static InputStream getFileInputStream(String filename) throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("files")
        .resolve(filename));
  }

  @Test
  void responseMapper_selectsCorrectMapping_forContext() throws IOException {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    Model model = Rio.parse(getFileInputStream("beer.trig"), "", RDFFormat.TRIG);
    when(rdfRmlMapper.mapItemToRdf4jModel(any(), any())).thenReturn(model);
    HttpMethodOperation otherOperation = HttpMethodOperation.builder()
        .name("other")
        .build();

    TriplesMap triplesMap = CarmlTriplesMap.builder()
        .id("test")
        .build();
    TriplesMap otherTriplesMap = CarmlTriplesMap.builder()
        .id("other")
        .build();

    Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation =
        Map.of(OPERATION, Set.of(triplesMap), otherOperation, Set.of(otherTriplesMap));

    ResponseMapper responseMapper = new Notation3RmlResponseMapper(rdfRmlMapper, mappingsPerOperation, Set.of());

    responseMapper.toResponse(Map.of(), OPERATION);

    verify(rdfRmlMapper, times(1)).mapItemToRdf4jModel(Map.of(), Set.of(triplesMap));
    verify(rdfRmlMapper, times(0)).mapItemToRdf4jModel(Map.of(), Set.of(otherTriplesMap));
  }

  @Test
  void responseMapper_appliesNamespacesToModel_givenNamespaces() {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    Model model = mock(Model.class);
    when(rdfRmlMapper.mapItemToRdf4jModel(any(), any())).thenReturn(model);
    when(model.iterator()).thenReturn(Collections.emptyIterator());
    Map<HttpMethodOperation, Set<TriplesMap>> mappingsPerOperation = Map.of(OPERATION, Set.of());
    Set<Namespace> namespaces = Set.of(RDFS.NS, OWL.NS);

    ResponseMapper responseMapper = new Notation3RmlResponseMapper(rdfRmlMapper, mappingsPerOperation, namespaces);

    responseMapper.toResponse(Map.of(), OPERATION);

    verify(model, times(2)).setNamespace(any());
  }

  @Test
  void responseMapper_throwsException_forUnsupportedInputObject() {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    ResponseMapper responseMapper = new Notation3RmlResponseMapper(rdfRmlMapper, Map.of(), Set.of());
    Object input = Set.of();
    Object context = Map.of();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> responseMapper.toResponse(input, context));

    assertThat(exception.getMessage(), startsWith("Input can only be of type Map, but was"));
  }

  @Test
  void responseMapper_throwsException_forUnsupportedContextObject() {
    RdfRmlMapper rdfRmlMapper = mock(RdfRmlMapper.class);
    ResponseMapper responseMapper = new Notation3RmlResponseMapper(rdfRmlMapper, Map.of(), Set.of());
    Object input = Map.of();
    Object context = Map.of();

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> responseMapper.toResponse(input, context));

    assertThat(exception.getMessage(), startsWith("Context can only be of type HttpMethodOperation, but was"));
  }

}
