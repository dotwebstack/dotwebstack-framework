package org.dotwebstack.framework.backend.rdf4j.mapping;

import static org.dotwebstack.framework.backend.rdf4j.matcher.IsEqualIgnoringLineBreaks.equalToIgnoringLineBreaks;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.rdf4j.model.SparqlQueryResult;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.util.MimeType;

public class SparqlResponseMapperTest {

  @ParameterizedTest
  @MethodSource("createResponseMappersWithInputObjectClass")
  void responseMapper_returnsBoolean_forClass(ResponseMapper responseMapper, Class<?> inputObjectClass) {
    // Act
    boolean actualResult = responseMapper.supportsInputObjectClass(inputObjectClass);

    // Assert
    assertThat(actualResult, is(true));
  }

  private static Stream<Arguments> createResponseMappersWithInputObjectClass() {
    // Arrange
    return Stream.of(Arguments.of(new SparqlJsonResultResponseMapper(), SparqlQueryResult.class),
        Arguments.of(new SparqlXmlResultResponseMapper(), SparqlQueryResult.class));
  }

  @ParameterizedTest
  @MethodSource("createResponseMappersWithOutputMimeType")
  void responseMapper_returnsBoolean_forMimeType(ResponseMapper responseMapper, MimeType outputMimeType) {
    // Act
    boolean actualResult = responseMapper.supportsOutputMimeType(outputMimeType);

    // Assert
    assertThat(actualResult, is(true));
  }

  private static Stream<Arguments> createResponseMappersWithOutputMimeType() {
    // Arrange
    return Stream.of(
        Arguments.of(new SparqlJsonResultResponseMapper(),
            SparqlJsonResultResponseMapper.SPARQL_RESULT_JSON_MEDIA_TYPE),
        Arguments.of(new SparqlXmlResultResponseMapper(), SparqlXmlResultResponseMapper.SPARQL_RESULT_XML_MEDIA_TYPE));
  }

  @ParameterizedTest
  @MethodSource("createResponseMappersWithExpectedResultFileName")
  void responseMapper_returnsString_forSparql(ResponseMapper responseMapper, String expectedResultFileName)
      throws IOException {
    // Arrange
    InputStream is = getFileInputStream("input-response-mapper.sparql");
    SparqlQueryResult sparqlQueryResult = new SparqlQueryResult(is);

    // Act
    String actualResult = responseMapper.toResponse(sparqlQueryResult);
    String expectedResult = new String(getFileInputStream(expectedResultFileName).readAllBytes());

    // Assert
    assertThat(actualResult, equalToIgnoringLineBreaks(expectedResult));
  }

  private static Stream<Arguments> createResponseMappersWithExpectedResultFileName() {
    // Arrange
    return Stream.of(Arguments.of(new SparqlJsonResultResponseMapper(), "output-response-mapper-sparqlresultjson.txt"),
        Arguments.of(new SparqlXmlResultResponseMapper(), "output-response-mapper-sparqlresultxml.txt"));
  }

  @ParameterizedTest
  @MethodSource("createResponseMappers")
  void responseMapper_throwsIllegalArgumentException_forModel(ResponseMapper responseMapper) {
    // Arrange
    Model model = new DynamicModelFactory().createEmptyModel();

    // Act/Assert
    assertThrows(IllegalArgumentException.class, () -> responseMapper.toResponse(model));
  }

  @ParameterizedTest
  @MethodSource("createResponseMappers")
  void responseMapper_throwsResponseMapperException_forInputStreamIsThrowingIOException(ResponseMapper responseMapper) {
    // Arrange
    InputStream is = mock(InputStream.class, new IOExceptionAnswer());
    SparqlQueryResult sparqlQueryResult = new SparqlQueryResult(is);

    // Act/Assert
    assertThrows(ResponseMapperException.class, () -> responseMapper.toResponse(sparqlQueryResult));
  }

  private static Stream<Arguments> createResponseMappers() {
    // Arrange
    return Stream.of(Arguments.of(new SparqlJsonResultResponseMapper()),
        Arguments.of(new SparqlXmlResultResponseMapper()));
  }

  private InputStream getFileInputStream(String filename) throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("test-files")
        .resolve(filename));
  }

  private static class IOExceptionAnswer implements Answer<Object> {

    public Object answer(InvocationOnMock invocation) throws Throwable {
      throw new IOException();
    }

  }
}
