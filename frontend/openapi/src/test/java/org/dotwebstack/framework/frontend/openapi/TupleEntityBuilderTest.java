package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.frontend.openapi.properties.PropertyHandlerAdapter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TupleEntityBuilderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private TupleQueryResult tupleQueryResult;

  @Mock
  private PropertyHandlerAdapter propertyHandler;

  @Mock
  private BindingSet bindingSet;

  private TupleEntityBuilder tupleEntityBuilder;

  @Before
  public void setUp() {
    tupleEntityBuilder = new TupleEntityBuilder(propertyHandler);
  }

  @Test
  public void buildArrayWithObjects() {
    // Arrange
    ArrayProperty arrayProperty = new ArrayProperty();
    ObjectProperty itemProperty = new ObjectProperty();
    StringProperty nameProperty = new StringProperty();
    itemProperty.setProperties(ImmutableMap.of("name", nameProperty));
    arrayProperty.setItems(itemProperty);
    when(tupleQueryResult.hasNext()).thenReturn(true, false);
    when(tupleQueryResult.next()).thenReturn(bindingSet);
    when(bindingSet.hasBinding("name")).thenReturn(true);
    when(bindingSet.getValue("name")).thenReturn(DBEERPEDIA.BROUWTOREN_NAME);
    when(propertyHandler.handle(nameProperty, DBEERPEDIA.BROUWTOREN_NAME)).thenReturn(
        DBEERPEDIA.BROUWTOREN_NAME.stringValue());

    // Act
    Object entity = tupleEntityBuilder.build(tupleQueryResult, arrayProperty);

    // Arrange
    assertThat(entity, equalTo(
        ImmutableList.of(ImmutableMap.of("name", DBEERPEDIA.BROUWTOREN_NAME.stringValue()))));
  }


  @Test
  public void buildArrayWithNonObjects() {
    // Arrange
    ArrayProperty arrayProperty = new ArrayProperty();
    StringProperty itemProperty = new StringProperty();
    arrayProperty.setItems(itemProperty);

    // Assert
    thrown.expect(EntityBuilderRuntimeException.class);
    thrown.expectMessage("Only object properties are supported for array items.");

    // Act
    tupleEntityBuilder.build(tupleQueryResult, arrayProperty);
  }

  @Test
  public void buildNonArray() {
    // Arrange
    ObjectProperty objectProperty = new ObjectProperty();

    // Act
    Object entity = tupleEntityBuilder.build(tupleQueryResult, objectProperty);

    // Arrange
    assertThat(entity, equalTo(ImmutableMap.of()));
  }

}
