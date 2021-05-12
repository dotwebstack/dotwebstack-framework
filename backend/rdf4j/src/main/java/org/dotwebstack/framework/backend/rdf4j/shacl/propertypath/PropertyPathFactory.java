package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import static org.dotwebstack.framework.backend.rdf4j.helper.MemStatementListHelper.listOf;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;

public class PropertyPathFactory {

  private PropertyPathFactory() {}

  private static final ImmutableMap.Builder<IRI, Function<List<BasePath>, BasePath>> BUILDER =
      new ImmutableMap.Builder<>();

  static {
    BUILDER.put(RDF.FIRST, PropertyPathFactory::sequencePath);
    BUILDER.put(SHACL.INVERSE_PATH, PropertyPathFactory::inversePath);
    BUILDER.put(SHACL.ALTERNATIVE_PATH, PropertyPathFactory::alternativePath);
    BUILDER.put(SHACL.ZERO_OR_MORE_PATH, PropertyPathFactory::zeroOrMore);
    BUILDER.put(SHACL.ONE_OR_MORE_PATH, PropertyPathFactory::oneOrMore);
    BUILDER.put(SHACL.ZERO_OR_ONE_PATH, PropertyPathFactory::zeroOrOne);
  }

  private static final Map<IRI, Function<List<BasePath>, BasePath>> MAP = BUILDER.build();

  public static BasePath create(Model model, Resource subject, IRI predicate) {
    var value = ValueUtils.findRequiredProperty(model, subject, predicate);

    if (value instanceof MemBNode) {
      MemBNode blankNode = (MemBNode) value;
      IRI iri = blankNode.getSubjectStatementList()
          .get(0)
          .getPredicate();

      List<BasePath> childs = listOf(blankNode.getSubjectStatementList()).stream()
          .map(child -> create(model, child.getSubject(), child.getPredicate()))
          .collect(Collectors.toList());

      return MAP.get(iri)
          .apply(childs);
    }
    return PredicatePath.builder()
        .iri((IRI) value)
        .build();
  }

  private static BasePath sequencePath(List<BasePath> propertyPaths) {
    assert propertyPaths.size() == 2;
    return SequencePath.builder()
        .first(propertyPaths.get(0))
        .rest(propertyPaths.get(1))
        .build();
  }

  private static BasePath inversePath(List<BasePath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return InversePath.builder()
        .object(propertyPaths.get(0))
        .build();
  }

  private static BasePath alternativePath(List<BasePath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return AlternativePath.builder()
        .object((SequencePath) propertyPaths.get(0))
        .build();
  }

  private static BasePath zeroOrMore(List<BasePath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return ZeroOrMorePath.builder()
        .object((PredicatePath) propertyPaths.get(0))
        .build();
  }

  private static BasePath oneOrMore(List<BasePath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return OneOrMorePath.builder()
        .object((PredicatePath) propertyPaths.get(0))
        .build();
  }

  private static BasePath zeroOrOne(List<BasePath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return ZeroOrOnePath.builder()
        .object((PredicatePath) propertyPaths.get(0))
        .build();
  }
}
