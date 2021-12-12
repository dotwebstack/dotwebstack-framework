package org.dotwebstack.framework.backend.postgres;

import static java.util.function.Predicate.not;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatial;
import org.dotwebstack.framework.backend.postgres.model.PostgresSpatialReferenceSystem;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.ext.spatial.SpatialConstants;
import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;
import org.dotwebstack.framework.ext.spatial.model.Spatial;
import org.dotwebstack.framework.ext.spatial.model.SpatialReferenceSystem;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class PostgresSpatialBackendModule implements SpatialBackendModule<PostgresSpatialReferenceSystem> {

  private static final String F_TABLE_SCHEMA = "f_table_schema";

  private static final String F_TABLE_NAME = "f_table_name";

  private static final String F_GEOMETRY_COLUMN = "f_geometry_column";

  private static final String SRID = "srid";

  private static final String GEOMETRY_COLUMNS_STMT = String.format("SELECT %s, %s, %s, %s FROM geometry_columns",
      F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, SRID);

  private final Map<String, Integer> sridByTableColumn;

  private final Schema schema;

  public PostgresSpatialBackendModule(Schema schema, DatabaseClient databaseClient) {
    this.schema = schema;
    this.sridByTableColumn = getSridByTableColumn(databaseClient);
  }

  private Map<String, Integer> getSridByTableColumn(DatabaseClient databaseClient) {
    return databaseClient.sql(GEOMETRY_COLUMNS_STMT)
        .fetch()
        .all()
        .map(this::mapToEntry)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        .onErrorContinue((e, i) -> LOG.warn("Retrieving geometry columns failed. Exception: {}", e.getMessage()))
        .onErrorReturn(Map.of())
        .block();
  }

  private AbstractMap.SimpleEntry<String, Integer> mapToEntry(Map<String, Object> row) {
    String key = row.get(F_TABLE_SCHEMA) + "." + row.get(F_TABLE_NAME) + "." + row.get(F_GEOMETRY_COLUMN);
    Integer value = (Integer) row.get(SRID);
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  @Override
  public Class<PostgresSpatialReferenceSystem> getSpatialReferenceSystemClass() {
    return PostgresSpatialReferenceSystem.class;
  }

  @Override
  public void init(Spatial spatial) {
    Map<String, ObjectType<? extends ObjectField>> objectTypes = schema.getObjectTypes();

    Map<String, List<PostgresObjectField>> allFieldsPerTableName = objectTypes.values()
        .stream()
        .map(PostgresObjectType.class::cast)
        .filter(not(PostgresObjectType::isNested))
        .map(objectType -> new AbstractMap.SimpleEntry<>(objectType.getTable(), getFields(objectType)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    allFieldsPerTableName.entrySet()
        .forEach(field -> setSpatial(spatial, field));
  }

  private List<PostgresObjectField> getFields(PostgresObjectType objectType) {
    return objectType.getFields()
        .values()
        .stream()
        .map(this::getPostgresObjectFields)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private List<PostgresObjectField> getPostgresObjectFields(PostgresObjectField field) {
    return field.getTargetType() == null || !field.getTargetType()
        .isNested() ? List.of(field) : getFields((PostgresObjectType) field.getTargetType());
  }

  private void setSpatial(Spatial spatial, Map.Entry<String, List<PostgresObjectField>> allFieldsPerTableName) {
    allFieldsPerTableName.getValue()
        .stream()
        .filter(this::isGeometryType)
        .forEach(field -> addSpatial(spatial, allFieldsPerTableName.getKey(), field));
  }

  private boolean isGeometryType(PostgresObjectField postgresObjectField) {
    return SpatialConstants.GEOMETRY.equals(postgresObjectField.getType());
  }

  private void addSpatial(Spatial spatial, String tableName, PostgresObjectField objectField) {
    Integer srid = getSrid(tableName, objectField.getColumn());
    BiMap<Integer, String> spatialReferenceSystems =
        getSpatialReferenceSystems(spatial, tableName, objectField.getColumn());
    BiMap<Integer, Integer> equivalents = getEquivalents(spatial, spatialReferenceSystems);
    BiMap<Integer, String> bboxes = getBboxes(spatial, tableName, objectField.getColumn());

    PostgresSpatial postgresSpatial = PostgresSpatial.builder()
        .spatialReferenceSystems(spatialReferenceSystems)
        .equivalents(equivalents)
        .bboxes(bboxes)
        .srid(srid)
        .build();

    objectField.setSpatial(postgresSpatial);
  }

  private BiMap<Integer, Integer> getEquivalents(Spatial spatial, Map<Integer, String> spatialReferenceSystems) {
    return HashBiMap.create(spatial.getReferenceSystems()
        .entrySet()
        .stream()
        .filter(entry -> spatialReferenceSystems.containsKey(entry.getKey()))
        .filter(entry -> entry.getValue()
            .getEquivalent() != null)
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()
            .getEquivalent())));
  }

  private BiMap<Integer, String> getSpatialReferenceSystems(Spatial spatial, String tableName, String geometryColumn) {
    return getTableNamesPerSrid(PostgresSpatialReferenceSystem::getColumnSuffix, spatial, tableName, geometryColumn);
  }

  private BiMap<Integer, String> getBboxes(Spatial spatial, String tableName, String geometryColumn) {
    return getTableNamesPerSrid(PostgresSpatialReferenceSystem::getBboxColumnSuffix, spatial, tableName,
        geometryColumn);
  }

  private BiMap<Integer, String> getTableNamesPerSrid(Function<PostgresSpatialReferenceSystem, String> suffixFunc,
      Spatial spatial, String tableName, String geometryColumn) {
    return HashBiMap.create(spatial.getReferenceSystems()
        .entrySet()
        .stream()
        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(),
            getColumnName(suffixFunc, geometryColumn, entry.getValue())))
        .filter(entry -> entry.getKey()
            .equals(getSrid(tableName, entry.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  private String getColumnName(Function<PostgresSpatialReferenceSystem, String> suffixFunc, String geometryColumn,
      SpatialReferenceSystem srs) {
    Optional<String> columnSuffix = Optional.of(srs)
        .map(PostgresSpatialReferenceSystem.class::cast)
        .map(suffixFunc);

    return columnSuffix.map(geometryColumn::concat)
        .orElse(geometryColumn);
  }

  private Integer getSrid(String tableName, String columnName) {
    var geometryTableColumn = tableName + "." + columnName;

    return sridByTableColumn.keySet()
        .stream()
        .filter(key -> key.endsWith(geometryTableColumn))
        .findFirst()
        .map(sridByTableColumn::get)
        .orElse(null);
  }

}
