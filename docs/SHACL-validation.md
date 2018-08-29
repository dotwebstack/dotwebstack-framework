## Configurating DWS SHACL Validation. ##
The configuration nesting is needed
* Elmo:Service
  * Elmo:transaction
    * Elmo:sequentialFlow
      * Elmo:ValidationStep
        * Elmo:conformsTo
           * Named graph with SHACl constraints

The SHACL constraints should be available to DWS' in-memory RDF repository so the files should be in the config/model directory.
