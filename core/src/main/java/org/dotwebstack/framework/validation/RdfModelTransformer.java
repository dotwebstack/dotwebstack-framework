package org.dotwebstack.framework.validation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

public class RdfModelTransformer {

  public static Model mergeResourceWithPrefixes(InputStream inputStreamPrefixes,
      InputStream inputStreamData) throws IOException {
    final Resource mergedDataResource =
        new InputStreamResource(new SequenceInputStream(inputStreamPrefixes, inputStreamData));
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

    RDFWriter turtleWriter = Rio.createWriter(RDFFormat.TURTLE, byteArrayOutputStream);
    RDFParser trigParser = Rio.createParser(RDFFormat.TRIG);
    trigParser.setRDFHandler(turtleWriter);
    trigParser.parse(mergedDataResource.getInputStream(), "");

    Model result = Rio.parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "",
        RDFFormat.TURTLE);

    byteArrayOutputStream.close();
    inputStreamData.close();
    return result;
  }

  public static Model getModel(Resource trigFile) throws IOException {
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

    org.eclipse.rdf4j.model.Model result = Rio.parse(
        new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "", RDFFormat.TURTLE);

    byteArrayOutputStream.close();
    return result;
  }

  public static Model getModel(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    RDFWriter turtleWriter = Rio.createWriter(RDFFormat.TURTLE, byteArrayOutputStream);

    RDFParser trigParser = Rio.createParser(RDFFormat.TRIG);
    trigParser.setRDFHandler(turtleWriter);
    trigParser.parse(inputStream, "");

    org.eclipse.rdf4j.model.Model result = Rio.parse(
        new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "", RDFFormat.TURTLE);

    byteArrayOutputStream.close();
    return result;
  }
}
