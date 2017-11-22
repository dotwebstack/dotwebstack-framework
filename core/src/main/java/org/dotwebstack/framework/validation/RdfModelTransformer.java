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
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.topbraid.spin.util.JenaUtil;

public class RdfModelTransformer {

  public static Model transformTrigFileToModel(Resource trigFile) throws IOException {
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

  public static Model transformInputStreamToModel(InputStream inputStreamData) throws IOException {
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

  public static Model mergeResourceWithPrefixes(InputStream inputStreamPrefixes,
                                                InputStream inputStreamData) throws IOException {
    final Resource mergedDataResource = new InputStreamResource(
        new SequenceInputStream(inputStreamPrefixes, inputStreamData));
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    RDFWriter turtleWriter = Rio.createWriter(RDFFormat.TURTLE, byteArrayOutputStream);
    RDFParser trigParser = Rio.createParser(RDFFormat.TRIG);
    trigParser.setRDFHandler(turtleWriter);
    trigParser.parse(mergedDataResource.getInputStream(), "");
    Model model = JenaUtil.createMemoryModel();
    model.read(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "",
        FileUtils.langTurtle);
    byteArrayOutputStream.close();
    inputStreamData.close();
    return model;
  }
}
