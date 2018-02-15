package org.dotwebstack.framework.frontend.openapi.ldpath;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import lombok.NonNull;
import org.apache.marmotta.ldpath.LDPath;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.exception.LDPathParseException;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;
import org.eclipse.rdf4j.sail.memory.model.IntegerMemLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allow sorting of the selection passed as first argument on property specified as second argument.
 * Usage:
 *
 * <ul>
 * <li><b>fn:sort(path-expression, property)</b>: sorts the results on the specified property
 * according to ascending number order</li>
 * <li><b>fn:sort(path-expression, property, property type)</b>: sorts the results on the specified
 * property according to the given type in ascending direction; property type can be one of
 * "string", "number" or "date"</li>
 * <li><b>fn:sort(path-expression, property, property type, direction)</b>: sorts the results
 * according to the given property type in the specified direction; type can be one of "string",
 * "number" or "date"; direction can be one of "asc" or "desc"</li>
 *
 * </ul>
 * Author: Nathan van Dalen
 */
public class SortByPropertyFunction<N> extends SelectorFunction<N> {

  private static final Logger LOG = LoggerFactory.getLogger(SortByPropertyFunction.class);
  private static final int OBJECT_ARRAY_INDEX = 0;
  private static final int PROPERTY_INDEX = 1;
  private static final int PROPERTY_TYPE_INDEX = 2;
  private static final int DIRECTION_INDEX = 3;
  private final ImmutableMap<String, String> ldPathNamespaces;
  private final StringTransformer<N> transformer;

  private enum Type {
    STRING,
    NUMBER
  }

  enum Direction {
    ASC,
    DESC
  }

  public SortByPropertyFunction(ImmutableMap<String, String> ns) {
    this.transformer = new StringTransformer<>();
    this.ldPathNamespaces = ns;
  }

  @Override
  @SafeVarargs
  public final Collection<N> apply(@NonNull RDFBackend<N> nodeRdfBackend, N context,
      Collection<N>... args) {
    // parse arguments
    Iterator<N> iterator = getIterator(args[OBJECT_ARRAY_INDEX]);
    String property = getProperty(nodeRdfBackend, args);
    Type propertyType = getPropertyType(nodeRdfBackend, args);
    java.util.Comparator<Comparable<?>> comparator =
        new InnerComparator(getSortDirection(nodeRdfBackend, args));

    LDPath<N> ldpath = new LDPath<>(nodeRdfBackend);
    Map<Comparable<?>, N> sortedMap = new TreeMap<>(comparator);
    while (iterator.hasNext()) {
      N child = iterator.next();
      try {
        N result = ldpath.pathQuery(child, property, ldPathNamespaces).iterator().next();
        sortedMap.put(determineValue(propertyType, result), child);
      } catch (LDPathParseException | IllegalStateException | NoSuchElementException exception) {
        LOG.error("LDPath expression could not be parsed", exception);
      }
    }

    return new ArrayList<>(sortedMap.values());
  }

  private Iterator<N> getIterator(Collection<N> arg) {
    return arg.iterator();
  }

  private String getProperty(RDFBackend<N> nodeRdfBackend,
      @SuppressWarnings("unchecked") Collection<N>... args) {
    try {
      return transformer.transform(nodeRdfBackend, args[PROPERTY_INDEX].iterator().next(), null);
    } catch (IndexOutOfBoundsException ex) {
      LOG.warn("Error determining property. Defaulting to 'ROOT'", ex);
      return ".";
    }
  }

  private Type getPropertyType(RDFBackend<N> nodeRdfBackend,
      @SuppressWarnings("unchecked") Collection<N>... args) {
    if (args.length > PROPERTY_TYPE_INDEX) {
      String arg = transformer.transform(nodeRdfBackend,
          args[PROPERTY_TYPE_INDEX].iterator().next(), null).toUpperCase();
      try {
        return Type.valueOf(arg);
      } catch (IllegalArgumentException ex) {
        LOG.warn(String.format("Unknown property '%s'", arg), ex);
        return Type.STRING;
      }
    } else {
      return Type.STRING;
    }
  }

  private Direction getSortDirection(RDFBackend<N> nodeRdfBackend,
      @SuppressWarnings("unchecked") Collection<N>... args) {
    if (args.length > DIRECTION_INDEX) {
      String arg = transformer.transform(nodeRdfBackend, args[DIRECTION_INDEX].iterator().next(),
          null).toUpperCase();
      try {
        return Direction.valueOf(arg);
      } catch (IllegalArgumentException ex) {
        LOG.warn(String.format("Unknown property '%s'", arg), ex);
        return Direction.ASC;
      }
    } else {
      return Direction.ASC;
    }
  }

  private Comparable<?> determineValue(Type propertyType, N result) {
    switch (propertyType) {
      case STRING:
        return result.toString();
      case NUMBER:
        return ((IntegerMemLiteral) result).intValue();
      default:
        throw new IllegalStateException("Unsupported property type supplied");
    }
  }

  @Override
  public String getLocalName() {
    return "sortByProperty";
  }

  @Override
  public String getSignature() {
    return "fn:sortByProperty(nodes : NodeList, property : String "
        + ", (\"string\"|\"number\") [, (\"asc\"|\"desc\") ]) : NodeList ";
  }

  @Override
  public String getDescription() {
    return "Sort the node list passed as first argument on the property evaluated by the LD path "
        + "expression specified in the second argument. "
        + "The third argument specifies the property type. "
        + "The fourth argument is used to determine the sort direction.";
  }

  static class InnerComparator implements java.util.Comparator<Comparable<?>> {

    private final Direction direction;

    InnerComparator(Direction direction) {
      this.direction = direction;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public int compare(Comparable o1, Comparable o2) {
      if (direction == Direction.ASC) {
        return o1.compareTo(o2);
      }
      return o2.compareTo(o1);
    }
  }
}
