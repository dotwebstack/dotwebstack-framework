# DotWebStack Framework

An extensible full-stack framework which offers the foundation and building blocks for developing RESTful API’s.

[![Build Status](https://travis-ci.org/dotwebstack/dotwebstack-framework.svg?branch=master)](https://travis-ci.org/dotwebstack/dotwebstack-framework)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dotwebstack.framework/dotwebstack-core/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/org.dotwebstack.framework/dotwebstack-core/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.dotwebstack.framework%3Adotwebstack-framework&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.dotwebstack.framework%3Adotwebstack-framework)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.dotwebstack.framework%3Adotwebstack-framework&metric=coverage)](https://sonarcloud.io/dashboard?id=org.dotwebstack.framework%3Adotwebstack-framework)

## TODO

* Global configuration for namespaces
* Query filter arguments
* Validate SHACL shapes against GraphQL schema
* Decide whether to use property shape names for GraphQL schema mapping
* Combine query results from multiple backends
* Naming consistency: `iri` vs `uri`
* Single or multiple repository connections?

## Getting started
To get started with DotWebStack, create a new Spring project and add dependencies to one or more of the 
service modules, one or more of the backend modules and spring-boot-starter-webflux. In maven this would look
something like this:

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

You then need to add a Spring application with a component scan on the `org.dotwebstack.framework` package:

```java
@SpringBootApplication
@ComponentScan("org.dotwebstack.framework")
public class ExampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(ExampleApplication.class, args);
  }
}
```

In the `resource` folder, there should be a properties file (`application.yml`) and a `config` folder.
The config folder contains a GraphQL schema file (`schema.graphqls`) and adapter files for the specific service and 
backend. The service adapter files contain the mapping rules to get from a request for a specific service to the 
GraphQL core and back from the GraphQL core to a response for a specific service. The backend adapter files are defined 
in a folder called `model` and contain the rules to generate a backend specific query from GraphQL and read a GraphQL 
result from the query response. See the specific service and backend modules to find out how to configure them.

## Links

* Travis CI: https://travis-ci.org/dotwebstack/dotwebstack-framework
* SonarCloud: https://sonarcloud.io/dashboard/index/org.dotwebstack.framework:dotwebstack-framework

## License

The DotWebStack Framework is published under the [MIT License](LICENSE.md).


