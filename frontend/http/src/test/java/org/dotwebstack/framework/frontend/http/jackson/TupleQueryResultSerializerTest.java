package org.dotwebstack.framework.frontend.http.jackson;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

@RunWith(MockitoJUnitRunner.class)
public class TupleQueryResultSerializerTest {

  @Mock
  private TupleQueryResult tupleQueryResult;

  private ObjectMapper objectMapper;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapperProvider().getContext(TupleQueryResult.class);
  }

  @Test
  public void testEmptyQueryResult() throws IOException, JSONException {
    // Arrange
    when(tupleQueryResult.hasNext()).thenReturn(false);

    // Act
    String result = objectMapper.writeValueAsString(tupleQueryResult);

    // Assert
    JSONAssert.assertEquals("[]", result, true);
  }

  @Test
  public void testQueryResult() throws IOException, JSONException {
    // Arrange
    BindingSet bindingSet = new ListBindingSet(
        ImmutableList.of("identifier", "name", "yearOfFoundation", "craftMember", "fte"),
        DBEERPEDIA.BROUWTOREN, DBEERPEDIA.BROUWTOREN_NAME, DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION,
        DBEERPEDIA.BROUWTOREN_CRAFT_MEMBER, DBEERPEDIA.BROUWTOREN_FTE);
    when(tupleQueryResult.hasNext()).thenReturn(true, false);
    when(tupleQueryResult.next()).thenReturn(bindingSet);

    // Act
    String result = objectMapper.writeValueAsString(tupleQueryResult);

    // Assert
    JSONArray expected = new JSONArray().put(
        new JSONObject().put("identifier", DBEERPEDIA.BROUWTOREN.stringValue()).put("name",
            DBEERPEDIA.BROUWTOREN_NAME.stringValue()).put("yearOfFoundation",
                DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.shortValue()).put("craftMember",
                    DBEERPEDIA.BROUWTOREN_CRAFT_MEMBER.booleanValue()).put("fte",
                        DBEERPEDIA.BROUWTOREN_FTE.decimalValue()));
    JSONAssert.assertEquals(expected.toString(), result, true);
  }

}
