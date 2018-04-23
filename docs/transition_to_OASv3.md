# OASv3 overstap

## OASv2 vs OASv3 parser

Herbruikbare onderdelen die in `components` gedefinieerd kunnen worden:
* schemas
* responses
* parameters
* examples
* requestBodies
* headers
* securitySchemes
* links
* callbacks

Zelfde functionaliteit, nieuwe locatie:
`swagger` --> `openapi`
`host` + `basePath` + `schemes` --> `server`
`definitions` + `parameters` + `responses` --> `components`
`securityDefinitions` --> `security`
`consumes` --> `content` in een `requestbody`
`produces` --> `content` in een `response`


## Waar zijn wijzigingen in de code nodig?

De swagger parser is ondertussen van de RC-status af, versie 2.0.0 is beschikbaar:
* Maven: https://mvnrepository.com/artifact/io.swagger.parser.v3/swagger-parser-v3/2.0.0
* GitHub: https://github.com/swagger-api/swagger-parser/tree/v2.0.0

swagger-parser dependency moet omgezet worden naar:
```
    <dependency>
      <groupId>io.swagger.parser.v3</groupId>
      <artifactId>swagger-parser-v3</artifactId>
      <version>2.0.0</version>
    </dependency>
```

Een groot deel van de elementen uit het model van OASv2 parser bestaat nog. Ook de utilities e.d. zijn vernieuwd. Echter: 

* Property klasses --> Schema klasses
* Er zit meer in inner enums/klasses
* BodyParameter bestaat niet meer; de vervangende `RequestBody` vertoont nu afwijkend gedrag van de andere parameters (valt volgens de spec ook niet meer onder de parameters)
* Path - Operation relatie is aangepast en volgt nu precies de opbouw van de elementen zoals gedefinieerd in de spec

In het dotwebstack-framework zijn alleen aanpassingen nodig in de `openapi` module:

* OpenApiRequestMapper, RequestParameterMapper: Significante refactoring nodig. Hier wordt veel gebruik gemaakt van de parser en een groot deel van het model.
* Overig: Vooral imports fixen ivm naamwijziging. Hier en daar licht refactor werk nodig door verschuivingen van elementen in het model.

Daarnaast hebben we een vrij sterke afhankelijkheid op de Atlassian `swagger-request-validator` library. Deze ondersteunt op dit moment OASv3 nog niet. Versie 2.0.0, op dit moment in de maak, gaat dit wel doen. Voortgang hiervan kan [hier](https://bitbucket.org/atlassian/swagger-request-validator/issues/113/v20-swagger-request-validator-v2) gevolgd worden.


## OAS splitsen

*Documentatie*
* https://swagger.io/docs/specification/using-ref/

*Wat afsplitsen?*
* Parameters die we in meerdere APIs gebruiken, e.g. alles uit de `_kadasterdataplatform.trig` file
* Error en ExtendedError
* Responses die we in meerdere APIs gebruiken, e.g. 400, 401, 403, 406, 503



