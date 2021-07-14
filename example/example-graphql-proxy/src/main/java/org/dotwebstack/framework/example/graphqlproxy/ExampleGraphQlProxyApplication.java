package org.dotwebstack.framework.example.graphqlproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class ExampleGraphQlProxyApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleGraphQlProxyApplication.class, args);
  }
}
