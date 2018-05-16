# HOWTO extend the framework

The dotwebstack framework is designed to be extended. The following extention points are available:

- Adding a new front-end module;
- Adding a new back-end;

## Adding a new front-end module
All front-end modules implement and extend [jersey](https://jersey.github.io/) interfaces and classes.

The front-end uses one generic module, [http](https://github.com/dotwebstack/dotwebstack-framework/tree/master/frontend/http), from which all other front-end modules are derived. Among other tasks, the http module will look for other front-end modules and initialize them, so these modules can register the appropiate URL paths.

Currently, two additional front-end modules are available in the dotwebstack-framework repository:

- [ld](https://github.com/dotwebstack/dotwebstack-framework/tree/master/frontend/ld), for returning Linked Data serialisations (RDF/XML, Turtle, JSON-LD) for dereferenceable URI's;
- [openapi](https://github.com/dotwebstack/dotwebstack-framework/tree/master/frontend/openapi), for dealing with RESTful API's that conform to the Open API Specification.

The dotwebstack-theatre-legacy reponsitory contains a third module:

- [ldtlegacy](https://github.com/dotwebstack/dotwebstack-theatre-legacy/tree/master/src/main/java/org/dotwebstack/ldtlegacy), for returning a HTML representation for dereferenceable URI's.

Please look at the module implementation in the dotwebstack-theatre-legacy repository for guidance how to create your own module.

## Adding a new back-end
To add a new back-end to the dotwebstack-framework (for example a SQL backend) you need to:

- Add a subclass of `elmo:Backend` to the elmo vocabulary (for example: `elmo:SqlBackend`);
- Add a class (`SqlBackend`) and corresponding factory that implements the interface `org.dotwebstack.framework.backend.Backend`;
- Add a class (`SqlBackendInformationProduct`) and corresponding factory that extends the class `org.dotwebstack.framework.informationproduct.AbstractInformationProduct`.

The class `SqlBackend` will contain the configuration information with regard to the operation you need to perform at the backend. For example, you might need a property `query` to specify the particular SQL query and a property `endpoint` to specify the location of the SQL database).

The class `SqlBackendInformationProduct` will implement the actual call to the SQL database. This call will be part of the `getResult` method. Such a call would:

- Merge parameter values with the query;
- Perform the call to the SQL database;
- Transform the result to RDF4j classes.

The last step is a consequency of the architectural design principle that all backends will return RDF data (tuple results or graph results). This greatly reduces the complexity of the framework. The consequence is that this concern is the responsibility of the backend implementor, who will probably has the best knowledge about such transformation!