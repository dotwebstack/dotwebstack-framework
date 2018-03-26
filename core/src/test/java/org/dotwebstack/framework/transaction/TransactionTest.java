package org.dotwebstack.framework.transaction;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.Flow;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Flow flow;

  @Mock
  private Collection<Parameter> parameters;

  @Test
  public void build_ThrowsException_WithMissingIdentifier() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new Transaction.Builder(null).build();
  }

  @Test
  public void build_CreatesTransaction_WithValidData() {
    // Act
    Transaction transaction = new Transaction.Builder(DBEERPEDIA.TRANSACTION).flow(flow)
        .parameters(parameters).build();

    // Assert
    assertThat(transaction.getIdentifier(), equalTo(DBEERPEDIA.TRANSACTION));
    assertThat(transaction.getFlow(), notNullValue());
    assertThat(transaction.getParameters(), notNullValue());
  }

}
