# Framework components

The following framework components are identified:

Component             | Owner  | Description
----------------------|--------|-----------------------------------------------------------------
frontend.http         |        | Routing, starting point for any frontend request
frontend.oas          | GRID   | Frontend for Open API Specification requests
frontend.ld           | PR6-13 | Frontend for LDP / LD / SPARQL-Graph protocol requests
core                  | multi  | Factory for information product retrieval and transactions
backend.elasticsearch | GRID   | Adapter for elastic search requests
backend.sparql        | ?      | Adapter for SPARQL endpoint requests

## Frontend

## Core

The core has the following features:

Feature          | Owner  | Description
-----------------|--------|-------------------------------------------------------
core.config      | GRID   | Configuration loader for all framework components
core.product     | GRID   | Factory for information products (read requests)
core.transaction | PR6-13 | Factory for transactions (write requests)
core.backend     | GRID   | Abstract query logic
