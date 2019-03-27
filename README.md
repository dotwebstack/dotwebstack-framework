# DotWebStack Framework

An extensible full-stack framework which offers the foundation and building blocks for developing (Linked) Data services, including URI dereferencing, RESTful API’s, and much more.

[![Build Status](https://travis-ci.org/dotwebstack/dotwebstack-framework.svg?branch=master)](https://travis-ci.org/dotwebstack/dotwebstack-framework)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dotwebstack.framework/dotwebstack-core/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/org.dotwebstack.framework/dotwebstack-core/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.dotwebstack.framework%3Adotwebstack-framework&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.dotwebstack.framework%3Adotwebstack-framework)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.dotwebstack.framework%3Adotwebstack-framework&metric=coverage)](https://sonarcloud.io/dashboard?id=org.dotwebstack.framework%3Adotwebstack-framework)

## TODO

* Global configuration for namespaces
* Query filter arguments
* Query sort argument
* Validate SHACL shapes against GraphQL schema
* Decide whether to use property shape names for GraphQL schema mapping
* Support property paths for property shapes
* Combine query results from multiple backends
* Naming consistency: `iri` vs `uri`
* Single or multiple repository connections?

## Release

To release a new version, run the following statement and follow instructions:

```
mvn release:prepare
```

Travis CI will now pick up the new tag and deploy the artifacts to the Central repository.

Clean up afterwards:

```
mvn release:clean
```

## Links

* Travis CI: https://travis-ci.org/dotwebstack/dotwebstack-framework
* SonarCloud: https://sonarcloud.io/dashboard/index/org.dotwebstack.framework:dotwebstack-framework

## License

The DotWebStack Framework is published under the [MIT License](LICENSE.md).


