package org.dotwebstack.framework.service.openapi.conversion;

import org.dotwebstack.framework.service.openapi.OpenApiProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ConverterTest {

  @Mock
  private OpenApiProperties openApiProperties;

  @Mock
  private OpenApiProperties.DateFormatProperties dateFormatProperties;

  @Test
  void test_convertDate_toGivenFormat() {
    // Arrange
    String expected = "2012-12-31T14:59:59.999-07:00";
    when(openApiProperties.getDateproperties()).thenReturn(dateFormatProperties);
    when(dateFormatProperties.getDatetimeformat()).thenReturn("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");
    when(dateFormatProperties.getTimezone()).thenReturn("America/Denver");
    ZonedDateTimeTypeConverter converter = new ZonedDateTimeTypeConverter(openApiProperties);
    ZonedDateTime input = ZonedDateTime.parse("2012-12-31T23:59:59.999+02:00", DateTimeFormatter.ISO_DATE_TIME);

    // Act
    String actual = converter.convert(input, new HashMap<>());

    // Assert
    assertEquals(expected, actual);
  }

}
