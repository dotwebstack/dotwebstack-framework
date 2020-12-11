package org.dotwebstack.framework.example.json;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class ExampleJsonApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleJsonApplication.class, args);
  }

}
