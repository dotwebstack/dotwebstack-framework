package org.dotwebstack.framework.frontend.openapi.entity;

import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import javax.ws.rs.core.MediaType;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TupleEntityTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void constructor_ThrowsException_WithMissingSchemaMap() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TupleEntity(null, null,null,null,null,null);
  }

  @Test
  public void constructor_ThrowsException_WithMissingResult() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new TupleEntity(ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, mock(Property.class)), null,null,null,null,null);
  }

}
