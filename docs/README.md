# DotWebStack Framework

Publishing rich data services can become quite complex. The DotWebStack framework provides a set of standardized building blocks to build rich data services with a minimal development effort.
DotWebStack provides a lot of advantages compared to other solutions:

- **Robust**: The framework is built upon proven open-source components, such as [Spring Boot](https://spring.io/projects/spring-boot), [GraphQL Java](https://www.graphql-java.com/) and [Project Reactor](https://projectreactor.io/). By relying on these building blocks, lots of robust enterprise features are available out-of-the-box or can be integrated easily.
- **Performant**: The framework fully embraces the reactive programming paradigm and relies heavily on Java's multi-threading capabilities.
- **Declarative**: The framework is configuration-based, which means there is no need to write any code for simple applications. 
- **Flexible**: The framework is composed of multiple decoupled modules and can easily be extended for more advanced use cases where more customization is required.
- **Battle-tested**: The framework is being used within the Dutch government for a few years already. A high level of quality assurance is being enforced from the beginning.

## Features

- Backend-agnostic. Official implementations available: [JSON](./backend/json.md), [PostgreSQL](./backend/postgres.md) and [RDF4J](./backend/rdf4j.md))
- Different service types, such as [REST](./service/openapi.md) and [GraphQL](./service/graphql.md)
- Smart backend fetching-algorithm for optimal performance
- Hypermedia controls (configuration-based)
- Using open standards (e.g. JSON Schema, OpenAPI Specification, GraphQL)

Curious how it works? [Get started](./overview/getting_started.md) now!

## License

The DotWebStack Framework is published under the [MIT License](LICENSE.md).
