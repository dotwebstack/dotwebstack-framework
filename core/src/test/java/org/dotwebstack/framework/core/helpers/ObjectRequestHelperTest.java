package org.dotwebstack.framework.core.helpers;

import static org.dotwebstack.framework.core.helpers.ObjectRequestHelper.addKeyFields;
import static org.dotwebstack.framework.core.query.model.SortDirection.ASC;
import static org.dotwebstack.framework.core.query.model.SortDirection.DESC;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.SortDirection;
import org.dotwebstack.framework.core.testhelpers.TestObjectField;
import org.dotwebstack.framework.core.testhelpers.TestObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObjectRequestHelperTest {

  @Test
  void addSortFields_doesNotModifyCollectionRequest_forExistingScalarField() {
    var nameObjectField = new TestObjectField();
    nameObjectField.setName("name");
    var nameSortAsc = getSortCriteria(List.of(nameObjectField), ASC);

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(getObjectRequest(null))
        .sortCriterias(List.of(nameSortAsc))
        .build();

    var expectedScalarFieldSize = collectionRequest.getObjectRequest()
        .getScalarFields()
        .size();

    ObjectRequestHelper.addSortFields(collectionRequest);

    assertThat(collectionRequest.getObjectRequest()
        .getScalarFields()
        .size(), is(expectedScalarFieldSize));
  }

  @ParameterizedTest
  @CsvSource({"nestedField", "nestedField.$system"})
  void addSortFields_doesNotModifyCollectionRequest_forExistingObjectField(String resultKey) {
    var nestedObjectField = new TestObjectField();
    nestedObjectField.setName("nestedField");

    var idObjectField = new TestObjectField();
    idObjectField.setName("nestedField_id");

    var nestedObjectType = new TestObjectType();
    nestedObjectType.setFields(Map.of("nestedField_id", idObjectField));
    nestedObjectType.setTable("NestedTable");
    nestedObjectType.setName("Nested");
    idObjectField.setObjectType(nestedObjectType);

    var breweryIdSortCriteria = getSortCriteria(List.of(nestedObjectField, idObjectField), DESC);

    Map<FieldRequest, ObjectRequest> objectFieldMap = new HashMap<>();

    objectFieldMap.put(FieldRequest.builder()
        .name("nestedField")
        .resultKey(resultKey)
        .build(), createNestedObjectRequest());

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(getObjectRequest(null, objectFieldMap))
        .sortCriterias(List.of(breweryIdSortCriteria))
        .build();

    var expectedObjectFieldsSize = collectionRequest.getObjectRequest()
        .getObjectFields()
        .size();

    ObjectRequestHelper.addSortFields(collectionRequest);

    assertThat(collectionRequest.getObjectRequest()
        .getObjectFields()
        .size(), is(expectedObjectFieldsSize));
  }

  @Test
  void addSortFields_addsScalarFieldToObjectRequest_forNonExistingScalarField() {
    var abvObjectField = new TestObjectField();
    abvObjectField.setName("abv");
    var nameSortAsc = getSortCriteria(List.of(abvObjectField), ASC);

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(getObjectRequest(null))
        .sortCriterias(List.of(nameSortAsc))
        .build();

    ObjectRequestHelper.addSortFields(collectionRequest);

    assertThat(collectionRequest.getObjectRequest()
        .getScalarFields()
        .size(), is(4));
    assertThat(collectionRequest.getObjectRequest()
        .getScalarFields()
        .stream()
        .filter(field -> field.getName()
            .equals("abv"))
        .findFirst()
        .orElseThrow()
        .getName(), is("abv"));
  }

  @Test
  void addSortFields_addsObjectRequest_forNonNestedObject() {
    var breweryObjectField = new TestObjectField();
    breweryObjectField.setName("brewery");

    var idObjectField = new TestObjectField();
    idObjectField.setName("id");

    var breweryObjectType = new TestObjectType();
    breweryObjectType.setFields(Map.of("id", idObjectField));
    breweryObjectType.setTable("BreweryTable");
    breweryObjectType.setName("Brewery");
    idObjectField.setObjectType(breweryObjectType);

    var breweryIdSortCriteria = getSortCriteria(List.of(breweryObjectField, idObjectField), DESC);

    var collectionRequest = CollectionRequest.builder()
        .objectRequest(getObjectRequest(null))
        .sortCriterias(List.of(breweryIdSortCriteria))
        .build();

    ObjectRequestHelper.addSortFields(collectionRequest);

    assertThat(collectionRequest.getObjectRequest()
        .getObjectFields()
        .size(), is(1));

    var newObjectRequest = collectionRequest.getObjectRequest()
        .getObjectFields()
        .entrySet()
        .stream()
        .findFirst()
        .orElseThrow();

    assertThat(newObjectRequest.getKey()
        .getResultKey(), is("brewery.$system"));
    assertThat(newObjectRequest.getValue()
        .getScalarFields()
        .stream()
        .findFirst()
        .orElseThrow()
        .getName(), is("id"));
  }

  @Test
  void addKeyFields_doesNotModifyCollectionRequest_forExistingScalarField() {
    List<KeyCriteria> keyCriterias = new ArrayList<>();

    var identifierObjectField = new TestObjectField();
    identifierObjectField.setName("identifier");
    keyCriterias.add(KeyCriteria.builder()
        .fieldPath(List.of(identifierObjectField))
        .value("id-1")
        .build());

    var objectRequest = getObjectRequest(keyCriterias);

    var expectedScalarFieldsSize = objectRequest.getScalarFields()
        .size();

    addKeyFields(objectRequest);

    assertThat(objectRequest.getScalarFields()
        .size(), is(expectedScalarFieldsSize));
  }

  @Test
  void addKeyFields_doesNotModifyCollectionRequest_forExistingObjectField() {
    List<KeyCriteria> keyCriterias = new ArrayList<>();

    var nestedObjectField = new TestObjectField();
    nestedObjectField.setName("nestedField");

    var idObjectField = new TestObjectField();
    idObjectField.setName("nestedField_id");

    keyCriterias.add(KeyCriteria.builder()
        .fieldPath(List.of(nestedObjectField, idObjectField))
        .value("id-1")
        .build());

    Map<FieldRequest, ObjectRequest> objectFieldMap = new HashMap<>();

    objectFieldMap.put(FieldRequest.builder()
        .name("nestedField")
        .resultKey("nestedField")
        .build(), createNestedObjectRequest());

    var objectRequest = getObjectRequest(keyCriterias, objectFieldMap);

    var expectedScalarFieldsSize = objectRequest.getScalarFields()
        .size();

    addKeyFields(objectRequest);

    assertThat(objectRequest.getScalarFields()
        .size(), is(expectedScalarFieldsSize));
  }

  @Test
  void addKeyFields_addsScalarFieldToObjectRequest_forNonExistingScalarField() {
    List<KeyCriteria> keyCriterias = new ArrayList<>();

    var identifierObjectField = new TestObjectField();
    identifierObjectField.setName("id");
    keyCriterias.add(KeyCriteria.builder()
        .fieldPath(List.of(identifierObjectField))
        .value("id-1")
        .build());


    var objectRequest = getObjectRequest(keyCriterias);

    addKeyFields(objectRequest);

    assertThat(objectRequest.getScalarFields()
        .size(), is(4));
    assertThat(objectRequest.getScalarFields()
        .stream()
        .filter(field -> field.getName()
            .equals("id"))
        .findFirst()
        .orElseThrow()
        .getName(), is("id"));
  }

  @Test
  void addKeyFields_addsObjectField_forNonNestedObject() {
    var breweryObjectField = new TestObjectField();
    breweryObjectField.setName("brewery");

    var idObjectField = new TestObjectField();
    idObjectField.setName("id");

    var breweryObjectType = new TestObjectType();
    breweryObjectType.setFields(Map.of("id", idObjectField));
    breweryObjectType.setTable("BreweryTable");
    breweryObjectType.setName("Brewery");

    idObjectField.setObjectType(breweryObjectType);

    List<KeyCriteria> keyCriterias = new ArrayList<>();
    keyCriterias.add(KeyCriteria.builder()
        .fieldPath(List.of(breweryObjectField, idObjectField))
        .value("id-1")
        .build());

    var objectRequest = getObjectRequest(keyCriterias);

    addKeyFields(objectRequest);

    assertThat(objectRequest.getObjectFields()
        .size(), is(1));

    var newBreweryObjectField = objectRequest.getObjectFields()
        .entrySet()
        .stream()
        .filter(objectField -> objectField.getKey()
            .getName()
            .equals("brewery"))
        .findFirst()
        .orElseThrow();

    assertThat(newBreweryObjectField.getKey()
        .getName(), is("brewery"));
    assertThat(((TestObjectType) newBreweryObjectField.getValue()
        .getObjectType()).getTable(), is("BreweryTable"));
    assertThat(newBreweryObjectField.getValue()
        .getScalarFields()
        .size(), is(1));
    assertThat(newBreweryObjectField.getValue()
        .getScalarFields()
        .stream()
        .findFirst()
        .orElseThrow()
        .getName(), is("id"));
  }

  @Test
  void addKeyFields_addsObjectField_forNestedObject() {
    var historyObjectField = new TestObjectField();
    historyObjectField.setName("history");

    var versieObjectField = new TestObjectField();
    versieObjectField.setName("versie");

    var historyObjectType = new TestObjectType();
    historyObjectType.setFields(Map.of("versie", versieObjectField));
    historyObjectType.setTable(null);
    historyObjectType.setName("History");

    versieObjectField.setObjectType(historyObjectType);

    List<KeyCriteria> keyCriterias = new ArrayList<>();
    keyCriterias.add(KeyCriteria.builder()
        .fieldPath(List.of(historyObjectField, versieObjectField))
        .value("1")
        .build());

    var objectRequest = getObjectRequest(keyCriterias);

    addKeyFields(objectRequest);

    assertThat(objectRequest.getObjectFields()
        .size(), is(1));

    var newBreweryObjectField = objectRequest.getObjectFields()
        .entrySet()
        .stream()
        .filter(objectField -> objectField.getKey()
            .getName()
            .equals("history"))
        .findFirst()
        .orElseThrow();

    assertThat(newBreweryObjectField.getKey()
        .getName(), is("history"));
    assertThat(Optional.ofNullable(((TestObjectType) newBreweryObjectField.getValue()
        .getObjectType()).getTable()), is(Optional.empty()));
    assertThat(newBreweryObjectField.getValue()
        .getScalarFields()
        .size(), is(1));
    assertThat(newBreweryObjectField.getValue()
        .getScalarFields()
        .stream()
        .findFirst()
        .orElseThrow()
        .getName(), is("versie"));
  }

  private SortCriteria getSortCriteria(List<ObjectField> nameObjectField, SortDirection sortDirection) {
    return SortCriteria.builder()
        .fieldPath(nameObjectField)
        .direction(sortDirection)
        .build();
  }

  private ObjectRequest getObjectRequest(List<KeyCriteria> keyCriterias) {
    return getObjectRequest(keyCriterias, new HashMap<>());
  }

  private ObjectRequest getObjectRequest(List<KeyCriteria> keyCriterias,
      Map<FieldRequest, ObjectRequest> objectFields) {
    var objectType = mock(ObjectType.class);

    return ObjectRequest.builder()
        .objectType(objectType)
        .scalarFields(new ArrayList<>(List.of(FieldRequest.builder()
            .name("identifier")
            .build(),
            FieldRequest.builder()
                .name("name")
                .build(),
            FieldRequest.builder()
                .name("soldPerYear")
                .build())))
        .objectFields(objectFields)
        .keyCriterias(keyCriterias)
        .build();
  }

  private ObjectRequest createNestedObjectRequest() {
    var idObjectField = new TestObjectField();
    idObjectField.setName("nestedField_id");

    var nestedObjectType = new TestObjectType();
    nestedObjectType.setFields(Map.of("nestedField_id", idObjectField));
    nestedObjectType.setTable("NestedTable");
    nestedObjectType.setName("Nested");

    idObjectField.setObjectType(nestedObjectType);

    return ObjectRequest.builder()
        .objectType(nestedObjectType)
        .scalarFields(new ArrayList<>(List.of(FieldRequest.builder()
            .name("nestedField_id")
            .resultKey("nestedField_id")
            .build())))
        .build();
  }
}
