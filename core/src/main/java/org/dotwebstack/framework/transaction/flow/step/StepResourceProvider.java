package org.dotwebstack.framework.transaction.flow.step;

import org.dotwebstack.framework.AbstractResourceProvider;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.repository.RepositoryConnection;

public class StepResourceProvider extends AbstractResourceProvider {

  /*
   Must be able to load step resources, e.g.
   delegates creation to StepFactory


      config:validationStep a elmo:ValidationStep;
        elmo:conformsTo config:ConceptShapeGraph;

      config:updateStep a elmo:UpdateStep;
        rdfs:label "Add missing labels";
        elmo:backend elmo:TransactionRepository;
				elmo:query """
					insert {
						?concept rdfs:label ?label
					}
					where {
						?concept skos:prefLabel ?label.
						FILTER NOT EXISTS {
							?concept rdfs:label ?existinglabel
						}
					}
				""";

      config:finalInsertOrUpdateStep a elmo:PersistenceStep;
        elmo:persistenceStrategy a elmo-sp:InsertIntoGraph;
        elmo:backend config:SparqlBackend;
        elmo:targetGraph <http://dotwebstack.org/all-concepts>;

      config:updateStep2 a elmo:UpdateStep;
				rdfs:label "Add hash";
        elmo:backend config:MySparqlBackend;
				elmo:query """
					insert {
						graph <http://dotwebstack.org/all-concepts> {
							?concept wdrs:hash ?hash
						}
					}
					where {
						graph <http://dotwebstack.org/all-concepts> {
							select (md5(?definition) as ?hash) {
								?concept skos:definition ?definition
							}
						}
					}
				"""

*/

  public StepResourceProvider(
      ConfigurationBackend configurationBackend,
      ApplicationProperties applicationProperties) {
    super(configurationBackend, applicationProperties);
  }

  @Override
  protected Step createResource(Model model, IRI identifier) {
    return null;
  }

  @Override
  protected GraphQuery getQueryForResources(RepositoryConnection conn) {
    return null;
  }
}
