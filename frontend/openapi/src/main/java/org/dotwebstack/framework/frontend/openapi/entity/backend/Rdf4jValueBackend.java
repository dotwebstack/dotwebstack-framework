package org.dotwebstack.framework.frontend.openapi.entity.backend;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Optional;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.NonNull;
import org.apache.marmotta.ldpath.api.backend.NodeBackend;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rdf4jValueBackend implements NodeBackend<Value> {

  private static final Logger LOG = LoggerFactory.getLogger(Rdf4jValueBackend.class);

  public Rdf4jValueBackend() {
    super();
  }

  /**
   * Test whether the node passed as argument is a literal.
   *
   * @param node the node to check
   * @return true if the node is a literal
   */
  @Override
  public boolean isLiteral(Value node) {
    return node instanceof Literal;
  }

  /**
   * Test whether the node passed as argument is a URI.
   *
   * @param node the node to check
   * @return true if the node is a URI
   */
  @Override
  public boolean isURI(Value node) {
    return node instanceof IRI;
  }

  /**
   * Test whether the node passed as argument is a blank node.
   *
   * @param node the node to check
   * @return true if the node is a blank node
   */
  @Override
  public boolean isBlank(Value node) {
    return node instanceof BNode;
  }

  /**
   * Return the language of the literal node passed as argument.
   *
   * @param node the literal node for which to return the language
   * @return a Locale representing the language of the literal, or null if the literal node has no
   *         language
   * @throws IllegalArgumentException in case the node is no literal
   */
  @Override
  public Locale getLiteralLanguage(@NonNull Value node) {
    try {
      Optional<String> language = ((Literal) node).getLanguage();
      return language.map(Locale::new).orElse(null);
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  /**
   * Return the URI of the type of the literal node passed as argument.
   *
   * @param node the literal node for which to return the typer
   * @return a URI representing the type of the literal content, or null if the literal is untyped
   * @throws IllegalArgumentException in case the node is no literal
   */
  @Override
  public URI getLiteralType(@NonNull Value node) {
    try {
      IRI dataTypeIri = ((Literal) node).getDatatype();
      if (dataTypeIri != null) {
        return getDataTypeUri(dataTypeIri);
      } else {
        return null;
      }
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  private URI getDataTypeUri(IRI dataTypeIri) {
    try {
      return new URI(dataTypeIri.stringValue());
    } catch (URISyntaxException ex) {
      LOG.error("literal datatype was not a valid URI: {}", dataTypeIri, ex);
      return null;
    }
  }

  /**
   * Return the string value of a node. For a literal, this will be the content, for a URI node it
   * will be the URI itself, and for a blank node it will be the identifier of the node.
   */
  @Override
  public String stringValue(@NonNull Value value) {
    return value.stringValue();
  }

  @Override
  public BigDecimal decimalValue(@NonNull Value node) {
    try {
      return ((Literal) node).decimalValue();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  @Override
  public BigInteger integerValue(@NonNull Value node) {
    try {
      return ((Literal) node).integerValue();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  @Override
  public Boolean booleanValue(@NonNull Value node) {
    try {
      return ((Literal) node).booleanValue();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  @Override
  public Date dateTimeValue(@NonNull Value node) {
    try {
      XMLGregorianCalendar cal = ((Literal) node).calendarValue();
      return cal.toGregorianCalendar().getTime();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  @Override
  public Date dateValue(@NonNull Value node) {
    try {
      XMLGregorianCalendar cal = ((Literal) node).calendarValue();
      return new GregorianCalendar(cal.getYear(), cal.getMonth(), cal.getDay()).getTime();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  @Override
  public Date timeValue(@NonNull Value node) {
    // from a XMLGregorianCalendar
    return dateTimeValue(node);
  }

  @Override
  public Long longValue(@NonNull Value node) {
    try {
      return ((Literal) node).longValue();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  @Override
  public Double doubleValue(@NonNull Value node) {
    try {
      return ((Literal) node).doubleValue();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  @Override
  public Float floatValue(@NonNull Value node) {
    try {
      return ((Literal) node).floatValue();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  @Override
  public Integer intValue(@NonNull Value node) {
    try {
      return ((Literal) node).intValue();
    } catch (ClassCastException ex) {
      throw getNodeCastException(node, ex);
    }
  }

  private IllegalArgumentException getNodeCastException(Value node, ClassCastException ex) {
    return new IllegalArgumentException(String.format("Value %s is not a literal but of type %s",
        node.stringValue(), debugType(node)), ex);
  }

  /**
   * Prints the type (URI,bNode,literal) by inspecting the parsed {@link Value} to improve error
   * messages and other loggings. In case of literals also the {@link #getLiteralType(Value) literal
   * type} is printed
   *
   * @param value the value or <code>null</code>
   * @return the type as string.
   */
  protected String debugType(Value value) {
    String namelessNodeType = isBlank(value) ? "bNode" : "literal (" + value.toString() + ")";
    String nodeType = isURI(value) ? "URI" : namelessNodeType;
    return value == null ? "null" : nodeType;
  }

  @Override
  public Literal createLiteral(@NonNull String content) {
    return SimpleValueFactory.getInstance().createLiteral(content);
  }

  @Override
  public Literal createLiteral(@NonNull String content, Locale language, URI type) {
    LOG.debug("creating literal with content \"{}\", language {}, datatype {}", content, language,
        type);
    if (language == null && type == null) {
      return createLiteral(content);
    } else if (type == null) {
      return SimpleValueFactory.getInstance().createLiteral(content, language.getLanguage());
    } else {
      return SimpleValueFactory.getInstance().createLiteral(content, createURI(type.toString()));
    }
  }

  @Override
  public IRI createURI(String uri) {
    return SimpleValueFactory.getInstance().createIRI(uri);
  }

}
