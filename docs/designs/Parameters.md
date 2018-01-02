# Input output parameters

## Doel
	  
Het doel is om een een generieke oplossing te maken voor verschillende usecases.
Meerdere input parameters moeten mappen naar eenzelfde TermParameter.
Het Paginator object is beschikbaar in de query template.
	  
## Use cases

De Paginator en de GeometryFilter.
	  
## Links

Voor de implementatie van next en previous links moet het Subject filter dit support leveren.
Het subject filter heeft een mechanisme om op next links en previous links te genereren op basis van de resultset.

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