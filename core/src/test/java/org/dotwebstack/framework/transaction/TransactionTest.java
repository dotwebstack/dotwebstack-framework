package org.dotwebstack.framework.transaction;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

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

  @Test
  public void build_CreatesTransaction_WithValidData() {
    // Act
    Transaction transaction = new Transaction.Builder(DBEERPEDIA.TRANSACTION).flow(flow).build();

    // Assert
    assertThat(transaction.getIdentifier(), equalTo(DBEERPEDIA.TRANSACTION));
    assertThat(site.getDomain(), equalTo(DBEERPEDIA.DOMAIN.stringValue()));
    assertThat(site.isMatchAllDomain(), equalTo(false));
  }
}
