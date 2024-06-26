package org.dotwebstack.framework.backend.postgres.codec;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.netty.buffer.Unpooled;
import io.r2dbc.postgresql.message.Format;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LocalDateCodecTest {

  private final LocalDateCodec localDateCodec = new LocalDateCodec(Set.of(1082));

  @ParameterizedTest
  @CsvSource({"1082, FORMAT_BINARY", "1082, FORMAT_TEXT"})
  void canDecode_returnsTrue_forSupportedDataType(int dataType, Format format) {
    var actual = localDateCodec.canDecode(dataType, format, Object.class);

    assertThat(actual, equalTo(true));
  }

  @ParameterizedTest
  @CsvSource({"10, FORMAT_BINARY", "10, FORMAT_BINARY"})
  void canDecode_returnsFalse_forUnsupportedDataType(int dataType, Format format) {
    var actual = localDateCodec.canDecode(dataType, format, Object.class);

    assertThat(actual, equalTo(false));
  }

  @ParameterizedTest
  @CsvSource({"2011, 01, 01", "20211, 01, 01", "-1001, 01, 01"})
  void decode_returnsDate_forValidTextFormattedDateString(String year, String month, String day) {
    var dateString = year.concat("-")
        .concat(month)
        .concat("-")
        .concat(day);
    var byteBuf = Unpooled.wrappedBuffer(dateString.getBytes());

    var actual = localDateCodec.decode(byteBuf, 1082, Format.FORMAT_TEXT, LocalDate.class);

    assertThat(actual, equalTo(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day))));
  }

  @ParameterizedTest
  @CsvSource({"2011, 01, 01", "20211, 01, 01", "-1001, 01, 01"})
  void decode_returnsDate_forValidBinaryFormattedDateString(String year, String month, String day) {
    var dateString = year.concat("-")
        .concat(month)
        .concat("-")
        .concat(day);
    var bytes = dateString.getBytes(StandardCharsets.UTF_8);
    var byteBuf = Unpooled.wrappedBuffer(bytes);

    var actual = localDateCodec.decode(byteBuf, 1082, Format.FORMAT_BINARY, LocalDate.class);

    assertThat(actual, equalTo(LocalDate.of(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day))));
  }

  @ParameterizedTest
  @CsvSource({"111-01-01", "2021-01-01 BC", "2021-01-01 AD", "2011-1-1"})
  void decode_throwsException_forInvalidDate(String date) {
    var bytes = date.getBytes(StandardCharsets.UTF_8);
    var byteBuf = Unpooled.wrappedBuffer(bytes);
    assertThrows(DateTimeParseException.class,
        () -> localDateCodec.decode(byteBuf, 1082, Format.FORMAT_TEXT, LocalDate.class));
  }
}
