package dev.portfolio.application;

import dev.portfolio.domain.Payment;
import dev.portfolio.infrastructure.PaymentRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Payments {
  private final PaymentRepository repo;
  private final EntityManager em;
  private final TransactionTemplate tx;

  public Payments(PaymentRepository repo, EntityManager em, TransactionTemplate tx) {
    this.repo = repo;
    this.em = em;
    this.tx = tx;
  }

  public record Result(Payment payment, boolean replay) {}

  public Result create(String key, BigDecimal amount, String currency, String scenario) {
    var existing = repo.findById(key);
    if (existing.isPresent()) return replay(existing.get(), amount, currency, scenario);
    try {
      Payment created =
          tx.execute(
              status -> {
                var payment = new Payment(key, amount, currency, scenario);
                em.persist(payment);
                em.flush();
                return payment;
              });
      return new Result(created, false);
    } catch (RuntimeException failure) {
      // A concurrent insert can win. The failed transaction is already rolled back.
      var winner = repo.findById(key);
      if (winner.isPresent()) return replay(winner.get(), amount, currency, scenario);
      throw failure;
    }
  }

  private Result replay(Payment p, BigDecimal amount, String currency, String scenario) {
    if (p.amount.compareTo(amount) != 0
        || !p.currency.equals(currency)
        || !p.scenario.equals(scenario))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Idempotency key already used with different input");
    return new Result(p, true);
  }

  public Payment reverse(String id) {
    return tx.execute(
        status -> {
          var p =
              repo.locked(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
          if (p.status.equals("DECLINED"))
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Declined payments cannot be reversed");
          if (!p.status.equals("REVERSED")) {
            p.status = "REVERSED";
            p.responseCode = "00";
            p.reversedAt = java.time.Instant.now();
          }
          return p;
        });
  }
}
