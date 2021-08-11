package org.dotwebstack.framework.example.oasproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class ExampleOasProxyApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleOasProxyApplication.class, args);
  }
}
