package org.dotwebstack.framework.service.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebExceptionHandler;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.spring.webflux.advice.ProblemExceptionHandler;

@Configuration
public class HttpServiceConfiguration {

  @Bean
  ProblemModule problemModule() {
    return new ProblemModule();
  }

  @Bean
  @Order(-2)
  WebExceptionHandler problemExceptionHandler(ObjectMapper mapper,
      ExceptionHandling exceptionHandling) {
    return new ProblemExceptionHandler(mapper, exceptionHandling);
  }

}
