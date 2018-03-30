package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.util.Map;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SchemaMapperUtilsTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void castLiteralValue_ThrowsException_ForNonLiteralValue() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Act
    SchemaMapperUtils.castLiteralValue(DBEERPEDIA.BROUWTOREN);
  }

  @Test
  public void castLiteralValue_ReturnsLiteral_ForLiteralValue() {
    // Act
    Literal result = SchemaMapperUtils.castLiteralValue(DBEERPEDIA.BROUWTOREN_NAME);

    // Assert
    assertThat(result, notNullValue());
    assertThat(result.getDatatype(), equalTo(XMLSchema.STRING));
  }

  @Test
  public void createLink_ReturnsLink_ForGivenUri() {
    // Arrange
    String uri = "http://foo";

    // Act
    Map<String, String> link = SchemaMapperUtils.createLink(URI.create("http://foo"));

    // Assert
    assertThat(link, equalTo(ImmutableMap.of("href", uri)));
  }

}
