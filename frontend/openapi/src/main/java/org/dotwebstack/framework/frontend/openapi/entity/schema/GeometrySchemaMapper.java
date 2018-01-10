package org.dotwebstack.framework.frontend.openapi.entity.schema;

import com.google.common.collect.ImmutableSet;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import java.util.Collection;
import java.util.Set;
import lombok.NonNull;
import nl.kadaster.pdok.grid.commons.geo.Crs;
import nl.kadaster.pdok.grid.commons.geo.Projector;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntityContext;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GeometrySchemaMapper extends ObjectSchemaMapper {

  private static final Logger LOG = LoggerFactory.getLogger(GeometrySchemaMapper.class);

  @Override
  public Object mapGraphValue(ObjectProperty schema, GraphEntityContext entityContext,
      ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {

    String ldPath =
        schema.getVendorExtensions().get(OpenApiSpecificationExtensions.LDPATH).toString();
    LdPathExecutor ldPathExecutor = entityContext.getLdPathExecutor();
    Collection<Value> queryResult = ldPathExecutor.ldPathQuery(valueContext.getValue(), ldPath);

    if (queryResult.isEmpty()) {
      if (!schema.getRequired()) {
        return null;
      }
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath expression for a required object property ('%s') yielded no result.", ldPath));
    }

    if (queryResult.size() > 1) {
      throw new SchemaMapperRuntimeException(String.format(
          "LDPath expression for object property ('%s') yielded multiple elements.", ldPath));
    }

    Value wktValue = queryResult.iterator().next();

    if (!isSupportedLiteral(wktValue)) {
      throw new SchemaMapperRuntimeException(
          String.format("LDPath query '%s' yielded a value which is not a literal with type <%s>.",
              ldPath, GEO.WKT_LITERAL.stringValue()));
    }

    return project(parseWktLiteral((Literal) wktValue), Crs.ETRS89.getEpsgCode());
  }

  @Override
  public boolean supports(@NonNull Property schema) {
    return schema instanceof ObjectProperty
        && "geometry".equals(schema.getVendorExtensions().get("x-dotwebstack-type"));
  }

  @Override
  protected Set<IRI> getSupportedDataTypes() {
    return ImmutableSet.of(GEO.WKT_LITERAL);
  }


  /**
   * Project a given {@link Geometry} to the requested coordinate reference system (CRS). Assumes
   * that the given {@link Geometry} is in the ETRS89 CRS.
   *
   * <p>
   * supports projection to epsg:28992 (RD).
   * </p>
   * <p>
   * <strong>Note: epsg:4326 is WSG84 and assumed to be the same as ETRS89 (which it isn't, but for
   * geometry in the Netherlands these are almost the same. (ETRS89 is actually epsg:4258)).
   * </strong>
   * </p>
   *
   * @param etrs89Geometry the {@link Geometry} to project (in the ETRS98 CRS))
   * @param epsgCode the CRS to project to identified by its EPSG code (e.g. "epsg:28992").
   * @return the projected Geometry.
   */
  private Geometry project(Geometry etrs89Geometry, String epsgCode) {
    if (epsgCode != null) {
      return (new Projector()).project(etrs89Geometry, Crs.ETRS89, Crs.valueOfEpsgCode(epsgCode));
    }

    return etrs89Geometry;
  }

  private Geometry parseWktLiteral(Literal wktLiteral) {
    try {
      return new WKTReader().read(wktLiteral.stringValue());
    } catch (ParseException ex) {
      throw new SchemaMapperRuntimeException(
          String.format("Cannot parse WKT string (%s).", wktLiteral.stringValue()), ex);
    }
  }

}
