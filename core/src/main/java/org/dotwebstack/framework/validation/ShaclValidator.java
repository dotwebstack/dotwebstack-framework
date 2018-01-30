package org.dotwebstack.framework.validation;

import java.util.Optional;
import lombok.NonNull;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleIRI;
import org.eclipse.rdf4j.sail.memory.model.MemIRI;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.validation.ValidationUtil;

@Service
public class ShaclValidator {

  public ValidationReport validate(@NonNull org.eclipse.rdf4j.model.Model dataModel,
      @NonNull org.eclipse.rdf4j.model.Model shapesModel) {
    Resource report =
        ValidationUtil.validateModel(getJenaModel(dataModel), getJenaModel(shapesModel), true);
    return new ValidationReport(report.getModel());
  }

  private Model getJenaModel(@NonNull org.eclipse.rdf4j.model.Model model) {
    Model jenaModel = ModelFactory.createDefaultModel();
    java.util.Iterator<org.eclipse.rdf4j.model.Statement> iterator = model.iterator();

    while (iterator.hasNext()) {
      org.eclipse.rdf4j.model.Statement rdf4jStatement = iterator.next();

      // create resource / subject
      Resource resource = rdf4jResourceToJenaResource(jenaModel, rdf4jStatement.getSubject());
      // create property / predicate
      Property property = rdf4jPropertyToJenaProperty(jenaModel, rdf4jStatement.getPredicate());
      // create rdfnode / object
      RDFNode node = rdf4jValueToJenaRdfNode(jenaModel, rdf4jStatement.getObject());

      Statement statement = ResourceFactory.createStatement(resource, property, node);
      jenaModel.add(statement);
    }
    return jenaModel;
  }

  private Resource rdf4jResourceToJenaResource(@NonNull Model jenaModel,
      @NonNull org.eclipse.rdf4j.model.Resource resource) {
    if (resource instanceof SimpleIRI || resource instanceof MemIRI) {
      return jenaModel.createResource(resource.stringValue());
    } else {
      return jenaModel.createResource(new AnonId(resource.stringValue()));
    }
  }

  private Property rdf4jPropertyToJenaProperty(@NonNull Model jenaModel, @NonNull IRI resource) {
    return jenaModel.createProperty(resource.getNamespace(), resource.getLocalName());
  }

  private RDFNode rdf4jValueToJenaRdfNode(@NonNull Model jenaModel, @NonNull Value value) {
    if (value instanceof org.eclipse.rdf4j.model.Resource) {
      return rdf4jResourceToJenaResource(jenaModel, (org.eclipse.rdf4j.model.Resource) value);
    } else {
      return rdf4jLiteralToJenaRdfNode(jenaModel, (Literal) value);
    }
  }

  private RDFNode rdf4jLiteralToJenaRdfNode(@NonNull Model jenaModel, @NonNull Literal value) {
    final Optional<String> language = value.getLanguage();
    if (value.getDatatype() != null) {
      return jenaModel.createTypedLiteral(value.stringValue(), value.getDatatype().stringValue());
    } else if (language.isPresent()) {
      return jenaModel.createLiteral(value.stringValue(), language.get());
    } else {
      return jenaModel.createLiteral(value.stringValue());
    }
  }
}
