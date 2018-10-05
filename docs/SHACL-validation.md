## Configurating DWS SHACL Validation. ##
The configuration nesting is needed
* Elmo:Service
  * Elmo:transaction
    * Elmo:sequentialFlow
      * Elmo:ValidationStep
        * Elmo:conformsTo
           * Named graph with SHACL constraints

The files containing SHACL constraints should be in the config/model directory as they must be available in DWS' in-memory RDF repository 
