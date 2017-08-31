package org.dotwebstack.framework.frontend.provider.graph;


import org.dotwebstack.framework.provider.graph.TriGGraphProvider;
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
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TriGGraphProviderTest {

    @Mock
    private OutputStream outputStream;

    @Captor
    private ArgumentCaptor<byte[]> byteCaptor;

    @Test
    public void isWritableForTriGMediaType() {
        // Arrange
        TriGGraphProvider provider = new TriGGraphProvider();

        // Act
        boolean result = provider.isWriteable(LinkedHashModel.class, null, null,
                new MediaType("application", "trig"));

        // Assert
        assertThat(result, is(true));
    }

    @Test
    public void isNotWritableForStringClass() {
        // Arrange
        TriGGraphProvider provider = new TriGGraphProvider();

        // Act
        boolean result = provider.isWriteable(String.class, null, null,
                new MediaType("application", "trig"));

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void isNotWritableForXmlMediaType() {
        // Arrange
        TriGGraphProvider provider = new TriGGraphProvider();

        // Act
        boolean result = provider.isWriteable(String.class, null, null,
                MediaType.APPLICATION_XML_TYPE);

        // Assert
        assertThat(result, is(false));
    }

    @Test
    public void writesTriGFormat() throws IOException {
        // Arrange
        TriGGraphProvider provider = new TriGGraphProvider();
        Model model = new ModelBuilder().subject(DBEERPEDIA.BREWERIES)
                .add(RDF.TYPE, DBEERPEDIA.BACKEND)
                .add(RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL)
                .build();

        // Act
        provider.writeTo(model, null, null, null, null, null, outputStream);

        // Assert
        // 2 times? feels like weird behaviour of the TriG parser
        verify(outputStream, times(2)).write(byteCaptor.capture(), anyInt(), anyInt());
        List<byte[]> values = byteCaptor.getAllValues();
        String result1 = new String(values.get(0));
        String result2 = new String(values.get(1));

        assertThat(result1, containsString("<http://dbeerpedia.org#Breweries> a <http://dbeerpedia.org#Backend> ;"));
        assertThat(result1, containsString("<http://www.w3.org/2000/01/rdf-schema#label> \"Beer breweries in The Netherlands\""));

        assertThat(result2, containsString("<http://dbeerpedia.org#Breweries> a <http://dbeerpedia.org#Backend> ;"));
        assertThat(result2, containsString("<http://www.w3.org/2000/01/rdf-schema#label> \"Beer breweries in The Netherlands\""));
    }

}
