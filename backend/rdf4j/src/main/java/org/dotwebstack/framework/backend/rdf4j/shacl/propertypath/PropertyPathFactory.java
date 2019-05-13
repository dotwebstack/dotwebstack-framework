package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.dotwebstack.framework.backend.rdf4j.ValueUtils;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;

public class PropertyPathFactory  {

  private PropertyPathFactory() {}

  private static final Map<IRI, Function<List<PropertyPath>,PropertyPath>> MAP =
      ImmutableMap.of(RDF.FIRST, PropertyPathFactory::sequencePath,
          SHACL.INVERSE_PATH, PropertyPathFactory::inversePath,
          SHACL.ALTERNATIVE_PATH, PropertyPathFactory::alternativePath,
          SHACL.ZERO_OR_MORE_PATH, PropertyPathFactory::zeroOrMore,
          SHACL.ONE_OR_MORE_PATH, PropertyPathFactory::oneOrMore);

  public static PropertyPath create(Model model, Resource subject, IRI predicate) {
    Value value = ValueUtils.findRequiredProperty(model, subject, predicate);

    if (value instanceof MemBNode) {
      MemBNode blankNode = (MemBNode) value;
      IRI iri = blankNode.getSubjectStatementList().get(0).getPredicate();

      List<PropertyPath> childs = memStatements(blankNode.getSubjectStatementList())
          .stream()
          .map(child -> create(model,child.getSubject(),child.getPredicate()))
          .collect(Collectors.toList());

      return MAP.get(iri).apply(childs);
    }
    return PredicatePath.builder().iri((IRI) value).build();
  }

  private static List<MemStatement> memStatements(MemStatementList memStatementList) {
    List<MemStatement> result = Lists.newArrayList(memStatementList.get(0));

    if (memStatementList.size() > 1) {
      result.add(memStatementList.get(1));
    }

    return result;
  }

  private static PropertyPath sequencePath(List<PropertyPath> propertyPaths) {
    assert propertyPaths.size() == 2;
    return SequencePath.builder()
        .first(propertyPaths.get(0))
        .rest(propertyPaths.get(1))
        .build();
  }

  private static PropertyPath inversePath(List<PropertyPath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return InversePath.builder()
        .object((PredicatePath) propertyPaths.get(0))
        .build();
  }

  private static PropertyPath alternativePath(List<PropertyPath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return AlternativePath.builder()
        .object(propertyPaths.get(0))
        .build();
  }

  private static PropertyPath zeroOrMore(List<PropertyPath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return ZeroOrMorePath.builder()
        .object(propertyPaths.get(0))
        .build();
  }

  private static PropertyPath oneOrMore(List<PropertyPath> propertyPaths) {
    assert propertyPaths.size() == 1;
    return OneOrMorePath.builder()
        .object(propertyPaths.get(0))
        .build();
  }
}
