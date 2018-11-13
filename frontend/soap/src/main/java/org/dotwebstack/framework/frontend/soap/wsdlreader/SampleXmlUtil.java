package org.dotwebstack.framework.frontend.soap.wsdlreader;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDurationBuilder;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDate;
import org.apache.xmlbeans.XmlDateTime;
import org.apache.xmlbeans.XmlDecimal;
import org.apache.xmlbeans.XmlDuration;
import org.apache.xmlbeans.XmlGDay;
import org.apache.xmlbeans.XmlGMonth;
import org.apache.xmlbeans.XmlGMonthDay;
import org.apache.xmlbeans.XmlGYear;
import org.apache.xmlbeans.XmlGYearMonth;
import org.apache.xmlbeans.XmlInteger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlTime;
import org.apache.xmlbeans.impl.schema.SchemaTypeImpl;
import org.apache.xmlbeans.impl.util.Base64;
import org.apache.xmlbeans.impl.util.HexBin;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class was extracted from the soapUI code base by centeractive ag in October 2011.
 * The main reason behind the extraction was to separate the code that is responsible
 * for the generation of the SOAP messages from the rest of the soapUI's code that is
 * tightly coupled with other modules, such as soapUI's graphical user interface, etc.
 * The goal was to create an open-source java project whose main responsibility is to
 * handle SOAP message generation and SOAP transmission purely on an XML level.
 * <br/>
 * centeractive ag would like to express strong appreciation to SmartBear Software and
 * to the whole team of soapUI's developers for creating soapUI and for releasing its
 * source code under a free and open-source licence. centeractive ag extracted and
 * modifies some parts of the soapUI's code in good faith, making every effort not
 * to impair any existing functionality and to supplement it according to our
 * requirements, applying best practices of software design.
 *
 * <p>Changes done:
 * - changing location in the package structure
 * - removal of dependencies and code parts that are out of scope of SOAP message generation
 * - minor fixes to make the class compile out of soapUI's code base
 * - introduction of SoapContext class
 *
 * <p>This class is further changed to be used as a template generator for the dotwebstack
 * Changes done:
 * - Added possibility to use annotations to add template instructions instead of sample values
 *
 */

/**
 * XmlBeans class for generating XML from XML Schemas
 */
class SampleXmlUtil {
  private static final Logger LOG = LoggerFactory.getLogger(SampleXmlUtil.class);

  private static final QName DWS_CURSOR_QNAME = new QName(Constants.DWS_NS, "cursor");
  private static final QName DWS_VALUES_FROM_QNAME = new QName(Constants.DWS_NS, "valuesFrom");
  private static final QName DWS_TYPE_FROM_QNAME = new QName(Constants.DWS_NS, "typeFrom");
  private static final QName DWS_GROUPBY_QNAME = new QName(Constants.DWS_NS, "groupBy");
  private static final QName DWS_GROUP_QNAME = new QName(Constants.DWS_NS, "group");

  private Random picker = new Random(1);

  private boolean soapEnc;
  private boolean exampleContent = false;
  private boolean typeComment = false;

  private boolean skipComments = false;
  private boolean ignoreOptional = false;

  /*
   * A list of XML-Schema types and global elements in the form of name@namespace which
   * will be excluded when generating sample requests and responses and input forms.
   * By default the XML-Schema root element is added since it is quite common in .NET
   * services and generates a sample xml fragment of about 300 kb!.
   */
  private Set<QName> excludedTypes = new HashSet<>();
  private SoapMultiValuesProvider multiValuesProvider;

  private ArrayList<SchemaType> typeStack = new ArrayList<>();

  private TupleQueryResult queryResult = null;
  private BindingSet currentRow = null;
  private String currentGroupingValue = null;

  public SampleXmlUtil(boolean soapEnc, SoapContext context,
      TupleQueryResult queryResult) {
    this.soapEnc = soapEnc;
    this.queryResult = queryResult;
    excludedTypes.addAll(context.getExcludedTypes());
    this.exampleContent = context.isExampleContent();
    this.typeComment = context.isTypeComments();
    this.skipComments = !context.isValueComments();
    this.ignoreOptional = !context.isBuildOptional();
    this.multiValuesProvider = context.getMultiValuesProvider();

    if (queryResult != null) {
      if (queryResult.hasNext()) {
        currentRow = queryResult.next();
        LOG.debug("Queryresult has at least one result");
      } else {
        LOG.debug("Queryresult is empty set");
      }
    } else {
      LOG.warn("No queryresult");
    }
  }

  public boolean isSoapEnc() {
    return soapEnc;
  }

  /**
   * Cursor position Before: <theElement>^</theElement> After:
   * <theElement>&lt;lots of stuff/&gt;^</theElement>
   */
  public void createSampleForType(SchemaAnnotation sannotation, SchemaType stype, XmlCursor xmlc) {
    QName nm = stype.getName();
    if (nm == null && stype.getContainerField() != null) {
      nm = stype.getContainerField().getName();
    }

    if (nm != null && excludedTypes.contains(nm)) {
      if (!skipComments) {
        xmlc.insertComment("Ignoring type [" + nm + "]");
      }
      return;
    }

    if (typeStack.contains(stype)) {
      return;
    }

    typeStack.add(stype);

    try {
      if (stype.isSimpleType() || stype.isURType()) {
        processSimpleType(sannotation, stype, xmlc);
        return;
      }

      // complex Type
      // <theElement>^</theElement>
      processAttributes(sannotation, stype, xmlc);

      // <theElement attri1="string">^</theElement>
      switch (stype.getContentType()) {
        case SchemaType.NOT_COMPLEX_TYPE:
        case SchemaType.EMPTY_CONTENT:
          // noop
          break;
        case SchemaType.SIMPLE_CONTENT:
          processSimpleType(sannotation, stype, xmlc);
          break;
        case SchemaType.MIXED_CONTENT:
          xmlc.insertChars(pick(WORDS) + " ");
          if (stype.getContentModel() != null) {
            processParticle(stype.getContentModel(), xmlc, true);
          }
          xmlc.insertChars(pick(WORDS));
          break;
        case SchemaType.ELEMENT_CONTENT:
          if (stype.getContentModel() != null) {
            processParticle(stype.getContentModel(), xmlc, false);
          }
          break;
        default:
          // Default added in order to avoid checkstyle violation.
          LOG.warn("Unhandled case encountered in SampleXmlUtil.createSampleForType");
          break;
      }
    } finally {
      typeStack.remove(typeStack.size() - 1);
    }
  }

  private void processSimpleType(SchemaAnnotation sannotation, SchemaType stype, XmlCursor xmlc) {

    if (soapEnc) {
      QName typeName = stype.getName();
      if (typeName != null) {
        xmlc.insertAttributeWithValue(XSI_TYPE, formatQName(xmlc, typeName));
      }
    }

    String sample = sampleDataForSimpleType(stype);

    //DOTWEBSTACK - Added
    if ((queryResult != null) && (sannotation != null)) {
      String variableName = SoapUtils.getAttributeValue(sannotation, DWS_VALUES_FROM_QNAME);
      if ((variableName != null) && (currentRow != null)) {
        if (currentRow.hasBinding(variableName)) {
          sample = currentRow.getValue(variableName).stringValue();
        } else {
          if (queryResult.getBindingNames().contains(variableName)) {
            //No value, but variable does exists, so probably nil
            xmlc.insertAttributeWithValue(XSI_NIL, "true");
            sample = "";
          } else {
            LOG.warn("Could not find binding variable: {}", variableName);
          }
        }
      }
    }

    xmlc.insertChars(sample);
  }

  @SuppressWarnings("squid:S3776")
  private String sampleDataForSimpleType(SchemaType schemaType) {
    String result = null;

    // swaRef
    if (schemaType.getName() != null) {
      if (schemaType.getName().equals(new QName("http://ws-i.org/profiles/basic/1.1/xsd", "swaRef"))) {
        return "cid:" + (long) (System.currentTimeMillis() * Math.random());
      }

      // xmime base64
      if (schemaType.getName().equals(new QName(Constants.XMLMIME_NS, "base64Binary"))) {
        return "cid:" + (long) (System.currentTimeMillis() * Math.random());
      }

      // xmime hexBinary
      if (schemaType.getName().equals(new QName(Constants.XMLMIME_NS, "hexBinary"))) {
        return "cid:" + (long) (System.currentTimeMillis() * Math.random());
      }
    }

    SchemaType primitiveType = schemaType.getPrimitiveType();
    if (primitiveType != null
        && (primitiveType.getBuiltinTypeCode() == SchemaType.BTC_BASE_64_BINARY || primitiveType
        .getBuiltinTypeCode() == SchemaType.BTC_HEX_BINARY)) {
      return "cid:" + (long) (System.currentTimeMillis() * Math.random());
    }

    if (!exampleContent) {
      return "?";
    }

    if (XmlObject.type.equals(schemaType)) {
      return "anyType";
    }

    if (XmlAnySimpleType.type.equals(schemaType)) {
      return "anySimpleType";
    }

    if (schemaType.getSimpleVariety() == SchemaType.LIST) {
      SchemaType itemType = schemaType.getListItemType();
      StringBuilder stringBuilder = new StringBuilder();
      int length = pickLength(schemaType);
      if (length > 0) {
        stringBuilder.append(sampleDataForSimpleType(itemType));
      }

      for (int i = 1; i < length; i += 1) {
        stringBuilder.append(' ');
        stringBuilder.append(sampleDataForSimpleType(itemType));
      }

      return stringBuilder.toString();
    }

    if (schemaType.getSimpleVariety() == SchemaType.UNION) {
      SchemaType[] possibleTypes = schemaType.getUnionConstituentTypes();
      if (possibleTypes.length == 0) {
        return "";
      }
      return sampleDataForSimpleType(possibleTypes[pick(possibleTypes.length)]);
    }

    XmlAnySimpleType[] enumValues = schemaType.getEnumerationValues();
    if (enumValues != null && enumValues.length > 0) {
      return enumValues[pick(enumValues.length)].getStringValue();
    }

    // Return an empty string if there is no primitive type.
    if (primitiveType == null) {
      return "";
    }

    switch (primitiveType.getBuiltinTypeCode()) {
      default:
      case SchemaType.BTC_NOT_BUILTIN:
        return "";

      case SchemaType.BTC_ANY_TYPE:
      case SchemaType.BTC_ANY_SIMPLE:
        return "anything";

      case SchemaType.BTC_BOOLEAN:
        return pick(2) == 0 ? "true" : "false";

      case SchemaType.BTC_BASE_64_BINARY:
        try {
          result = new String(
              Base64.encode(formatToLength(pick(WORDS), schemaType).getBytes("utf-8")));
        } catch (java.io.UnsupportedEncodingException e) {
          LOG.warn(
              "Caught UnsupportedEncodingException in SampleXmlUtil.sampleDataForSimpleType");
        }
        return result;

      case SchemaType.BTC_HEX_BINARY:
        return HexBin.encode(formatToLength(pick(WORDS), schemaType));

      case SchemaType.BTC_ANY_URI:
        return formatToLength("http://www." + pick(DNS1) + "." + pick(DNS2) + "/" + pick(WORDS) + "/"
            + pick(WORDS), schemaType);

      case SchemaType.BTC_QNAME:
        return formatToLength("qname", schemaType);

      case SchemaType.BTC_NOTATION:
        return formatToLength("notation", schemaType);

      case SchemaType.BTC_FLOAT:
        return "1.25";

      case SchemaType.BTC_DOUBLE:
        return "1.30";

      case SchemaType.BTC_DECIMAL:
        switch (closestBuiltin(schemaType).getBuiltinTypeCode()) {
          case SchemaType.BTC_SHORT:
            return formatDecimal("1", schemaType);
          case SchemaType.BTC_UNSIGNED_SHORT:
            return formatDecimal("5", schemaType);
          case SchemaType.BTC_BYTE:
            return formatDecimal("2", schemaType);
          case SchemaType.BTC_UNSIGNED_BYTE:
            return formatDecimal("6", schemaType);
          case SchemaType.BTC_INT:
            return formatDecimal("3", schemaType);
          case SchemaType.BTC_UNSIGNED_INT:
            return formatDecimal("7", schemaType);
          case SchemaType.BTC_LONG:
            return formatDecimal("10", schemaType);
          case SchemaType.BTC_UNSIGNED_LONG:
            return formatDecimal("11", schemaType);
          case SchemaType.BTC_INTEGER:
            return formatDecimal("100", schemaType);
          case SchemaType.BTC_NON_POSITIVE_INTEGER:
            return formatDecimal("-200", schemaType);
          case SchemaType.BTC_NEGATIVE_INTEGER:
            return formatDecimal("-201", schemaType);
          case SchemaType.BTC_NON_NEGATIVE_INTEGER:
            return formatDecimal("200", schemaType);
          case SchemaType.BTC_POSITIVE_INTEGER:
            return formatDecimal("201", schemaType);
          default:
          case SchemaType.BTC_DECIMAL:
            return formatDecimal("1000.00", schemaType);
        }

      case SchemaType.BTC_STRING:
        result = pick(WORDS, picker.nextInt(3));
        return formatToLength(result, schemaType);

      case SchemaType.BTC_DURATION:
        return formatDuration(schemaType);

      case SchemaType.BTC_DATE_TIME:
      case SchemaType.BTC_TIME:
      case SchemaType.BTC_DATE:
      case SchemaType.BTC_G_YEAR_MONTH:
      case SchemaType.BTC_G_YEAR:
      case SchemaType.BTC_G_MONTH_DAY:
      case SchemaType.BTC_G_DAY:
      case SchemaType.BTC_G_MONTH:
        return formatDate(schemaType);

    }
  }

  // a bit from the Aenid
  protected static final String[] WORDS = new String[]{"ipsa", "iovis",
      "rapidum", "iaculata", "e", "nubibus", "ignem",
      "disiecitque", "rates", "evertitque", "aequora",
      "ventis", "illum", "exspirantem", "transfixo", "pectore",
      "flammas", "turbine", "corripuit", "scopuloque", "infixit",
      "acuto", "ast", "ego", "quae", "divum", "incedo",
      "regina", "iovisque", "et", "soror", "et", "coniunx",
      "una", "cum", "gente", "tot", "annos", "bella", "gero",
      "et", "quisquam", "numen", "iunonis", "adorat", "praeterea",
      "aut", "supplex", "aris", "imponet", "honorem",
      "talia", "flammato", "secum", "dea", "corde", "volutans",
      "nimborum", "in", "patriam", "loca", "feta",
      "furentibus", "austris", "aeoliam", "venit", "hic",
      "vasto", "rex", "aeolus", "antro", "luctantis", "ventos",
      "tempestatesque", "sonoras", "imperio", "premit", "ac",
      "vinclis", "et", "carcere", "frenat", "illi",
      "indignantes", "magno", "cum", "murmure", "montis",
      "circum", "claustra", "fremunt", "celsa", "sedet",
      "aeolus", "arce", "sceptra", "tenens", "mollitque",
      "animos", "et", "temperat", "iras", "ni", "faciat",
      "maria", "ac", "terras", "caelumque", "profundum",
      "quippe", "ferant", "rapidi", "secum", "verrantque", "per",
      "auras", "sed", "pater", "omnipotens", "speluncis",
      "abdidit", "atris", "hoc", "metuens", "molemque", "et",
      "montis", "insuper", "altos", "imposuit", "regemque",
      "dedit", "qui", "foedere", "certo", "et", "premere",
      "et", "laxas", "sciret", "dare", "iussus", "habenas",};

  private static final String[] DNS1 = new String[]{"corp",
      "your", "my", "sample", "company", "test", "any"};
  private static final String[] DNS2 = new String[]{"com",
      "org", "com", "gov", "org", "com", "org", "com", "edu"};

  private int pick(int n) {
    return picker.nextInt(n);
  }

  private String pick(String[] a) {
    return a[pick(a.length)];
  }

  private String pick(String[] a, int count) {
    if (count <= 0) {
      count = 1;
    }

    int i = pick(a.length);
    StringBuilder stringBuilder = new StringBuilder(a[i]);
    while (count-- > 0) {
      i += 1;
      if (i >= a.length) {
        i = 0;
      }
      stringBuilder.append(' ');
      stringBuilder.append(a[i]);
    }

    return stringBuilder.toString();
  }

  private int pickLength(SchemaType schemaType) {
    XmlInteger length = (XmlInteger) schemaType.getFacet(SchemaType.FACET_LENGTH);
    if (length != null) {
      return length.getBigIntegerValue().intValue();
    }

    XmlInteger min = (XmlInteger) schemaType.getFacet(SchemaType.FACET_MIN_LENGTH);
    XmlInteger max = (XmlInteger) schemaType.getFacet(SchemaType.FACET_MAX_LENGTH);
    int minInt;
    int maxInt;

    if (min == null) {
      minInt = 0;
    } else {
      minInt = min.getBigIntegerValue().intValue();
    }
    if (max == null) {
      maxInt = Integer.MAX_VALUE;
    } else {
      maxInt = max.getBigIntegerValue().intValue();
    }
    // We try to keep the length of the array within reasonable limits,
    // at least 1 item and at most 3 if possible
    if (minInt == 0 && maxInt >= 1) {
      minInt = 1;
    }
    if (maxInt > minInt + 2) {
      maxInt = minInt + 2;
    }
    if (maxInt < minInt) {
      maxInt = minInt;
    }
    return minInt + pick(maxInt - minInt);
  }

  /**
   * Formats a given string to the required length by appending the source string to itself.
   * Then truncate the result, if necessary, to keep it within maxLength.
   */
  private String formatToLength(String string, SchemaType schemaType) {
    int minLength = string.length();
    int maxLength = minLength;

    try {
      SimpleValue min = (SimpleValue) schemaType.getFacet(SchemaType.FACET_LENGTH);
      if (min == null) {
        min = (SimpleValue) schemaType.getFacet(SchemaType.FACET_MIN_LENGTH);
      }
      if (min != null) {
        minLength = min.getIntValue();
      }

      // Determine the maximum length of the result. If there is no maximum,
      // assume that it is the same as the minumum.
      SimpleValue max = (SimpleValue) schemaType.getFacet(SchemaType.FACET_LENGTH);
      if (max != null) {
        max = (SimpleValue) schemaType.getFacet(SchemaType.FACET_MAX_LENGTH);
      }
      if (max != null) {
        maxLength = max.getIntValue();
      }
    } catch (Exception e) { // intValue can be out of range
    }

    return StringUtils.fill(string, string, minLength, maxLength);
  }

  @SuppressWarnings("squid:S3776")
  private String formatDecimal(String start, SchemaType schemaType) {
    XmlDecimal xmlD;
    xmlD = (XmlDecimal) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
    BigDecimal min = xmlD != null ? xmlD.getBigDecimalValue() : null;
    xmlD = (XmlDecimal) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
    BigDecimal max = xmlD != null ? xmlD.getBigDecimalValue() : null;
    boolean minInclusive = true;
    boolean maxInclusive = true;

    xmlD = (XmlDecimal) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
    if (xmlD != null) {
      BigDecimal minExcl = xmlD.getBigDecimalValue();
      if (min == null || min.compareTo(minExcl) < 0) {
        min = minExcl;
        minInclusive = false;
      }
    }
    xmlD = (XmlDecimal) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
    if (xmlD != null) {
      BigDecimal maxExcl = xmlD.getBigDecimalValue();
      if (max == null || max.compareTo(maxExcl) > 0) {
        max = maxExcl;
        maxInclusive = false;
      }
    }

    xmlD = (XmlDecimal) schemaType.getFacet(SchemaType.FACET_TOTAL_DIGITS);
    int totalDigits = -1;
    if (xmlD != null) {
      totalDigits = xmlD.getBigDecimalValue().intValue();

      StringBuilder stringBuilder = new StringBuilder(totalDigits);
      for (int i = 0; i < totalDigits; i++) {
        stringBuilder.append('9');
      }

      BigDecimal digitsLimit = new BigDecimal(stringBuilder.toString());
      if (max != null && max.compareTo(digitsLimit) > 0) {
        max = digitsLimit;
        maxInclusive = true;
      }

      digitsLimit = digitsLimit.negate();
      if (min != null && min.compareTo(digitsLimit) < 0) {
        min = digitsLimit;
        minInclusive = true;
      }
    }

    BigDecimal result = new BigDecimal(start);
    int sigMin = min == null ? 1 : result.compareTo(min);
    int sigMax = max == null ? -1 : result.compareTo(max);
    boolean minOk = sigMin > 0 || sigMin == 0 && minInclusive;
    boolean maxOk = sigMax < 0 || sigMax == 0 && maxInclusive;

    // Compute the minimum increment
    xmlD = (XmlDecimal) schemaType.getFacet(SchemaType.FACET_FRACTION_DIGITS);
    int fractionDigits = -1;
    BigDecimal increment;
    if (xmlD == null) {
      increment = new BigDecimal(1);
    } else {
      fractionDigits = xmlD.getBigDecimalValue().intValue();
      if (fractionDigits > 0) {
        StringBuilder stringBuilder = new StringBuilder("0.");
        for (int i = 1; i < fractionDigits; i++) {
          stringBuilder.append('0');
        }

        stringBuilder.append('1');
        increment = new BigDecimal(stringBuilder.toString());
      } else {
        increment = new BigDecimal(1);
      }
    }

    if (minOk && maxOk) {
      // OK
    } else if (minOk && !maxOk) {
      // TOO BIG
      if (maxInclusive) {
        result = max;
      } else {
        result = max.subtract(increment);
      }
    } else if (!minOk && maxOk) {
      // TOO SMALL
      if (minInclusive) {
        result = min;
      } else {
        result = min.add(increment);
      }
    } else {
      // MIN > MAX!!
    }

    // We have the number in the BigDecimal variable result.
    // According to Sonarqube, result can be null here.
    if (result == null) {
      return "";
    }

    // Adjust the scale according to the totalDigits and fractionDigits
    int digits = 0;
    BigDecimal one = new BigDecimal(BigInteger.ONE);
    BigDecimal n = result;
    while (n.abs().compareTo(one) >= 0) {
      n = n.movePointLeft(1);
      digits++;
    }

    if (fractionDigits > 0) {
      if (totalDigits >= 0) {
        result = result.setScale(Math.max(fractionDigits, totalDigits - digits));
      } else {
        result = result.setScale(fractionDigits);
      }
    } else {
      if (fractionDigits == 0) {
        result = result.setScale(0);
      }
    }

    return result.toString();
  }

  @SuppressWarnings("squid:S3776")
  private String formatDuration(SchemaType schemaType) {
    XmlDuration d = (XmlDuration) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
    GDuration minInclusive = null;
    if (d != null) {
      minInclusive = d.getGDurationValue();
    }

    d = (XmlDuration) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
    GDuration maxInclusive = null;
    if (d != null) {
      maxInclusive = d.getGDurationValue();
    }

    d = (XmlDuration) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
    GDuration minExclusive = null;
    if (d != null) {
      minExclusive = d.getGDurationValue();
    }

    d = (XmlDuration) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
    GDuration maxExclusive = null;
    if (d != null) {
      maxExclusive = d.getGDurationValue();
    }

    GDurationBuilder gdurb = new GDurationBuilder();
    gdurb.setSecond(pick(800000));
    gdurb.setMonth(pick(20));

    // Years
    // Months
    // Days
    // Hours
    // Minutes
    // Seconds
    // Fractions
    if (minInclusive != null) {
      if (gdurb.getYear() < minInclusive.getYear()) {
        gdurb.setYear(minInclusive.getYear());
      }
      if (gdurb.getMonth() < minInclusive.getMonth()) {
        gdurb.setMonth(minInclusive.getMonth());
      }
      if (gdurb.getDay() < minInclusive.getDay()) {
        gdurb.setDay(minInclusive.getDay());
      }
      if (gdurb.getHour() < minInclusive.getHour()) {
        gdurb.setHour(minInclusive.getHour());
      }
      if (gdurb.getMinute() < minInclusive.getMinute()) {
        gdurb.setMinute(minInclusive.getMinute());
      }
      if (gdurb.getSecond() < minInclusive.getSecond()) {
        gdurb.setSecond(minInclusive.getSecond());
      }
      if (gdurb.getFraction().compareTo(minInclusive.getFraction()) < 0) {
        gdurb.setFraction(minInclusive.getFraction());
      }
    }

    if (maxInclusive != null) {
      if (gdurb.getYear() > maxInclusive.getYear()) {
        gdurb.setYear(maxInclusive.getYear());
      }
      if (gdurb.getMonth() > maxInclusive.getMonth()) {
        gdurb.setMonth(maxInclusive.getMonth());
      }
      if (gdurb.getDay() > maxInclusive.getDay()) {
        gdurb.setDay(maxInclusive.getDay());
      }
      if (gdurb.getHour() > maxInclusive.getHour()) {
        gdurb.setHour(maxInclusive.getHour());
      }
      if (gdurb.getMinute() > maxInclusive.getMinute()) {
        gdurb.setMinute(maxInclusive.getMinute());
      }
      if (gdurb.getSecond() > maxInclusive.getSecond()) {
        gdurb.setSecond(maxInclusive.getSecond());
      }
      if (gdurb.getFraction().compareTo(maxInclusive.getFraction()) > 0) {
        gdurb.setFraction(maxInclusive.getFraction());
      }
    }

    if (minExclusive != null) {
      if (gdurb.getYear() <= minExclusive.getYear()) {
        gdurb.setYear(minExclusive.getYear() + 1);
      }
      if (gdurb.getMonth() <= minExclusive.getMonth()) {
        gdurb.setMonth(minExclusive.getMonth() + 1);
      }
      if (gdurb.getDay() <= minExclusive.getDay()) {
        gdurb.setDay(minExclusive.getDay() + 1);
      }
      if (gdurb.getHour() <= minExclusive.getHour()) {
        gdurb.setHour(minExclusive.getHour() + 1);
      }
      if (gdurb.getMinute() <= minExclusive.getMinute()) {
        gdurb.setMinute(minExclusive.getMinute() + 1);
      }
      if (gdurb.getSecond() <= minExclusive.getSecond()) {
        gdurb.setSecond(minExclusive.getSecond() + 1);
      }
      if (gdurb.getFraction().compareTo(minExclusive.getFraction()) <= 0) {
        gdurb.setFraction(minExclusive.getFraction().add(BigDecimal.valueOf(0.001)));
      }
    }

    if (maxExclusive != null) {
      if (gdurb.getYear() > maxExclusive.getYear()) {
        gdurb.setYear(maxExclusive.getYear());
      }
      if (gdurb.getMonth() > maxExclusive.getMonth()) {
        gdurb.setMonth(maxExclusive.getMonth());
      }
      if (gdurb.getDay() > maxExclusive.getDay()) {
        gdurb.setDay(maxExclusive.getDay());
      }
      if (gdurb.getHour() > maxExclusive.getHour()) {
        gdurb.setHour(maxExclusive.getHour());
      }
      if (gdurb.getMinute() > maxExclusive.getMinute()) {
        gdurb.setMinute(maxExclusive.getMinute());
      }
      if (gdurb.getSecond() > maxExclusive.getSecond()) {
        gdurb.setSecond(maxExclusive.getSecond());
      }
      if (gdurb.getFraction().compareTo(maxExclusive.getFraction()) > 0) {
        gdurb.setFraction(maxExclusive.getFraction());
      }
    }

    gdurb.normalize();
    return gdurb.toString();
  }

  @SuppressWarnings("squid:S3776")
  private String formatDate(SchemaType schemaType) {
    GDateBuilder gdateb = new GDateBuilder(new Date(1000L * pick(365 * 24 * 60 * 60)
        + (30L + pick(20)) * 365 * 24 * 60 * 60 * 1000));
    GDate min = null;
    GDate max = null;

    // Find the min and the max according to the type
    switch (schemaType.getPrimitiveType().getBuiltinTypeCode()) {
      case SchemaType.BTC_DATE_TIME:
        XmlDateTime xmlDateTime = (XmlDateTime) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (xmlDateTime != null) {
          min = xmlDateTime.getGDateValue();
        }

        xmlDateTime = (XmlDateTime) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if ((xmlDateTime != null)
            && (min == null || min.compareToGDate(xmlDateTime.getGDateValue()) <= 0)) {
          min = xmlDateTime.getGDateValue();
        }

        xmlDateTime = (XmlDateTime) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (xmlDateTime != null) {
          max = xmlDateTime.getGDateValue();
        }

        xmlDateTime = (XmlDateTime) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if ((xmlDateTime != null)
            && (max == null || max.compareToGDate(xmlDateTime.getGDateValue()) >= 0)) {
          max = xmlDateTime.getGDateValue();
        }

        break;

      case SchemaType.BTC_TIME:
        XmlTime xmlTime = (XmlTime) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (xmlTime != null) {
          min = xmlTime.getGDateValue();
        }

        xmlTime = (XmlTime) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if ((xmlTime != null)
            && (min == null || min.compareToGDate(xmlTime.getGDateValue()) <= 0)) {
          min = xmlTime.getGDateValue();
        }

        xmlTime = (XmlTime) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (xmlTime != null) {
          max = xmlTime.getGDateValue();
        }

        xmlTime = (XmlTime) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if ((xmlTime != null)
            && (max == null || max.compareToGDate(xmlTime.getGDateValue()) >= 0)) {
          max = xmlTime.getGDateValue();
        }

        break;

      case SchemaType.BTC_DATE:
        XmlDate xmlDate = (XmlDate) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (xmlDate != null) {
          min = xmlDate.getGDateValue();
        }

        xmlDate = (XmlDate) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if ((xmlDate != null)
            && (min == null || min.compareToGDate(xmlDate.getGDateValue()) <= 0)) {
          min = xmlDate.getGDateValue();
        }

        xmlDate = (XmlDate) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (xmlDate != null) {
          max = xmlDate.getGDateValue();
        }

        xmlDate = (XmlDate) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if ((xmlDate != null)
            && (max == null || max.compareToGDate(xmlDate.getGDateValue()) >= 0)) {
          max = xmlDate.getGDateValue();
        }

        break;

      case SchemaType.BTC_G_YEAR_MONTH:
        XmlGYearMonth xmlGYearMonth =
            (XmlGYearMonth) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (xmlGYearMonth != null) {
          min = xmlGYearMonth.getGDateValue();
        }

        xmlGYearMonth = (XmlGYearMonth) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if ((xmlGYearMonth != null)
            && (min == null || min.compareToGDate(xmlGYearMonth.getGDateValue()) <= 0)) {
          min = xmlGYearMonth.getGDateValue();
        }

        xmlGYearMonth = (XmlGYearMonth) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (xmlGYearMonth != null) {
          max = xmlGYearMonth.getGDateValue();
        }

        xmlGYearMonth = (XmlGYearMonth) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if ((xmlGYearMonth != null)
            && (max == null || max.compareToGDate(xmlGYearMonth.getGDateValue()) >= 0)) {
          max = xmlGYearMonth.getGDateValue();
        }

        break;

      case SchemaType.BTC_G_YEAR:
        XmlGYear xmlGYear = (XmlGYear) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (xmlGYear != null) {
          min = xmlGYear.getGDateValue();
        }

        xmlGYear = (XmlGYear) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if ((xmlGYear != null)
            && (min == null || min.compareToGDate(xmlGYear.getGDateValue()) <= 0)) {
          min = xmlGYear.getGDateValue();
        }

        xmlGYear = (XmlGYear) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (xmlGYear != null) {
          max = xmlGYear.getGDateValue();
        }

        xmlGYear = (XmlGYear) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if ((xmlGYear != null)
            && (max == null || max.compareToGDate(xmlGYear.getGDateValue()) >= 0)) {
          max = xmlGYear.getGDateValue();
        }

        break;

      case SchemaType.BTC_G_MONTH_DAY:
        XmlGMonthDay xmlGMonthDay =
            (XmlGMonthDay) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (xmlGMonthDay != null) {
          min = xmlGMonthDay.getGDateValue();
        }

        xmlGMonthDay = (XmlGMonthDay) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if ((xmlGMonthDay != null)
            && (min == null || min.compareToGDate(xmlGMonthDay.getGDateValue()) <= 0)) {
          min = xmlGMonthDay.getGDateValue();
        }

        xmlGMonthDay = (XmlGMonthDay) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (xmlGMonthDay != null) {
          max = xmlGMonthDay.getGDateValue();
        }

        xmlGMonthDay = (XmlGMonthDay) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if ((xmlGMonthDay != null)
            && (max == null || max.compareToGDate(xmlGMonthDay.getGDateValue()) >= 0)) {
          max = xmlGMonthDay.getGDateValue();
        }
        break;

      case SchemaType.BTC_G_DAY:
        XmlGDay xmlGDay = (XmlGDay) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (xmlGDay != null) {
          min = xmlGDay.getGDateValue();
        }

        xmlGDay = (XmlGDay) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if ((xmlGDay != null)
            && (min == null || min.compareToGDate(xmlGDay.getGDateValue()) <= 0)) {
          min = xmlGDay.getGDateValue();
        }

        xmlGDay = (XmlGDay) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (xmlGDay != null) {
          max = xmlGDay.getGDateValue();
        }

        xmlGDay = (XmlGDay) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if ((xmlGDay != null)
            && (max == null || max.compareToGDate(xmlGDay.getGDateValue()) >= 0)) {
          max = xmlGDay.getGDateValue();
        }

        break;

      case SchemaType.BTC_G_MONTH:
        XmlGMonth xmlGMonth = (XmlGMonth) schemaType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (xmlGMonth != null) {
          min = xmlGMonth.getGDateValue();
        }

        xmlGMonth = (XmlGMonth) schemaType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if ((xmlGMonth != null)
            && (min == null || min.compareToGDate(xmlGMonth.getGDateValue()) <= 0)) {
          min = xmlGMonth.getGDateValue();
        }

        xmlGMonth = (XmlGMonth) schemaType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (xmlGMonth != null) {
          max = xmlGMonth.getGDateValue();
        }

        xmlGMonth = (XmlGMonth) schemaType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if ((xmlGMonth != null)
            && (max == null || max.compareToGDate(xmlGMonth.getGDateValue()) >= 0)) {
          max = xmlGMonth.getGDateValue();
        }

        break;

      default:
        // Default added in order to avoid checkstyle violation.
        LOG.warn("Unhandled case encountered in SampleXmlUtil.formatDate");
        break;
    }

    if (min != null && max == null) {
      if (min.compareToGDate(gdateb) >= 0) {
        // Reset the date to min + (1-8) hours
        Calendar c = gdateb.getCalendar();
        c.add(Calendar.HOUR_OF_DAY, pick(8));
        gdateb = new GDateBuilder(c);
      }
    } else if (min == null && max != null) {
      if (max.compareToGDate(gdateb) <= 0) {
        // Reset the date to max - (1-8) hours
        Calendar c = gdateb.getCalendar();
        c.add(Calendar.HOUR_OF_DAY, 0 - pick(8));
        gdateb = new GDateBuilder(c);
      }
    } else if (((min != null) && (max != null))
        && ((min.compareToGDate(gdateb) >= 0) || (max.compareToGDate(gdateb) <= 0))) {
      // Find a date between the two
      Calendar c = min.getCalendar();
      Calendar cmax = max.getCalendar();
      c.add(Calendar.HOUR_OF_DAY, 1);
      if (c.after(cmax)) {
        c.add(Calendar.HOUR_OF_DAY, -1);
        c.add(Calendar.MINUTE, 1);
        if (c.after(cmax)) {
          c.add(Calendar.MINUTE, -1);
          c.add(Calendar.SECOND, 1);
          if (c.after(cmax)) {
            c.add(Calendar.SECOND, -1);
            c.add(Calendar.MILLISECOND, 1);
            if (c.after(cmax)) {
              c.add(Calendar.MILLISECOND, -1);
            }
          }
        }
      }

      gdateb = new GDateBuilder(c);
    }

    gdateb.setBuiltinTypeCode(schemaType.getPrimitiveType().getBuiltinTypeCode());
    if (pick(2) == 0) {
      gdateb.clearTimeZone();
    }
    return gdateb.toString();
  }

  private SchemaType closestBuiltin(SchemaType schemaType) {
    while (!schemaType.isBuiltinType()) {
      schemaType = schemaType.getBaseType();
    }
    return schemaType;
  }

  /**
   * Cursor position: Before this call: <outer><foo/>^</outer> (cursor at the
   * ^) After this call: <<outer><foo/><bar/>som text<etc/>^</outer>
   */
  private void processParticle(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
    int loop = determineLoopForQuery(determineMinMaxForSample(sp, xmlc), sp, false);

    while (loop-- > 0) {
      switch (sp.getParticleType()) {
        case (SchemaParticle.ELEMENT):
          processElement(sp, xmlc);
          break;
        case (SchemaParticle.SEQUENCE):
          processSequence(sp, xmlc, mixed);
          break;
        case (SchemaParticle.CHOICE):
          processChoice(sp, xmlc, mixed);
          break;
        case (SchemaParticle.ALL):
          processAll(sp, xmlc, mixed);
          break;
        case (SchemaParticle.WILDCARD):
          processWildCard(xmlc);
          break;
        default:
          LOG.warn("Unhandled case encountered in SampleXmlUtil.processParticle");
          break;
      }

      //DOTWEBSTACK added - Check if we want to continue with this element
      loop = determineLoopForQuery(loop, sp, true);
    }
  }

  private int determineMinMaxForSample(SchemaParticle sp, XmlCursor xmlc) {
    int minOccurs = sp.getIntMinOccurs();
    int maxOccurs = sp.getIntMaxOccurs();

    if (minOccurs == maxOccurs) {
      return minOccurs;
    }

    if (minOccurs == 0 && ignoreOptional) {
      return 0;
    }

    int result = minOccurs;
    if (result == 0) {
      result = 1;
    }

    if (sp.getParticleType() != SchemaParticle.ELEMENT) {
      return result;
    }

    // it probably only makes sense to put comments in front of individual
    // elements that repeat

    if (!skipComments) {
      if (sp.getMaxOccurs() == null) {
        if (minOccurs == 0) {
          xmlc.insertComment("Zero or more repetitions:");
        } else {
          xmlc.insertComment(minOccurs + " or more repetitions:");
        }
      } else if (sp.getIntMaxOccurs() > 1) {
        xmlc.insertComment(minOccurs + " to " + sp.getMaxOccurs() + " repetitions:");
      } else {
        xmlc.insertComment("Optional:");
      }
    }

    return result;
  }

  private void processElement(SchemaParticle sp, XmlCursor xmlc) {
    // cast as schema local element
    SchemaLocalElement element = (SchemaLocalElement) sp;

    // Add comment about type
    addElementTypeAndRestricionsComment(element, xmlc);

    // / ^ -> <elemenname></elem>^
    if (soapEnc) {
      xmlc.insertElement(element.getName().getLocalPart()); // test
      // encoded?
      // drop
      // namespaces.
    } else {
      xmlc.insertElement(element.getName().getLocalPart(), element.getName().getNamespaceURI());
      // / -> <elem>^</elem>
    }

    xmlc.toPrevToken();
    // -> <elem>stuff^</elem>

    String[] values = null;
    if (multiValuesProvider != null) {
      values = multiValuesProvider.getMultiValues(element.getName()).toArray(new String[]{});
    }
    if (values != null) {
      xmlc.insertChars(StringUtils.join(values, "s"));
    } else if (sp.isDefault()) {
      xmlc.insertChars(sp.getDefaultText());
    } else {
      createSampleForType(element.getAnnotation(), element.getType(), xmlc);
    }
    // -> <elem>stuff</elem>^
    xmlc.toNextToken();
  }

  //DOTWEBSTACK - Added: check if element is start of iteration, in such a case we need to continue
  @SuppressWarnings("squid:S3776")
  private int determineLoopForQuery(int currentLoop, SchemaParticle sp, boolean next) {
    // Stop if the schema particle is not an element.
    if (sp.getParticleType() != SchemaParticle.ELEMENT) {
      return currentLoop;
    }

    // cast as schema local element
    SchemaLocalElement element = (SchemaLocalElement) sp;

    SchemaAnnotation annotation = element.getAnnotation();

    // Stop if there is no annotation or query result.
    if (queryResult == null || annotation == null) {
      return currentLoop;
    }

    if (SoapUtils.hasAttribute(annotation, DWS_GROUP_QNAME)) {
      if (currentRow == null) {
        return 0;
      } else {
        return 1;
      }
    }

    if (SoapUtils.hasAttribute(annotation, DWS_CURSOR_QNAME)) {
      if (next) {
        if (queryResult.hasNext()) {
          currentRow = queryResult.next();
          String groupingVariable = SoapUtils.getAttributeValue(annotation, DWS_GROUPBY_QNAME);
          if ((groupingVariable != null) && currentRow.hasBinding(groupingVariable)) {
            String newGroupingValue = currentRow.getValue(groupingVariable).stringValue();
            if (currentGroupingValue == null) {
              currentGroupingValue = newGroupingValue;
              return 1;
            } else {
              if (newGroupingValue.equals(currentGroupingValue)) {
                return 1;
              } else {
                currentGroupingValue = newGroupingValue;
                return 0;
              }
            }
          }
          return 1;
        } else {
          currentRow = null;
          return 0;
        }
      } else {
        if (currentRow == null) {
          return 0;
        } else {
          return 1;
        }
      }
    }

    return currentLoop;
  }

  private static final String formatQName(XmlCursor xmlc, QName qualifiedName) {
    XmlCursor parent = xmlc.newCursor();
    parent.toParent();
    String prefix = parent.prefixForNamespace(qualifiedName.getNamespaceURI());
    parent.dispose();
    String name;
    if (prefix == null || prefix.length() == 0) {
      name = qualifiedName.getLocalPart();
    } else {
      name = prefix + ":" + qualifiedName.getLocalPart();
    }
    return name;
  }

  private static final QName HREF = new QName("href");
  private static final QName ID = new QName("id");
  public static final QName XSI_TYPE = new QName(Constants.XSI_NS, "type");
  public static final QName XSI_NIL = new QName(Constants.XSI_NS, "nil");
  public static final QName ENC_ARRAYTYPE = new QName("http://schemas.xmlsoap.org/soap/encoding/", "arrayType");
  private static final QName ENC_OFFSET = new QName("http://schemas.xmlsoap.org/s/encoding/", "offset");

  @SuppressWarnings("squid:S3878")
  protected static final Set<QName> SKIPPED_SOAP_ATTRS =
      new HashSet<>(Arrays.asList(new QName[]{HREF, ID, ENC_OFFSET}));

  private void processAttributes(SchemaAnnotation sannotation, SchemaType stype, XmlCursor xmlc) {
    if (soapEnc) {
      QName typeName = stype.getName();
      if (typeName != null) {
        xmlc.insertAttributeWithValue(XSI_TYPE, formatQName(xmlc, typeName));
      }
    }

    //DOTWEBSTACK - Added
    if ((queryResult != null) && (sannotation != null)) {
      String variableName = SoapUtils.getAttributeValue(sannotation, DWS_TYPE_FROM_QNAME);
      if ((variableName != null) && (currentRow != null)) {
        if (currentRow.hasBinding(variableName)) {
          String xsitype = currentRow.getValue(variableName).stringValue();
          xmlc.insertAttributeWithValue(XSI_TYPE, formatXsiType(xsitype, stype, xmlc));
        } else {
          LOG.warn("Could not find binding variable: {}", variableName);
        }
      }
    }

    SchemaProperty[] attrProps = stype.getAttributeProperties();
    for (int i = 0; i < attrProps.length; i++) {
      processAttribute(stype, xmlc, attrProps[i]);
    }
  }

  private String formatXsiType(final String xsitype, SchemaType stype, final XmlCursor xmlc) {
    String namespace = ((SchemaTypeImpl) stype).getTargetNamespace();
    String prefix = xmlc.prefixForNamespace(((SchemaTypeImpl) stype).getTargetNamespace());
    String type = xsitype.replace(namespace, "");
    return prefix + ":" + type;
  }

  @SuppressWarnings("squid:S3776")
  private void processAttribute(SchemaType stype, XmlCursor xmlc, SchemaProperty attr) {
    if (attr.getMinOccurs().intValue() == 0 && ignoreOptional) {
      return;
    }

    if (attr.getName().equals(new QName(Constants.XMLMIME_NS, "contentType"))) {
      xmlc.insertAttributeWithValue(attr.getName(), "application/?");
      return;
    }

    if (soapEnc) {
      if (SKIPPED_SOAP_ATTRS.contains(attr.getName())) {
        return;
      }
      if (ENC_ARRAYTYPE.equals(attr.getName())) {
        SOAPArrayType arrayType = ((SchemaWSDLArrayType) stype.getAttributeModel().getAttribute(
            attr.getName())).getWSDLArrayType();
        if (arrayType != null) {
          xmlc.insertAttributeWithValue(attr.getName(),
              formatQName(xmlc, arrayType.getQName()) + arrayType.soap11DimensionString());
        }
        return;
      }
    }

    String value = null;
    if (multiValuesProvider != null) {
      String[] values = multiValuesProvider.getMultiValues(attr.getName()).toArray(new String[]{});
      if (values != null) {
        value = StringUtils.join(values, ",");
      }
    }
    if (value == null) {
      value = attr.getDefaultText();
    }
    if (value == null) {
      value = sampleDataForSimpleType(attr.getType());
    }

    xmlc.insertAttributeWithValue(attr.getName(), value);
  }

  private void processSequence(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
    SchemaParticle[] spc = sp.getParticleChildren();
    for (int i = 0; i < spc.length; i++) {
      // / <parent>maybestuff^</parent>
      processParticle(spc[i], xmlc, mixed);
      // <parent>maybestuff...morestuff^</parent>
      if (mixed && i < spc.length - 1) {
        xmlc.insertChars(pick(WORDS));
      }
    }
  }

  private void processChoice(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
    SchemaParticle[] spc = sp.getParticleChildren();
    if (!skipComments) {
      xmlc.insertComment("You have a CHOICE of the next " + spc.length + " items at this level");
    }

    for (int i = 0; i < spc.length; i++) {
      processParticle(spc[i], xmlc, mixed);
    }
  }

  private void processAll(SchemaParticle sp, XmlCursor xmlc, boolean mixed) {
    SchemaParticle[] spc = sp.getParticleChildren();
    if (!skipComments) {
      xmlc.insertComment("You may enter the following " + spc.length + " items in any order");
    }

    for (int i = 0; i < spc.length; i++) {
      processParticle(spc[i], xmlc, mixed);
      if (mixed && i < spc.length - 1) {
        xmlc.insertChars(pick(WORDS));
      }
    }
  }

  private void processWildCard(XmlCursor xmlc) {
    if (!skipComments) {
      xmlc.insertComment("You may enter ANY elements at this point");
    }
  }

  private void addElementTypeAndRestricionsComment(SchemaLocalElement element, XmlCursor xmlc) {

    SchemaType type = element.getType();
    if (typeComment && (type != null && type.isSimpleType())) {
      StringBuilder stringBuilder = new StringBuilder();

      XmlAnySimpleType[] values = type.getEnumerationValues();
      if (values != null && values.length > 0) {
        stringBuilder.append(" - enumeration: [");
        for (int c = 0; c < values.length; c++) {
          if (c > 0) {
            stringBuilder.append(",");
          }

          stringBuilder.append(values[c].getStringValue());
        }

        stringBuilder.append("]");
      }

      String info = stringBuilder.toString();
      if (type.isAnonymousType()) {
        xmlc.insertComment("anonymous type" + info);
      } else {
        xmlc.insertComment("type: " + type.getName().getLocalPart() + info);
      }
    }
  }
}
