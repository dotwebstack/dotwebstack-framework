package org.dotwebstack.framework.backend.sparql.validationstep;

import java.util.Collection;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.backend.sparql.QueryEvaluator;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.transaction.flow.step.AbstractStepExecutor;
import org.dotwebstack.framework.transaction.flow.step.validation.ValidationStep;
import org.dotwebstack.framework.validation.ShaclValidationException;
import org.dotwebstack.framework.validation.ShaclValidator;
import org.dotwebstack.framework.validation.ValidationReport;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.RepositoryResult;

public class ValidationStepExecutor extends AbstractStepExecutor<ValidationStep> {

  private FileConfigurationBackend backend;

  private Model transactionModel;

  private ValidationStep validationStep;

  private QueryEvaluator queryEvaluator;

  public ValidationStepExecutor(@NonNull ValidationStep validationStep,
      @NonNull Model transactionModel, @NonNull FileConfigurationBackend backend,
      @NonNull QueryEvaluator queryEvaluator) {
    super(validationStep);
    this.backend = backend;
    this.transactionModel = transactionModel;
    this.validationStep = validationStep;
    this.queryEvaluator = queryEvaluator;
  }

  @Override
  public void execute(Collection<Parameter> parameters, Map<String, String> parameterValues) {
    final Model validationModel;

    RepositoryResult<Resource> graphs = backend.getRepository().getConnection().getContextIDs();
    System.out.println("*** all graphs");
    while (graphs.hasNext()) {
      System.out.println(graphs.next().toString());
    }
    System.out.println("***");

    try {
      if (ELMO.SHACL_CONCEPT_GRAPHNAME.equals(step.getConformsTo())) {
        System.out.println(ELMO.SHACL_CONCEPT_GRAPHNAME + " = " + step.getConformsTo());
      } else {
        System.out.println("Found no shacl concept graph name....");
      }
      System.out.println("graph name: " + step.getConformsTo().toString());
      validationModel =
          QueryResults.asModel(backend.getRepository().getConnection().getStatements(null, null,
              null, step.getConformsTo()));
    } catch (RDF4JException ex) {
      System.out.println(ex.getMessage());
      System.out.println(ex.toString());
      throw new BackendException(String.format("........: (%s)", ex.getMessage()), ex);
    }

    final ShaclValidator shaclValidator = new ShaclValidator();
    System.out.println("transaction model:\n***");
    System.out.println(transactionModel.toString() + "\n***");
    System.out.println("validation model:\n***");
    System.out.println(validationModel.toString() + "\n***");
    final ValidationReport report = shaclValidator.validate(transactionModel, validationModel);
    System.out.println("result --> " + report.isValid());
    if (!report.isValid()) {
      throw new ShaclValidationException(report.printReport());
    }
  }
}
