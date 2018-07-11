package org.dotwebstack.framework.frontend.soap.wsdlreader;

class StringUtils {

  public static final String EMPTY = "";

  private StringUtils() {
    throw new IllegalStateException("Constructor of utility class StringUtils");
  }

  public static boolean isBlank(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return true;
    }
    for (int i = 0; i < strLen; i++) {
      if (! Character.isWhitespace(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static boolean isNotBlank(String str) {
    return !StringUtils.isBlank(str);
  }

  public static String join(Object[] array, String separator) {
    if (array == null) {
      return null;
    }
    return join(array, separator, 0, array.length);
  }

  public static String join(Object[] array, String separator, int startIndex, int endIndex) {
    if (array == null) {
      return null;
    }
    if (separator == null) {
      separator = EMPTY;
    }

    // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
    //       (Assuming that all Strings are roughly equally long)
    int bufSize = (endIndex - startIndex);
    if (bufSize <= 0) {
      return EMPTY;
    }

    bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length())
            + separator.length());

    StringBuilder buf = new StringBuilder(bufSize);

    for (int i = startIndex; i < endIndex; i++) {
      if (i > startIndex) {
        buf.append(separator);
      }
      if (array[i] != null) {
        buf.append(array[i]);
      }
    }
    return buf.toString();
  }

  // Remove one or more occurrences of the string in the second argument
  // from the start of the string in the first argument.
  public static String removeLeadingString(String string, String toRemove) {
    String newString = string;

    while (newString.startsWith(toRemove)) {
      newString = newString.substring(toRemove.length());
    }

    return newString;
  }
}
