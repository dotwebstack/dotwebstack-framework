package org.dotwebstack.framework.backend.rdf4j.mapping;

import static org.dotwebstack.framework.backend.rdf4j.matcher.IsEqualIgnoringLineBreaks.equalToIgnoringLineBreaks;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.mapping.ResponseMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.ParserConfig;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.ParseErrorLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.util.MimeType;

public class ResponseMapperTest {

  @ParameterizedTest
  @MethodSource("createResponseMappersWithInputObjectClass")
  void shouldSupportsInputObjectClass(ResponseMapper<Model> responseMapper, Class<?> inputObjectClass) {
    boolean actualResult = responseMapper.supportsInputObjectClass(inputObjectClass);

    assertThat(actualResult, is(true));
  }

  private static Stream<Arguments> createResponseMappersWithInputObjectClass() {
    return Stream.of(Arguments.of(new Notation3ResponseMapper(), Model.class),
        Arguments.of(new TurtleResponseMapper(), Model.class), Arguments.of(new RdfXmlResponseMapper(), Model.class),
        Arguments.of(new JsonLdResponseMapper(), Model.class), Arguments.of(new TrigResponseMapper(), Model.class),
        Arguments.of(new NQuadsResponseMapper(), Model.class));
  }

  @ParameterizedTest
  @MethodSource("createResponseMappersWithOutputMimeType")
  void shouldSupportsOutputMimeType(ResponseMapper<Model> responseMapper, MimeType outputMimeType) {
    boolean actualResult = responseMapper.supportsOutputMimeType(outputMimeType);

    assertThat(actualResult, is(true));
  }

  private static Stream<Arguments> createResponseMappersWithOutputMimeType() {
    return Stream.of(Arguments.of(new Notation3ResponseMapper(), Notation3ResponseMapper.N3_MEDIA_TYPE),
        Arguments.of(new TurtleResponseMapper(), TurtleResponseMapper.TURTLE_MEDIA_TYPE),
        Arguments.of(new RdfXmlResponseMapper(), RdfXmlResponseMapper.RDF_XML_MEDIA_TYPE),
        Arguments.of(new JsonLdResponseMapper(), JsonLdResponseMapper.JSON_LD_MEDIA_TYPE),
        Arguments.of(new TrigResponseMapper(), TrigResponseMapper.TRIG_MEDIA_TYPE),
        Arguments.of(new NQuadsResponseMapper(), NQuadsResponseMapper.N_QUADS_MEDIA_TYPE));
  }

  @ParameterizedTest
  @MethodSource("createResponseMappersWithExpectedResultFileName")
  void shouldMapModelTo(ResponseMapper<Model> responseMapper, String expectedResultFileName) throws IOException {
    InputStream is = getFileInputStream("input-response-mapper.trig");
    Model model =
        Rio.parse(is, "", RDFFormat.TRIG, new ParserConfig(), SimpleValueFactory.getInstance(), new ParseErrorLogger());

    String actualResult = responseMapper.toResponse(model);
    String expectedResult = new String(getFileInputStream(expectedResultFileName).readAllBytes());

    assertThat(actualResult, equalToIgnoringLineBreaks(expectedResult));
  }

  private static Stream<Arguments> createResponseMappersWithExpectedResultFileName() {
    return Stream.of(Arguments.of(new Notation3ResponseMapper(), "output-response-mapper-n3.txt"),
        Arguments.of(new TurtleResponseMapper(), "output-response-mapper-turtle.txt"),
        Arguments.of(new RdfXmlResponseMapper(), "output-response-mapper-rdfxml.txt"),
        Arguments.of(new JsonLdResponseMapper(), "output-response-mapper-jsonld.txt"),
        Arguments.of(new TrigResponseMapper(), "output-response-mapper-trig.txt"),
        Arguments.of(new NQuadsResponseMapper(), "output-response-mapper-nquads.txt"));
  }

  private InputStream getFileInputStream(String filename) throws IOException {
    return Files.newInputStream(Paths.get("src", "test", "resources")
        .resolve("test-files")
        .resolve(filename));
  }
}
