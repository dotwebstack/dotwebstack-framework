package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_FIELD;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_IS_RESOURCE;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.NonNull;
import org.dotwebstack.framework.backend.rdf4j.directives.Rdf4jDirectives;
import org.dotwebstack.framework.backend.rdf4j.query.context.FieldPathHelper;
import org.dotwebstack.framework.backend.rdf4j.shacl.NodeShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleLiteral;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

public class CompareHelper {

  private CompareHelper() {}

  public static Comparator<Value> getComparator(boolean asc) {
    return (value1, value2) -> compareValue(value1, value2, asc);
  }

  @SuppressWarnings("rawtypes")
  public static Comparator<Value> getComparator(boolean asc, @NonNull Model model,
      @NonNull GraphQLArgument sortArgument, @NonNull NodeShape nodeShape, @Nonnull GraphQLObjectType objectType) {
    Map sortArguments = (Map) ((List) sortArgument.getDefaultValue()).get(0);

    Object sortResource = sortArguments.get(SORT_FIELD_IS_RESOURCE);
    Object sortField = sortArguments.get(SORT_FIELD_FIELD);

    boolean isResource = (boolean) (Objects.nonNull(sortResource) ? sortResource : false);
    String field = Objects.nonNull(sortField) ? sortField.toString() : sortArgument.getName();

    List<GraphQLFieldDefinition> fieldPath = FieldPathHelper.getFieldDefinitions(objectType, field);

    return (value1, value2) -> compareOptional(resolveValue(value1, model, fieldPath, isResource, nodeShape),
        resolveValue(value2, model, fieldPath, isResource, nodeShape), asc);
  }

  private static Optional<Value> resolveValue(Value value, Model model, List<GraphQLFieldDefinition> fieldPath,
      boolean isResource, NodeShape nodeShape) {

    return isResource && fieldPath.size() == 1 ? Optional.of(value)
        : resolveOtherValue((Resource) value, model, isResource, nodeShape, fieldPath);
  }

  private static Optional<Value> resolveOtherValue(Resource value, Model model, boolean isResource, NodeShape nodeShape,
      List<GraphQLFieldDefinition> fieldPath) {
    GraphQLFieldDefinition fieldDefinition = fieldPath.get(0);

    if (Objects.nonNull(fieldDefinition.getDirective(Rdf4jDirectives.RESOURCE_NAME))) {
      return Optional.of(value);
    }

    PropertyShape propertyShape = nodeShape.getPropertyShape(fieldDefinition.getName());
    BasePath basePath = propertyShape.getPath();

    IRI iri = basePath instanceof PredicatePath ? ((PredicatePath) basePath).getIri() : basePath.getBaseIri();

    Optional<Value> childOptional = Models.getProperty(model, value, iri);

    return fieldPath.size() == 1 ? childOptional
        : childOptional.flatMap(child -> resolveValue(child, model, fieldPath.subList(1, fieldPath.size()), isResource,
            propertyShape.getNode()));
  }

  private static int compareOptional(Optional<Value> optional1, Optional<Value> optional2, boolean asc) {
    if (optional1.isPresent() && optional2.isPresent()) {
      return compareValue(optional1.get(), optional2.get(), asc);
    }

    // sort in comparison to null values
    return optional1.isEmpty() ? 1 : -1;
  }

  private static int compareValue(@NonNull Value value1, @NonNull Value value2, boolean asc) {
    if (value1 instanceof IRI) {
      return compareStringValue(value1, value2, asc);
    }

    IRI datatype = ((SimpleLiteral) value1).getDatatype();
    if (isInteger(datatype)) {
      return compareIntegerLiteral((SimpleLiteral) value1, (SimpleLiteral) value2, asc);
    }

    if (isDecimal(datatype)) {
      return compareDecimalLiteral((SimpleLiteral) value1, (SimpleLiteral) value2, asc);
    }
    return compareStringValue(value1, value2, asc);
  }

  private static int compareStringValue(Value value1, Value value2, boolean asc) {
    if (asc) {
      return value1.stringValue()
          .compareTo(value2.stringValue());
    }
    return value2.stringValue()
        .compareTo(value1.stringValue());
  }

  private static int compareIntegerLiteral(SimpleLiteral integerLiteral1, SimpleLiteral integerLiteral2, boolean asc) {
    if (asc) {
      return integerLiteral1.integerValue()
          .compareTo(integerLiteral2.integerValue());
    }
    return integerLiteral2.integerValue()
        .compareTo(integerLiteral1.integerValue());
  }

  private static int compareDecimalLiteral(SimpleLiteral integerLiteral1, SimpleLiteral integerLiteral2, boolean asc) {
    if (asc) {
      return Float.compare(integerLiteral1.floatValue(), integerLiteral2.floatValue());
    }
    return Float.compare(integerLiteral2.floatValue(), integerLiteral1.floatValue());
  }


  private static boolean isInteger(IRI datatype) {
    return Objects.equals(datatype, XMLSchema.INT) || Objects.equals(datatype, XMLSchema.INTEGER);
  }

  private static boolean isDecimal(IRI datatype) {
    return Objects.equals(datatype, XMLSchema.FLOAT) || Objects.equals(datatype, XMLSchema.DOUBLE)
        || Objects.equals(datatype, XMLSchema.DECIMAL);
  }


}
