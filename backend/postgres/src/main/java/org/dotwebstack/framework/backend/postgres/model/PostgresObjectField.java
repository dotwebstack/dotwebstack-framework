package org.dotwebstack.framework.backend.postgres.model;

import static java.util.Optional.ofNullable;
import static org.dotwebstack.framework.core.helpers.TypeHelper.REF;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.dotwebstack.framework.core.helpers.StringHelper;
import org.dotwebstack.framework.core.model.AbstractObjectField;
import org.dotwebstack.framework.core.model.ObjectType;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class PostgresObjectField extends AbstractObjectField {
  private static final String INFIX = "__";

  private static final Pattern NAME_PATTERN_1ST = Pattern.compile("([^A-Z])(\\d*[A-Z])");

  private static final Pattern NAME_PATTERN_2ND = Pattern.compile("([A-Z])([A-Z][^A-Z])");

  private static final String NAME_REPLACEMENT = "$1_$2";

  private static final String TSV_PREFIX = "_tsv";

  private String column;

  @Valid
  private List<JoinColumn> joinColumns = new ArrayList<>();

  @Valid
  private JoinTable joinTable;

  private String mappedBy;

  private PostgresObjectField mappedByObjectField;

  private String presenceColumn;

  @JsonIgnore
  private PostgresSpatial spatial;

  public PostgresObjectField() {
    super();
  }

  public PostgresObjectField(PostgresObjectField objectField) {
    super();
    this.name = objectField.getName();
    this.objectType = objectField.getObjectType();
    this.type = objectField.getType();
    this.keys = objectField.getKeys();
    this.isList = objectField.isList();
    this.nullable = objectField.isNullable();
    this.pageable = objectField.isPageable();
    this.visible = objectField.isVisible();
    this.aggregationOf = objectField.getAggregationOf();
    this.keyField = objectField.getKeyField();
    this.valueFetcher = objectField.getValueFetcher();
    this.enumeration = objectField.getEnumeration();
    this.aggregationOfType = objectField.getAggregationOfType();
    this.targetType = objectField.getTargetType();
    this.arguments = objectField.getArguments();

    this.spatial = objectField.getSpatial();
    this.column = objectField.getColumn();
    this.joinColumns = objectField.getJoinColumns();
    this.joinTable = objectField.getJoinTable();
    this.mappedBy = objectField.getMappedBy();
    this.mappedByObjectField = objectField.getMappedByObjectField();
    this.presenceColumn = objectField.getPresenceColumn();
  }

  public void initColumns() {
    initColumns(null);
  }

  public void initColumns(List<PostgresObjectField> ancestors) {
    if (column == null) {
      var tempName = NAME_PATTERN_1ST.matcher(name)
          .replaceAll(NAME_REPLACEMENT);

      var columnName = NAME_PATTERN_2ND.matcher(tempName)
          .replaceAll(NAME_REPLACEMENT)
          .toLowerCase();


      column = ofNullable(ancestors).map(this::toColumn)
          .map(prefix -> {
            if (columnName.equals(REF)) {
              return prefix;
            }
            return prefix.concat(INFIX)
                .concat(columnName);
          })
          .orElse(columnName);
    }
  }


  private String toColumn(List<PostgresObjectField> ancestors) {
    return ancestors.stream()
        .map(PostgresObjectField::getName)
        .filter(name -> !name.equals(REF))
        .map(StringHelper::toSnakeCase)
        .collect(Collectors.joining("__"));
  }

  public boolean hasNestedFields() {
    return Optional.of(this)
        .map(AbstractObjectField::getTargetType)
        .filter(ObjectType::isNested)
        .isPresent();
  }
}
