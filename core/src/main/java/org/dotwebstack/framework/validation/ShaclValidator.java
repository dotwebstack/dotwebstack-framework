package org.dotwebstack.framework.validation;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.URI;
import org.eclipse.rdf4j.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.validation.ValidationUtil;

@Service
public class ShaclValidator {

  private static final Logger LOG = LoggerFactory.getLogger(ShaclValidator.class);

  public ValidationReport validate(Model dataModel, Model shapesModel) {
    Resource report = ValidationUtil.validateModel(dataModel, shapesModel, true);
    return new ValidationReport(report.getModel());
  }
}
