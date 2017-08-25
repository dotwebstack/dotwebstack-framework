package org.dotwebstack.framework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class IntegrationTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(IntegrationTestApplication.class, args);
  }

}
