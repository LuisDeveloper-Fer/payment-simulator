package dev.portfolio.infrastructure;

import dev.portfolio.domain.Payment;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface PaymentRepository extends JpaRepository<Payment, String> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Payment p where p.id=:id")
  Optional<Payment> locked(String id);
}
