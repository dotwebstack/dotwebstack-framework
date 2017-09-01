package org.dotwebstack.framework.frontend.http.provider.graph;


import org.dotwebstack.framework.frontend.http.provider.graph.RdfXmlGraphProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RdfXmlGraphProviderTest {

    @Mock
    private OutputStream outputStream;

    @Captor
    private ArgumentCaptor<byte[]> byteCaptor;

    @Test
    public void isWritableForRdfXmlMediaType() {
        // Arrange
        RdfXmlGraphProvider provider = new RdfXmlGraphProvider();

        // Act
        boolean result = provider.isWriteable(LinkedHashModel.class, null, null,
                new MediaType("application", "rdf+xml"));

        // Assert
        assertThat(result, is(true));
    }

    @Test
    public void isNotWritableForStringClass() {
        // Arrange
        RdfXmlGraphProvider provider = new RdfXmlGraphProvider();

        // Act
        boolean result = provider.isWriteable(String.class, null, null,
                new MediaType("application", "rdf+xml"));

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void isNotWritableForXmlMediaType() {
        // Arrange
        RdfXmlGraphProvider provider = new RdfXmlGraphProvider();

        // Act
        boolean result = provider.isWriteable(String.class, null, null,
                MediaType.APPLICATION_XML_TYPE);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void writesRdfXmlFormat() throws IOException {
        // Arrange
        RdfXmlGraphProvider provider = new RdfXmlGraphProvider();
        Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES)
                .add(RDF.TYPE, DBEERPEDIA.BACKEND)
                .add(RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL)
                .build();

        // Act
        provider.writeTo(model, null, null, null, null, null, outputStream);

        // Assert
        verify(outputStream).write(byteCaptor.capture(), anyInt(), anyInt());
        String result = new String(byteCaptor.getValue());
        assertThat(result, containsString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertThat(result, containsString("<rdf:Description rdf:about=\"http://dbeerpedia.org#Breweries\">"));
        assertThat(result, containsString("<rdf:type rdf:resource=\"http://dbeerpedia.org#Backend\"/>"));
        assertThat(result, containsString("<label xmlns=\"http://www.w3.org/2000/01/rdf-schema#\">Beer breweries in The Netherlands</label>"));
    }

}
