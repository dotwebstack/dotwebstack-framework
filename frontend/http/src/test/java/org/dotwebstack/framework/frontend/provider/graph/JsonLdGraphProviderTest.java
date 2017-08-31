package org.dotwebstack.framework.frontend.provider.graph;


import org.dotwebstack.framework.provider.graph.JsonLdGraphProvider;
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
public class JsonLdGraphProviderTest {

    @Mock
    private OutputStream outputStream;

    @Captor
    private ArgumentCaptor<byte[]> byteCaptor;

    @Test
    public void isWritableForJsonLdMediaType() {
        // Arrange
        JsonLdGraphProvider provider = new JsonLdGraphProvider();

        // Act
        boolean result = provider.isWriteable(LinkedHashModel.class, null, null,
                new MediaType("application", "ld+json"));

        // Assert
        assertThat(result, is(true));
    }

    @Test
    public void isNotWritableForStringClass() {
        // Arrange
        JsonLdGraphProvider provider = new JsonLdGraphProvider();

        // Act
        boolean result = provider.isWriteable(String.class, null, null,
                new MediaType("application", "ld+json"));

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void isNotWritableForXmlMediaType() {
        // Arrange
        JsonLdGraphProvider provider = new JsonLdGraphProvider();

        // Act
        boolean result = provider.isWriteable(String.class, null, null,
                MediaType.APPLICATION_XML_TYPE);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void writesJsonLdFormat() throws IOException {
        // Arrange
        JsonLdGraphProvider provider = new JsonLdGraphProvider();
        Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES)
                .add(RDF.TYPE, DBEERPEDIA.BACKEND)
                .add(RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL)
                .build();

        // Act
        provider.writeTo(model, null, null, null, null, null, outputStream);

        // Assert
        verify(outputStream).write(byteCaptor.capture(), anyInt(), anyInt());
        String result = new String(byteCaptor.getValue());
        assertThat(result, containsString("[{\"@id\":\"http://dbeerpedia.org#Breweries\",\"@type\":[\"http://dbeerpedia.org#Backend\"]"));
        assertThat(result, containsString("http://www.w3.org/2000/01/rdf-schema#label\":[{\"@value\":\"Beer breweries in The Netherlands\"}]}]"));
    }

}
