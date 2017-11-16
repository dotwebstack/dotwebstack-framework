package org.dotwebstack.framework.validate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileUtils;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.spin.util.JenaUtil;

@Service
public class ShaclValidator implements Validator<Resource, Model, InputStream> {

  private static final Logger LOG = LoggerFactory.getLogger(ShaclValidator.class);

  private Model transformTrigFileToModel(Resource trigFile) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    InputStream trigFileInputStream = trigFile.getInputStream();

    RDFWriter turtleWriter = Rio.createWriter(RDFFormat.TURTLE, byteArrayOutputStream);
    RDFParser trigParser = Rio.createParser(RDFFormat.TRIG);

    trigParser.setRDFHandler(turtleWriter);
    if (trigFile instanceof InputStreamResource) {
      trigParser.parse(trigFileInputStream, "/");
    } else {
      trigParser.parse(trigFileInputStream, "");
    }

    Model model = JenaUtil.createMemoryModel();
    model.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "",
        FileUtils.langTurtle);

    byteArrayOutputStream.close();
    trigFileInputStream.close();

    return model;
  }

  @Override
  public void getValidationReport(Model reportModel) throws ShaclValidationException {
    StmtIterator iterator = reportModel.listStatements();

    Boolean isValid = false;
    String resultPath = "";
    String resultMessage = "";
    String focusNode = "";

    while (iterator.hasNext()) {
      Statement statement = iterator.nextStatement();
      Property predicate = statement.getPredicate();
      RDFNode object = statement.getObject();

      if (predicate.getLocalName().equals("conforms") && object instanceof Literal) {
        Literal literal = object.asLiteral();
        isValid = literal.getBoolean();
      }
      if (predicate.getLocalName().equals("resultPath")
          && object instanceof org.apache.jena.rdf.model.Resource) {
        org.apache.jena.rdf.model.Resource resource = object.asResource();
        resultPath = resource.toString();
      }
      if (predicate.getLocalName().equals("resultMessage") && object instanceof Literal) {
        Literal literal = object.asLiteral();
        resultMessage = literal.getString();
      }
      if (predicate.getLocalName().equals("focusNode") && object instanceof RDFNode) {
        RDFNode rdfNode = object;
        focusNode = rdfNode.toString();
        break;
      }
    }
    if (!isValid) {
      throw new ShaclValidationException(String
          .format("Invalid configuration at path [%s] on node [%s] with error message [%s]",
              resultPath, focusNode, resultMessage));
    }
  }

  @Override
  public void validate(InputStream data, Resource shapes) throws ShaclValidationException {
    try {
      Model dataModel = transformTrigFileToModel(new InputStreamResource(data));
      Model dataShape = transformTrigFileToModel(shapes);

      org.apache.jena.rdf.model.Resource report = ValidationUtil
          .validateModel(dataModel, dataShape, true);

      getValidationReport(report.getModel());
    } catch (IOException e) {
      throw new ShaclValidationException("File could not read during the validation process", e);
    }
  }

  @Override
  public void validate(InputStream data, Resource shapes, Resource prefixes)
      throws ShaclValidationException {
    try {
      Resource mergedDataResource = new InputStreamResource(
          new SequenceInputStream(prefixes.getInputStream(), data));
      Model dataModel = transformTrigFileToModel(mergedDataResource);
      Model dataShape = transformTrigFileToModel(shapes);

      org.apache.jena.rdf.model.Resource report = ValidationUtil
          .validateModel(dataModel, dataShape, true);

      getValidationReport(report.getModel());
    } catch (IOException e) {
      throw new ShaclValidationException("File could not read during the validation process", e);
    }
  }
}
