package org.dotwebstack.framework.param.term;

import com.google.common.collect.ImmutableList;
import lombok.NonNull;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public final class TermParameterFactory {

  // XXX (PvH) Sonar zal klagen over een ontbrekende private constructor

  // XXX (PvH) Ik zou deze method hernoemen naar create- of newTermParameter, zodat het duidelijk is
  // dat je een nieuw object maakt. Bij factories betekent een getXXX dat de waarde mogelijk uit de
  // cache gehaald wordt.
  public static TermParameter getTermParameter(@NonNull IRI identifier, @NonNull String name,
      @NonNull PropertyShape shape, boolean required) {

    Value defaultValue = shape.getDefaultValue();
    IRI type = shape.getDatatype();
    if (type.equals(XMLSchema.STRING)) {
      String defVal = defaultValue != null ? defaultValue.stringValue() : null;
      return new StringTermParameter(identifier, name, required, defVal);
    } else if (type.equals(XMLSchema.INTEGER)) {
      Integer defVal = defaultValue != null ? ((Literal) defaultValue).intValue() : null;
      return new IntegerTermParameter(identifier, name, required, defVal);
    } else if (type.equals(XMLSchema.BOOLEAN)) {
      Boolean defVal = defaultValue != null ? ((Literal) defaultValue).booleanValue() : null;
      return new BooleanTermParameter(identifier, name, required, defVal);
    } else if (type.equals(XMLSchema.ANYURI)) {
      return new IriTermParameter(identifier, name, required, (IRI) defaultValue);
    } else {
      // XXX (PvH) Je kan hier gewoon het type meegeven ipv de String. %s in String.format zal
      // toString aanroepen.
      throwConfigException(type.toString());
    }

    // XXX (PvH) Ik zou geen else doen en geen null returnen, maar de method eindigen met de
    // exception. Nu is dit dode code.

    return null;
  }

  private static void throwConfigException(String type) {
    throw new ConfigurationException(
        String.format("Unsupported data type: <%s>. Supported types: %s", type, ImmutableList.of(
            XMLSchema.BOOLEAN, XMLSchema.STRING, XMLSchema.INTEGER, XMLSchema.ANYURI)));
  }

}
