# Getting started

To get started with DotWebStack Framework, create a new Spring project and add dependencies to one or more of
the service modules, one or more of the backend modules and spring-boot-starter-webflux. In maven
this would look something like this:

```xml

<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>
  <dependency>
    <groupId>org.dotwebstack.framework</groupId>
    <artifactId>backend-rdf4j</artifactId>
  </dependency>
  <dependency>
    <groupId>org.dotwebstack.framework</groupId>
    <artifactId>service-graphql</artifactId>
  </dependency>
</dependencies>
```

You then need to add a Spring application with a component scan on the `org.dotwebstack.framework`
package:

```java

@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class ExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }
}
```

In the `resource` folder, there should be a properties file (`application.yml`) and a `config`
folder. The config folder contains a GraphQL schema file (`schema.graphqls`), `dotwebstack.yaml`
configuration file and adapter files for the specific service and backend. The service adapter files
contain the mapping rules to get from a request for a specific service to the GraphQL core and back
from the GraphQL core to a response for a specific service. The backend adapter files are defined in
a folder called `model` and contain the rules to generate a backend specific query from GraphQL and
read a GraphQL result from the query response. See the specific service and backend modules to find
out how to configure them.
