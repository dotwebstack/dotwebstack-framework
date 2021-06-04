package org.dotwebstack.framework.backend.postgres.query.model;

import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.filter.EqualsFilterCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class PostgresObjectRequestTest {

  @Test
  void addFilterCriteria_createsNoObjectField_forSimpleFilter() {
    var beerTypeConfiguration = createBeerTypeConfiguration(Optional.empty());

    var postgresObjectRequest = PostgresObjectRequest.builder()
        .typeConfiguration(beerTypeConfiguration)
        .scalarFields(List.of(beerTypeConfiguration.getFields().get("name")))
        .build();

    FilterCriteria filterCriteria = createEqualsFilterCriteria("", beerTypeConfiguration.getFields().get("name"));

    postgresObjectRequest.addFilterCriteria(List.of(filterCriteria));

    assertObjectRequest(postgresObjectRequest, "name", Origin.REQUESTED);
  }

  @Test
  void addFilterCriteria_createsOneObjectField_forOneLevelNestedFilter() {
    var breweryTypeCOnfiguration = createBreweryTypeConfiguration(Optional.empty());
    var beerTypeConfiguration = createBeerTypeConfiguration(Optional.of(breweryTypeCOnfiguration));

    var postgresObjectRequest = PostgresObjectRequest.builder()
        .typeConfiguration(beerTypeConfiguration)
        .scalarFields(List.of(beerTypeConfiguration.getFields().get("name")))
        .build();

    FilterCriteria filterCriteria = createEqualsFilterCriteria("brewery.brewName", breweryTypeCOnfiguration.getFields().get("brewName"));

    postgresObjectRequest.addFilterCriteria(List.of(filterCriteria));

    assertObjectFields(postgresObjectRequest, "brewery", "brewName", Origin.FILTERING);
  }

  @Test
  void addFilterCriteria_createsTwoObjectFields_forTwoLevelNestedFilter() {
    var addressTypeConfiguration = createAddressTypeConfiguration();
    var breweryTypeCOnfiguration = createBreweryTypeConfiguration(Optional.of(addressTypeConfiguration));
    var beerTypeConfiguration = createBeerTypeConfiguration(Optional.of(breweryTypeCOnfiguration));

    var postgresObjectRequest = PostgresObjectRequest.builder()
        .typeConfiguration(beerTypeConfiguration)
        .scalarFields(List.of(beerTypeConfiguration.getFields().get("name")))
        .build();

    FilterCriteria filterCriteria = createEqualsFilterCriteria("brewery.address.city", addressTypeConfiguration.getFields().get("city"));

    postgresObjectRequest.addFilterCriteria(List.of(filterCriteria));

    assertObjectFields(postgresObjectRequest, "brewery.address", "city", Origin.FILTERING);
  }

  @Test
  void addFilterCriteria_createsNoObjectFields_forTwoLevelNestedFilterWhenObjectFieldsAlreadyExist() {
    var addressTypeConfiguration = createAddressTypeConfiguration();
    var breweryTypeCOnfiguration = createBreweryTypeConfiguration(Optional.of(addressTypeConfiguration));
    var beerTypeConfiguration = createBeerTypeConfiguration(Optional.of(breweryTypeCOnfiguration));

    var postgresObjectRequest = PostgresObjectRequest.builder()
        .typeConfiguration(beerTypeConfiguration)
        .scalarFields(List.of(beerTypeConfiguration.getFields().get("name")))
        .build();

    ObjectFieldConfiguration breweryObjectField = createObjectFieldForBrewery(beerTypeConfiguration, breweryTypeCOnfiguration,
            addressTypeConfiguration);
    postgresObjectRequest.getObjectFields().add(breweryObjectField);
    postgresObjectRequest.getObjectFieldsByType().put("brewery", breweryObjectField);

    FilterCriteria filterCriteria = createEqualsFilterCriteria("brewery.address.city", addressTypeConfiguration.getFields().get("city"));

    postgresObjectRequest.addFilterCriteria(List.of(filterCriteria));

    assertObjectFields(postgresObjectRequest, "brewery.address", "city", Origin.FILTERING);
  }

  private void assertObjectFields(PostgresObjectRequest objectRequest, String fieldPath, String fieldName, Origin origin) {
    assertThat(objectRequest.getObjectFields().size(), is(1));
    var objectField = getObjectField(StringUtils.split(fieldPath, "."), objectRequest);
    var scalarFields = objectField.getObjectRequest().getScalarFields();
    assertThat(scalarFields, hasSize(1));
    var scalarField = (PostgresFieldConfiguration) scalarFields.get(0);
    assertThat(scalarField.getName(), is(fieldName));
    assertThat(scalarField.getOrigins(), hasItem(origin));
  }

  private void assertObjectRequest(ObjectRequest objectRequest, String fieldName, Origin origin) {
    var scalarFields = objectRequest.getScalarFields();
    assertThat(scalarFields, hasSize(1));
    var scalarField = (PostgresFieldConfiguration) scalarFields.get(0);
    assertThat(scalarField.getName(), is(fieldName));
    assertThat(scalarField.getOrigins(), hasItem(origin));
  }

  private ObjectFieldConfiguration getObjectField(String[] fieldPaths, ObjectRequest objectRequest) {
    var objectField = getObjectField(objectRequest.getObjectFields(),fieldPaths[0])
        .orElseThrow(IllegalStateException::new);

    fieldPaths = Arrays.copyOfRange(fieldPaths, 1, fieldPaths.length);
    if(fieldPaths.length > 0) {
      return getObjectField(fieldPaths, objectField.getObjectRequest());
    }
    return objectField;
  }

  private Optional<ObjectFieldConfiguration> getObjectField(List<ObjectFieldConfiguration> objectFields, String name) {
    assertThat(objectFields, hasSize(1));
    return objectFields.stream().filter(objectFieldConfiguration -> objectFieldConfiguration.getField().getName().equals(name)).findFirst();
  }

  private EqualsFilterCriteria createEqualsFilterCriteria(String fieldPath, PostgresFieldConfiguration fieldConfiguration) {
    return EqualsFilterCriteria.builder()
        .fieldPath(fieldPath)
        .field(fieldConfiguration)
        .build();
  }

  private PostgresTypeConfiguration createBeerTypeConfiguration(Optional<PostgresTypeConfiguration> breweryTypeConfiguration) {
    var beerNameFieldConfiguration = new PostgresFieldConfiguration();
    beerNameFieldConfiguration.setColumn("nameColumn");
    beerNameFieldConfiguration.setName("name");

    var beerTypeConfiguration = new PostgresTypeConfiguration();
    beerTypeConfiguration.setKeys(List.of());
    beerTypeConfiguration.setTable("BeerTable");
    beerTypeConfiguration.setName("Beer");
    Map<String, PostgresFieldConfiguration> beerFields = new HashMap<>();
    beerFields.put("name", beerNameFieldConfiguration);

    if(!breweryTypeConfiguration.isEmpty()) {
      var breweryFieldConfiguration = new PostgresFieldConfiguration();
      breweryFieldConfiguration.setJoinColumns(List.of(new JoinColumn()));
      breweryFieldConfiguration.setColumn("brewery");
      breweryFieldConfiguration.setName("brewery");
      breweryFieldConfiguration.setTypeConfiguration(breweryTypeConfiguration.get());
      beerFields.put("brewery", breweryFieldConfiguration);
    }

    beerTypeConfiguration.setFields(beerFields);
    beerNameFieldConfiguration.setTypeConfiguration(beerTypeConfiguration);
    return beerTypeConfiguration;
  }

  private PostgresTypeConfiguration createBreweryTypeConfiguration(Optional<PostgresTypeConfiguration> addressTypeConfiguration) {
    var breweryNameFieldConfiguration = new PostgresFieldConfiguration();
    breweryNameFieldConfiguration.setColumn("brewName");
    breweryNameFieldConfiguration.setName("brewName");

    var breweryTypeConfiguration = new PostgresTypeConfiguration();
    breweryTypeConfiguration.setKeys(List.of());
    breweryTypeConfiguration.setTable("BreweryTable");
    breweryTypeConfiguration.setName("Brewery");
    Map<String, PostgresFieldConfiguration> breweryFields = new HashMap<>();
    breweryFields.put("brewName", breweryNameFieldConfiguration);

    if(!addressTypeConfiguration.isEmpty()) {
      var addressFieldConfiguration = new PostgresFieldConfiguration();
      addressFieldConfiguration.setJoinColumns(List.of(new JoinColumn()));
      addressFieldConfiguration.setColumn("address");
      addressFieldConfiguration.setName("address");
      addressFieldConfiguration.setTypeConfiguration(addressTypeConfiguration.get());
      breweryFields.put("address", addressFieldConfiguration);
    }

    breweryTypeConfiguration.setFields(breweryFields);

    return breweryTypeConfiguration;
  }

  private PostgresTypeConfiguration createAddressTypeConfiguration() {
    var addressCityFieldConfiguration = new PostgresFieldConfiguration();
    addressCityFieldConfiguration.setName("city");

    var addressTypeConfiguration = new PostgresTypeConfiguration();
    addressTypeConfiguration.setKeys(List.of());
    addressTypeConfiguration.setTable("AddressTable");
    addressTypeConfiguration.setName("Address");
    addressTypeConfiguration.setFields(Map.of("city", addressCityFieldConfiguration));

    return addressTypeConfiguration;
  }

  private ObjectFieldConfiguration createObjectFieldForBrewery(PostgresTypeConfiguration beerTypeConfiguration, PostgresTypeConfiguration breweryTypeConfiguration, PostgresTypeConfiguration addressTypeConfiguration){
    PostgresFieldConfiguration breweryFieldConfiguration = beerTypeConfiguration.getFields().get("brewery");
    PostgresFieldConfiguration addressFieldConfiguration = breweryTypeConfiguration.getFields().get("address");
    PostgresFieldConfiguration cityFieldConfiguration = addressTypeConfiguration.getFields().get("city");

    var addressObjectField = createObjectFieldConfiguration(addressFieldConfiguration, addressTypeConfiguration);
      addressObjectField.getObjectRequest().addScalarField(cityFieldConfiguration);

    var breweryObjectField = createObjectFieldConfiguration(breweryFieldConfiguration, breweryTypeConfiguration);
    breweryObjectField.getObjectRequest().getObjectFields().add(addressObjectField);

    return breweryObjectField;
  }

  private ObjectFieldConfiguration createObjectFieldConfiguration(PostgresFieldConfiguration fieldConfiguration,
      PostgresTypeConfiguration typeConfiguration) {
    var objectRequest = ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .build();

    return ObjectFieldConfiguration.builder()
        .field(fieldConfiguration)
        .objectRequest(objectRequest)
        .build();
  }
}
