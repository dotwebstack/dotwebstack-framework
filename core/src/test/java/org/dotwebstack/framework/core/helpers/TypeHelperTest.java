package org.dotwebstack.framework.core.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import graphql.Scalars;
import graphql.language.NonNullType;
import org.junit.jupiter.api.Test;

public class TypeHelperTest {

  @Test
  public void createNonNullType_returns_typeForScalar() {
    // Arrange / Act
    NonNullType type = TypeHelper.createNonNullType(Scalars.GraphQLString);

    // Assert
    assertEquals(Scalars.GraphQLString.getName(), TypeHelper.getTypeName(type));
  }
}
