package org.dotwebstack.framework.core.query.model.filter;

import static org.dotwebstack.framework.core.query.model.filter.FieldPathHelper.createFieldPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FieldPathHelperTest {

  @Test
  void createFieldPath_returnsFieldPath_forDefault() {
    var typeConfiguration = Mockito.mock(TypeConfiguration.class);

    var fieldConfiguration = Mockito.mock(AbstractFieldConfiguration.class);

    when(typeConfiguration.getField("identifier")).thenReturn(Optional.of(fieldConfiguration));

    FieldPath result = createFieldPath(typeConfiguration, "identifier");

    assertThat(result, notNullValue());
    assertThat(result.getFieldConfiguration(), equalTo(fieldConfiguration));
  }

  @Test
  @SuppressWarnings("unchecked")
  void createFieldPath_returnsFieldPath_forNested() {
    var breweryTypeConfiguration = Mockito.mock(TypeConfiguration.class);
    var breweryIdentifierFieldConfiguration = Mockito.mock(AbstractFieldConfiguration.class);
    when(breweryTypeConfiguration.getField("identifier")).thenReturn(Optional.of(breweryIdentifierFieldConfiguration));

    var beerTypeConfiguration = Mockito.mock(TypeConfiguration.class);

    var fieldConfiguration = Mockito.mock(AbstractFieldConfiguration.class);
    when(beerTypeConfiguration.getField("brewery")).thenReturn(Optional.of(fieldConfiguration));
    when(fieldConfiguration.getTypeConfiguration()).thenReturn(breweryTypeConfiguration);

    FieldPath result = createFieldPath(beerTypeConfiguration, "brewery.identifier");

    assertThat(result, notNullValue());
    assertThat(result.getFieldConfiguration(), equalTo(fieldConfiguration));
    assertThat(result.getChild(), notNullValue());
    assertThat(result.getChild()
        .getFieldConfiguration(), equalTo(breweryIdentifierFieldConfiguration));
  }

}
