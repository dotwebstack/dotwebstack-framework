package org.dotwebstack.framework.service.openapi.exception;

import static org.dotwebstack.framework.service.openapi.exception.ExceptionRuleHelper.getExceptionRule;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.util.stream.Stream;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.RequestValidationException;
import org.dotwebstack.framework.core.templating.TemplatingException;
import org.dotwebstack.framework.service.openapi.mapping.MappingException;
import org.dotwebstack.graphql.orchestrate.exception.GraphqlJavaOrchestrateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ExceptionRuleHelperTest {


  @ParameterizedTest
  @MethodSource("exceptionRules")
  void getExceptionRule_returnsRule_forException(Exception exception, HttpStatus httpStatus, String title) {

    var result = getExceptionRule(exception);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get()
        .getException(), is(exception.getClass()));
    assertThat(result.get()
        .getTitle(), is(title));
    assertThat(result.get()
        .getResponseStatus(), is(httpStatus));
  }

  private static Stream<Arguments> exceptionRules() {
    return Stream.of(
        Arguments.of(new NotAcceptableException("", new Throwable()), NOT_ACCEPTABLE,
            "Unsupported media type requested"),
        Arguments.of(new ParameterValidationException("", new Throwable()), BAD_REQUEST,
            "Error while obtaining request parameters"),
        Arguments.of(new MappingException("", new Throwable()), INTERNAL_SERVER_ERROR, "Internal server error"),
        Arguments.of(new GraphQlErrorException("", new Throwable()), INTERNAL_SERVER_ERROR, "Internal server error"),
        Arguments.of(new NoContentException("", new Throwable()), NO_CONTENT, "No content"),
        Arguments.of(new NotFoundException("", new Throwable()), NOT_FOUND, "No results found"),
        Arguments.of(new UnsupportedOperationException("", new Throwable()), UNSUPPORTED_MEDIA_TYPE, "Not supported"),
        Arguments.of(new BadRequestException("", new Throwable()), BAD_REQUEST, "Error while processing the request"),
        Arguments.of(new RequestValidationException("", new Throwable()), BAD_REQUEST,
            "Error while processing the request"),
        Arguments.of(new InvalidConfigurationException("", new Throwable()), INTERNAL_SERVER_ERROR,
            "Bad configuration"),
        Arguments.of(new TemplatingException("", new Throwable()), INTERNAL_SERVER_ERROR, "Templating went wrong"));
  }

  @Test
  void getExceptionRule_returnsEmpty_forNonListedException() {
    var result = getExceptionRule(new IllegalStateException());

    assertThat(result.isEmpty(), is(true));
  }

  @Test
  void getExceptionRule_returnsInternalServerErrorRule_forGraphlqlJavaOrchestrateExceptionWith500status() {
    var expected = ExceptionRule.builder()
        .exception(GraphqlJavaOrchestrateException.class)
        .responseStatus(INTERNAL_SERVER_ERROR)
        .title("Internal server error")
        .build();

    var result =
        getExceptionRule(new GraphqlJavaOrchestrateException(INTERNAL_SERVER_ERROR, "Something went wrong serverside"));

    assertThat(result.isPresent(), is(true));
    assertThat(result.get()
        .getException(), is(expected.getException()));
    assertThat(result.get()
        .getTitle(), is(expected.getTitle()));
    assertThat(result.get()
        .getResponseStatus(), is(expected.getResponseStatus()));
  }

  @Test
  void getExceptionRule_returnsInternalServerErrorRule_forGraphlqlJavaOrchestrateExceptionWith400status() {
    var expected = ExceptionRule.builder()
        .exception(GraphqlJavaOrchestrateException.class)
        .responseStatus(BAD_REQUEST)
        .title("Error while processing the request")
        .detail(true)
        .build();

    var result = getExceptionRule(new GraphqlJavaOrchestrateException(BAD_REQUEST, "Bad request"));

    assertThat(result.isPresent(), is(true));
    assertThat(result.get()
        .getException(), is(expected.getException()));
    assertThat(result.get()
        .getTitle(), is(expected.getTitle()));
    assertThat(result.get()
        .getResponseStatus(), is(expected.getResponseStatus()));
    assertThat(result.get()
        .isDetail(), is(expected.isDetail()));
  }

}
