# Transition to OASv3 

## OASv2 vs OASv3 parser

Reusable fields that can be defined in the `components` object:
* schemas
* responses
* parameters
* examples
* requestBodies
* headers
* securitySchemes
* links
* callbacks

Same functionality, new location:
`swagger` --> `openapi`
`host` + `basePath` + `schemes` --> `server`
`definitions` + `parameters` + `responses` --> `components`
`securityDefinitions` --> `security`
`consumes` --> `content` in a `requestbody`
`produces` --> `content` in a `response`


## Required changes in the dotwebstack-framework codebase

The swagger parser has recently lost its RC status, version 2.0.0 (supporting OASv3) has recently been released:
* Maven: https://mvnrepository.com/artifact/io.swagger.parser.v3/swagger-parser-v3/2.0.0
* GitHub: https://github.com/swagger-api/swagger-parser/tree/v2.0.0

The `swagger-parser dependency` has to be changed to:
```
    <dependency>
      <groupId>io.swagger.parser.v3</groupId>
      <artifactId>swagger-parser-v3</artifactId>
      <version>2.0.0</version>
    </dependency>
```

The majority of the elements from the model used by the OASv2 parser still exists in the model used by the OASv3 parser. However:
* Property classes --> Schema classes
* The OASv3 parser model makes use of inner classes and enums more often
* The `BodyParameter` doesn't exist anymore; it is replaced with a `RequestBody` which has been effectively split off from the other parameters (and according to the OASv3 specification isn't a parameter anymore)
* The Path - Operation model has been altered and now strictly follows the model as defined by the OASv3 spec.

The dotwebstack-framework only requires changes in the `openapi` module:
* OpenApiRequestMapper, RequestParameterMapper: Significant refactoring needed. These classes utilize the parser and the model intensively.
* Other: Imports need fixes because of classname changes. Some classes may require a tad more work because elements have been shifted around in the model

Besides the dependency on the `swagger-parser`, the dotwebstack-framework has a dependency on the Atlassian `swagger-request-validator` library (which is dependent on the `swagger-parser`). This library doesn't support OASv3 yet. Version 2.0, currently under development, will. Progress can be checked  [here](https://bitbucket.org/atlassian/swagger-request-validator/issues/113/v20-swagger-request-validator-v2).


## A multi-file OAS

*Documentation*
* https://swagger.io/docs/specification/using-ref/

*What do we split off? (Kadaster Dataplatform specific section)*
* Parameters used in multiple APIs, e.g. everything from the `_kadasterdataplatform.trig` file 
* Error en ExtendedError definitions
* Responses used in multiple APIs, e.g. 400, 401, 403, 406, 503
