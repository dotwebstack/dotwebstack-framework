package org.dotwebstack.framework.frontend.http.jackson;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.collect.ImmutableList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(MockitoJUnitRunner.class)
public class TupleQueryResultJsonSerializerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private JsonGenerator jsonGenerator;

  @Mock
  private TupleQueryResult tupleQueryResult;

  private TupleQueryResultJsonSerializer jsonSerializer;

  @Before
  public void setUp() {
    jsonSerializer = new TupleQueryResultJsonSerializer();
  }

  @Test
  public void serialize_ThrowsException_WithMissingQuery() throws IOException, JSONException {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    jsonSerializer.serialize(null, jsonGenerator, null);
  }

  @Test
  public void serialize_ThrowsException_WithMissingJsonGenerator()
      throws IOException, JSONException {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    jsonSerializer.serialize(tupleQueryResult, null, null);
  }

  @Test
  public void serialize_GivesEmptyResult_WhenQueryIsEmpty() throws IOException, JSONException {
    // Arrange
    JsonFactory factory = new JsonFactory();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    JsonGenerator jsonGenerator = factory.createGenerator(outputStream);
    when(tupleQueryResult.hasNext()).thenReturn(false);

    // Act
    jsonSerializer.serialize(tupleQueryResult, jsonGenerator, null);

    // Assert
    jsonGenerator.close();
    String result = outputStream.toString();
    JSONAssert.assertEquals("[]", result, true);
  }

  @Test
  public void serialize_GivesJsonResult_WhenQueryGivesData() throws IOException, JSONException {
    // Arrange
    JsonFactory factory = new JsonFactory();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    JsonGenerator jsonGenerator = factory.createGenerator(outputStream);

    BindingSet bindingSet = new ListBindingSet(
        ImmutableList.of("identifier", "name", "yearOfFoundation", "craftMember", "fte"),
        DBEERPEDIA.BROUWTOREN, DBEERPEDIA.BROUWTOREN_NAME, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION,
        DBEERPEDIA.BROUWTOREN_CRAFT_MEMBER, DBEERPEDIA.BROUWTOREN_FTE);
    when(tupleQueryResult.hasNext()).thenReturn(true, false);
    when(tupleQueryResult.next()).thenReturn(bindingSet);

    // Act
    jsonSerializer.serialize(tupleQueryResult, jsonGenerator, null);

    // Assert
    jsonGenerator.close();
    String result = outputStream.toString();
    JSONArray expected = new JSONArray().put(
        new JSONObject().put("identifier", DBEERPEDIA.BROUWTOREN.stringValue()).put("name",
            DBEERPEDIA.BROUWTOREN_NAME.stringValue()).put("yearOfFoundation",
                DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.shortValue()).put("craftMember",
                    DBEERPEDIA.BROUWTOREN_CRAFT_MEMBER.booleanValue()).put("fte",
                        DBEERPEDIA.BROUWTOREN_FTE.decimalValue()));
    JSONAssert.assertEquals(expected.toString(), result, true);
  }

}
