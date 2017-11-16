package org.dotwebstack.framework.validation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import org.apache.jena.rdf.model.Model;
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

  private Model transformInputStreamToModel(InputStream inputStreamData) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    RDFWriter turtleWriter = Rio.createWriter(RDFFormat.TURTLE, byteArrayOutputStream);
    RDFParser trigParser = Rio.createParser(RDFFormat.TRIG);

    trigParser.setRDFHandler(turtleWriter);
    trigParser.parse(inputStreamData, "");

    Model model = JenaUtil.createMemoryModel();
    model.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "",
        FileUtils.langTurtle);

    byteArrayOutputStream.close();
    inputStreamData.close();

    return model;
  }

  @Override
  public void reportValidationResult(Model reportModel) throws ShaclValidationException {
    Boolean isValid = reportModel.listStatements().toList()
        .stream()
        .filter(
            item -> item.getPredicate().toString()
                .equals("http://www.w3.org/ns/shacl#conforms"))
        .findFirst().get().getObject().asLiteral().getBoolean();
    
    if (!isValid) {
      final String resultPath = reportModel.listStatements().toList()
          .stream()
          .filter(
              item -> item.getPredicate().toString()
                  .equals("http://www.w3.org/ns/shacl#resultPath"))
          .findFirst().get().getObject().toString();

      final String resultMessage = reportModel.listStatements().toList()
          .stream()
          .filter(
              item -> item.getPredicate().toString()
                  .equals("http://www.w3.org/ns/shacl#resultMessage"))
          .findFirst().get().getObject().toString();

      final String focusNode = reportModel.listStatements().toList()
          .stream()
          .filter(
              item -> item.getPredicate().toString()
                  .equals("http://www.w3.org/ns/shacl#focusNode"))
          .findFirst().get().getObject().toString();
      throw new ShaclValidationException(String
          .format("Invalid configuration at path [%s] on node [%s] with error message [%s]",
              resultPath, focusNode, resultMessage));
    }
  }

  @Override
  public void validate(InputStream data, Resource shapes) throws ShaclValidationException {
    try {
      Model dataModel = transformInputStreamToModel(data);
      Model dataShape = transformTrigFileToModel(shapes);

      org.apache.jena.rdf.model.Resource report = ValidationUtil
          .validateModel(dataModel, dataShape, true);

      reportValidationResult(report.getModel());
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

      reportValidationResult(report.getModel());
    } catch (IOException e) {
      throw new ShaclValidationException("File could not read during the validation process", e);
    }
  }
}
