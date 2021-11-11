package org.dotwebstack.framework.example.openapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class ExampleOpenApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleOpenApiApplication.class, args);
  }
}
