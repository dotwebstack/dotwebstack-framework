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

  private RdfModelTransformer() {}

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

  public static Model getModel(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    RDFWriter turtleWriter = Rio.createWriter(RDFFormat.TURTLE, byteArrayOutputStream);

    RDFParser trigParser = Rio.createParser(RDFFormat.TRIG);
    trigParser.setRDFHandler(turtleWriter);
    trigParser.parse(inputStream, "");

    Model result = Rio.parse(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()), "",
        RDFFormat.TURTLE);

    byteArrayOutputStream.close();
    return result;
  }
}
