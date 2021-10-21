package org.dotwebstack.framework.example.orchestrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class ExampleOrchestrateApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleOrchestrateApplication.class, args);
  }
}
