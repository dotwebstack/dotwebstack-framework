package org.dotwebstack.framework.backend;

import java.util.Collection;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.StepExecutor;
import org.dotwebstack.framework.transaction.flow.step.persistence.PersistenceStep;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

public interface Backend {

  Resource getIdentifier();

  InformationProduct createInformationProduct(Resource identifier, String label,
      Collection<Parameter> parameters, Model statements);

  StepExecutor createPersistenceStepExecutor(PersistenceStep persistenceStep,
      Model transactionModel);

}
