# Input output parameters

## Doel
	  
Het doel is om een een generieke oplossing te maken voor verschillende usecases.
Meerdere input parameters kunnen properties uit een andere parameter verkrijgen.
Input parameters hebben dus afhankelijkheden met elkaar. 
Het GeometryParameter/Paginator object is beschikbaar in de query template.
Het GeometryParameter object is een wrapper om het JTS Geometry object en bevat de volgende properties:
- Operator (intersects, within, etc.)
- Geometry

## Use cases

De Paginator en de GeometryFilter.
	  
## Afhankelijkheden tussen parameters
	  
De parameters krijgen referenties naar andere Parameters:
	  
```
	  ro:GeometryFilter a elmo:GeometryFilter;
            elmo:name "_geo";
          elmo:refs [
              elmo:ref ro:CrsParameter;
              elmo:ref ro:OperationParameter;
          ]
          .
```
          
	  
## Links

Voor de implementatie van next en previous links moet het Term filter dit support leveren.
Het term filter heeft een mechanisme om op next links en previous links te genereren op basis van de resultset.

## Voorbeelden OpenAPI specification

Gegeven de volgende definities: 
```
GeoJSON:
    type: "object"
    required:
    - "coordinates"
    - "type"
    properties:
      type:
        type: "string"
        enum:
        - "Point"
        - "Polygon"
        - "LineString"
        - "MultiPoint"
        - "MultiLineString"
        - "MultiPolygon"
        example: "Polygon"
      coordinates:
        type: "array"
# do *not* set type of array content explicitly (because it may vary and depend on whether
# this Geo object describes point or polygon or multi-point, or.. etc)
        description: "Array met coördinaten behorende bij dit GeoJSON object. De structuur van de array hangt af van het `type`."
        example: [[[5.858910083770752,51.84376540294041],[5.85968255996704,51.84259879644993],[5.860852003097533,51.84413658957469],[5.858910083770752,51.84376540294041]]]
		
GeoPost: 
 properties:
            _geo:
              type: "object"
			  x-dotwebstack-parameter:
                identifier: http://foo#GeometryFilter  
	         properties:
                intersects:
                  $ref: "#/definitions/GeoJSON"
``` 
	  
Voor GeometryFilter  opgenomen in een body van een request:
```
 parameters:
   - name: body
     in: body
     schema: $ref: "#/definitions/GeoPost"
  
  - in: header
    name: X-Content-Crs
    x-dotwebstack-parameter:
      identifier: http://foo#GlobalCrsParameter
	  description: "CRS van de meegegeven geometrie"
      property: crs
```
Voor Paginator:

```
parameters:
   - in: query
     name: p
     x-dotwebstack-parameter:
       identifier: http://foo#Paginator
       property: page
   - in: query
     name: ps
     x-dotwebstack-parameter:
       identifier: http://foo#Paginator
       property: pageSize
``` 


## CRS

Uiteindelijk moet de ERTS 89 als CRS ondersteund worden.
http://www.opengis.net/def/crs/EPSG/0/4258

Client stuurt een request met een geometrie in RD-projectie
De parameter afhandeling leest de geometrie in als zijnde geprojecteerd in RD (obv CrsParameter) en transformeert die naar ETRS89.
De data in GraphDB is geprojecteerd in ETRS89.

```
PREFIX geo: <http://www.opengis.net/ont/geosparql#>
PREFIX geof: <http://www.opengis.net/def/function/geosparql/>
PREFIX geosparql: <http://www.opengis.net/ont/geosparql#>
CONSTRUCT {
    ?s ?p ?o .
}
WHERE {
    {
        GRAPH ?g {
            bind(<http://data.informatiehuisruimte.nl/ro/id/geometry/5B6E25AE12AF05F7F2D5B2018BFC6C72> as ?s)
             ?s geosparql:asWKT ?geometry .
            FILTER(geof:sfWithin(?geometry,"<http://www.opengis.net/def/crs/EPSG/0/4258>POINT (6.591371722193622 53.26016547820686)"^^geo:wktLiteral))
              ?s ?p ?o .
````

## Conclusie

We zijn tot de conclusie gekomen dat het design/implementatie rekening moet houden met:
-	Parameters afhankelijkheden hebben (zowel bij creatie als properties)

     De Geometry filter moet een property uit een andere parameter (waar de CRS is ingesteld) bepalen.

-	Globale CRS en lokale CRS instellingen gedaan worden. 
-	Voor geometrie-functies ondersteunen we een hardcoded functie beschrijving die door de Geometry filter opgepakt moet worden (within, intersects, etc.).
