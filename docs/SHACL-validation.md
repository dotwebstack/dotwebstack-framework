## Configurating DWS Shacl Validation. ##
The configuration nesting is needed
*	Elmo:Serivce
    * Elmo:transaction
        * Elmo:sequentialFlow
            * Elmo:ValidationStep
                * Elmo:conformsTo
                    * Named graph with shacle constraints

The SHACL constraints should be available to DWS' in-memory RDF repository so the files should be in the config/model directory.
