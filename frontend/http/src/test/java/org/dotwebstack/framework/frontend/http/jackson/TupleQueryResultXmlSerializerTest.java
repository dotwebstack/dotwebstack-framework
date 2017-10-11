package org.dotwebstack.framework.frontend.http.jackson;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TupleQueryResultXmlSerializerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private TupleQueryResult tupleQueryResult;

  @Mock
  private JsonGenerator jsonGenerator;

  private TupleQueryResultXmlSerializer serializer;

  @Before
  public void setUp() {
    serializer = new TupleQueryResultXmlSerializer();
  }

  @Test
  public void serialize_ThrowsException_WithMissingQuery() throws IOException, JSONException {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    serializer.serialize(null, jsonGenerator, null);
  }

  @Test
  public void serialize_ThrowsException_WithMissingJsonGenerator()
      throws IOException, JSONException {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    serializer.serialize(tupleQueryResult, null, null);
  }

  @Test
  public void serialize_GivesXmlResult_WhenQueryGivesData() throws IOException {
    // Arrange
    BindingSet bindingSet = new ListBindingSet(
        ImmutableList.of("identifier", "name", "yearOfFoundation", "craftMember", "fte"),
        DBEERPEDIA.BROUWTOREN, DBEERPEDIA.BROUWTOREN_NAME, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION,
        DBEERPEDIA.BROUWTOREN_CRAFT_MEMBER, DBEERPEDIA.BROUWTOREN_FTE);
    when(tupleQueryResult.hasNext()).thenReturn(true, false);
    when(tupleQueryResult.next()).thenReturn(bindingSet);

    XmlFactory factory = new XmlFactory();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ToXmlGenerator generator = factory.createGenerator(outputStream);

    // Act
    serializer.serialize(tupleQueryResult, generator, null);


    // Assert
    generator.close();
    assertThat(outputStream.toString(), containsString("<results>"));
    assertThat(outputStream.toString(), containsString(
        String.format("<identifier>%s</identifier>", DBEERPEDIA.BROUWTOREN.toString())));
    assertThat(outputStream.toString(), containsString("<name>Brouwtoren</name>"));
    assertThat(outputStream.toString(), containsString("<fte>1.8</fte>"));
    assertThat(outputStream.toString(), containsString("<craftMember>true</craftMember>"));
    assertThat(outputStream.toString(),
        containsString("<yearOfFoundation>2014</yearOfFoundation>"));
    assertThat(outputStream.toString(), containsString("</results>"));
  }

}
