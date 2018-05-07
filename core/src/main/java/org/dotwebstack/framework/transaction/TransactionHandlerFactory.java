package org.dotwebstack.framework.transaction;

import lombok.NonNull;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.stereotype.Service;

@Service
public class TransactionHandlerFactory {

  public TransactionHandler newTransactionHandler(@NonNull Transaction transaction,
      @NonNull Model model) {
    return new TransactionHandler(new SailRepository(new MemoryStore()), transaction, model);
  }

}
