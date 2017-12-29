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

Voor Geometry:
```
parameters:
  - in: body
    x-dotwebstack-parameter:
      identifier: http://foo#GeometryFilter
      property: geometry
  - in: header
    name: X-Content-Crs
    x-dotwebstack-parameter:
      identifier: http://foo#GlobalCrsParameter
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