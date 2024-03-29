package org.dotwebstack.framework.backend.postgres;

import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.backend.postgres.helpers.PostgresSpatialHelper.getSegmentsTableName;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dotwebstack.framework.backend.postgres.helpers.CyclicRefDetector;
import org.dotwebstack.framework.backend.postgres.model.GeometryMetadata;
import org.dotwebstack.framework.backend.postgres.model.GeometrySegmentsTable;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
class PostgresSpatialBackendModule implements SpatialBackendModule<PostgresSpatialReferenceSystem> {

  private static final String F_TABLE_SCHEMA = "f_table_schema";

  private static final String F_TABLE_NAME = "f_table_name";

  private static final String F_GEOMETRY_COLUMN = "f_geometry_column";

  private static final String F_GEOGRAPHY_COLUMN = "f_geography_column";

  private static final String SRID = "srid";

  private static final String GEOMETRY_COLUMNS_STMT = String.format("SELECT %s, %s, %s, %s FROM geometry_columns",
      F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, SRID);

  private static final String GEOGRAPHY_COLUMNS_STMT =
      String.format("SELECT %s, %s, %s as %s, %s FROM geography_columns", F_TABLE_SCHEMA, F_TABLE_NAME,
          F_GEOGRAPHY_COLUMN, F_GEOMETRY_COLUMN, SRID);

  private static final String GEO_COLUMNS_STMT =
      String.format("%s UNION ALL %s", GEOMETRY_COLUMNS_STMT, GEOGRAPHY_COLUMNS_STMT);

  private static final String SEGMENTS_TABLES_STMT =
      String.format("SELECT %s, %s, %s FROM geometry_columns where %s LIKE '%%__segments'", F_TABLE_SCHEMA,
          F_TABLE_NAME, F_GEOMETRY_COLUMN, F_TABLE_NAME);

  static final String FOREIGNKEYS_SEGMENT_TABLE_STMT = """
          WITH cte_fk_constraints
          AS
          ( SELECT
              unnest(con.conkey) AS "parent",
              unnest(con.confkey) AS "child",
              con.confrelid,
              con.conrelid,
              ns.nspname AS schema_name,
              cl.relname AS table_name,
              con.conname AS fk_name
            FROM
              pg_class cl
              join pg_namespace ns on cl.relnamespace = ns.oid
              join pg_constraint con on con.conrelid = cl.oid
            WHERE
              cl.relname = '%s'
              AND con.contype = 'f'
          )

          SELECT
            constr.schema_name,
            constr.table_name,
            constr.fk_name,
            child.attname AS join_column_name,
            cl.relname as referenced_table_name,
            parent.attname AS referenced_column_name
          FROM cte_fk_constraints constr
            JOIN pg_attribute parent ON
                 parent.attrelid = constr.confrelid AND parent.attnum = constr.child
            JOIN pg_class cl ON
                 cl.oid = constr.confrelid
            JOIN pg_attribute child ON
                 child.attrelid = constr.conrelid AND child.attnum = constr.parent
      """;

  private final Map<String, GeometryMetadata> geoMetadataByTableColumn;

  private final Schema schema;

  public PostgresSpatialBackendModule(Schema schema, PostgresClient postgresClient) {
    this.schema = schema;
    this.geoMetadataByTableColumn = getGeoMetadataByTableColumn(postgresClient);
  }

  private Map<String, GeometryMetadata> getGeoMetadataByTableColumn(PostgresClient postgresClient) {
    var segmentsTables = retrieveSegmentsTables(postgresClient);
    return postgresClient.fetch(GEO_COLUMNS_STMT)
        .map(row -> mapToGeoMetadata(row, segmentsTables))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        .onErrorMap(exception -> invalidConfigurationException("Retrieving geometry columns failed.", exception))
        .block();
  }

  private Map<String, GeometrySegmentsTable> retrieveSegmentsTables(PostgresClient postgresClient) {
    return postgresClient.fetch(SEGMENTS_TABLES_STMT)
        .flatMap(row -> {
          var schemaName = (String) row.get(F_TABLE_SCHEMA);
          var tableName = (String) row.get(F_TABLE_NAME);
          var geoColumn = (String) row.get(F_GEOMETRY_COLUMN);
          var joinColumns = getJoinColumns(tableName, postgresClient);

          return joinColumns
              .switchIfEmpty(Mono.error(illegalStateException("Empty join columns for segments table %s", tableName)))
              .map(joinColumnList -> createSegmentsTable(schemaName, tableName, geoColumn, joinColumnList));
        })
        .collect(Collectors.toMap(GeometrySegmentsTable::getTableName, identity()))
        .onErrorMap(exception -> invalidConfigurationException(exception.getMessage()))
        .block();
  }

  private Map.Entry<String, GeometryMetadata> mapToGeoMetadata(Map<String, Object> row,
      Map<String, GeometrySegmentsTable> segmentsTablesByName) {
    var tableName = (String) row.get(F_TABLE_NAME);
    var geoColumnName = (String) row.get(F_GEOMETRY_COLUMN);
    var srid = (int) row.get(SRID);
    var geoMetadata = GeometryMetadata.builder()
        .srid(srid)
        .segmentsTable(getSegmentsTable(tableName, geoColumnName, segmentsTablesByName))
        .build();
    return new AbstractMap.SimpleEntry<>(tableName.concat(".")
        .concat(geoColumnName), geoMetadata);
  }

  private GeometrySegmentsTable createSegmentsTable(String schemaName, String tableName, String geoColumnName,
      List<JoinColumn> joinColumns) {
    return new GeometrySegmentsTable(schemaName, tableName, geoColumnName, joinColumns);
  }

  private Mono<List<JoinColumn>> getJoinColumns(String tableName, PostgresClient postgresClient) {
    String query = String.format(FOREIGNKEYS_SEGMENT_TABLE_STMT, tableName);
    return postgresClient.fetch(query)
        .map(row -> {
          var joinColumn = new JoinColumn();
          joinColumn.setName((String) row.get("join_column_name"));
          joinColumn.setReferencedColumn((String) row.get("referenced_column_name"));
          return joinColumn;
        })
        .collect(Collectors.toList());
  }

  private Optional<GeometrySegmentsTable> getSegmentsTable(String tableName, String geoColumnName,
      Map<String, GeometrySegmentsTable> segmentsTablesByName) {
    var segmentsTableName = getSegmentsTableName(tableName, geoColumnName);
    if (segmentsTablesByName.containsKey(segmentsTableName)) {
      return Optional.of(segmentsTablesByName.get(segmentsTableName));
    }
    return Optional.empty();
  }

  @Override
  public Class<PostgresSpatialReferenceSystem> getSpatialReferenceSystemClass() {
    return PostgresSpatialReferenceSystem.class;
  }

  @Override
  public void init(Spatial spatial) {
    Map<String, ObjectType<? extends ObjectField>> objectTypes = schema.getObjectTypes();

    objectTypes.values()
        .stream()
        .map(PostgresObjectType.class::cast)
        .filter(not(PostgresObjectType::isNested))
        .map(this::createAllFieldsPerTableNameEntry)
        .forEach(field -> setSpatial(spatial, field));
  }

  private Map.Entry<String, List<PostgresObjectField>> createAllFieldsPerTableNameEntry(PostgresObjectType objectType) {
    var tableNames = objectType.getTable()
        .split("\\.");
    var table = tableNames[tableNames.length - 1];
    var fields = getFields(objectType, new CyclicRefDetector());
    return new AbstractMap.SimpleEntry<>(table, fields);
  }

  private List<PostgresObjectField> getFields(PostgresObjectType objectType, CyclicRefDetector cyclicRefDetector) {
    return objectType.getFields()
        .values()
        .stream()
        .map(field -> getPostgresObjectFields(objectType, field, cyclicRefDetector))
        .flatMap(List::stream)
        .toList();
  }

  private List<PostgresObjectField> getPostgresObjectFields(PostgresObjectType objectType, PostgresObjectField field,
      CyclicRefDetector cyclicRefDetector) {
    if (field.getTargetType() == null || !field.getTargetType()
        .isNested()) {
      return List.of(field);
    }

    if (cyclicRefDetector.isProcessed(objectType, field)) {
      return List.of();
    }

    return getFields((PostgresObjectType) field.getTargetType(), cyclicRefDetector);
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
    var geoMetadata = getGeoMetadata(tableName, objectField.getColumn());
    Optional<GeometrySegmentsTable> segmentsTable = geoMetadata.isPresent() ? geoMetadata.get()
        .getSegmentsTable() : Optional.empty();
    Integer srid = geoMetadata.map(GeometryMetadata::getSrid)
        .orElse(null);

    BiMap<Integer, String> spatialReferenceSystems =
        getSpatialReferenceSystems(spatial, tableName, objectField.getColumn());
    BiMap<Integer, Integer> equivalents = getEquivalents(spatial, spatialReferenceSystems);
    BiMap<Integer, String> bboxes = getBboxes(spatial, tableName, objectField.getColumn());

    PostgresSpatial postgresSpatial = PostgresSpatial.builder()
        .spatialReferenceSystems(spatialReferenceSystems)
        .equivalents(equivalents)
        .bboxes(bboxes)
        .srid(srid)
        .unifyInputGeometry(spatial.isUnifyInputGeometry())
        .segmentsTable(segmentsTable)
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
    return getColumnNamesPerSrid(PostgresSpatialReferenceSystem::getColumnSuffix, spatial, tableName, geometryColumn);
  }

  private BiMap<Integer, String> getBboxes(Spatial spatial, String tableName, String geometryColumn) {
    return getColumnNamesPerSrid(PostgresSpatialReferenceSystem::getBboxColumnSuffix, spatial, tableName,
        geometryColumn);
  }

  private BiMap<Integer, String> getColumnNamesPerSrid(Function<PostgresSpatialReferenceSystem, String> suffixFunc,
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

  private Optional<GeometryMetadata> getGeoMetadata(String tableName, String columnName) {
    var tableColumn = tableName + "." + columnName;
    return Optional.ofNullable(geoMetadataByTableColumn.get(tableColumn));
  }

  private Integer getSrid(String tableName, String columnName) {
    var geometryTableColumn = tableName + "." + columnName;
    var geoMetadata = geoMetadataByTableColumn.get(geometryTableColumn);
    return geoMetadata != null ? geoMetadata.getSrid() : null;
  }
}
