package org.dotwebstack.framework.example.rmlproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class ExampleRmlProxyApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleRmlProxyApplication.class, args);
  }
}
