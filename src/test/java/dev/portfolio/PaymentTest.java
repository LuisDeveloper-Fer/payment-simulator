package dev.portfolio;
import dev.portfolio.application.Payments;
import dev.portfolio.infrastructure.PaymentRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
@SpringBootTest class PaymentTest {
 @Autowired Payments payments;@Autowired PaymentRepository repo;
 @Test void concurrentDuplicatePaymentHasOneDurableResult() throws Exception {
  String key=UUID.randomUUID().toString();
  try(var executor=Executors.newFixedThreadPool(8)){
   var tasks=new ArrayList<Callable<Payments.Result>>();
   for(int i=0;i<8;i++)tasks.add(()->payments.create(key,new BigDecimal("10.00"),"PEN","APPROVED"));
   var results=executor.invokeAll(tasks);long created=0;for(var r:results){if(!r.get().replay())created++;assertThat(r.get().payment().status).isEqualTo("APPROVED");}
   assertThat(created).isEqualTo(1);
  }
  assertThat(payments.reverse(key).status).isEqualTo("REVERSED");assertThat(payments.reverse(key).status).isEqualTo("REVERSED");
  assertThatThrownBy(()->payments.create(key,new BigDecimal("20.00"),"PEN","APPROVED")).hasMessageContaining("409");
 }
 @Test void declinedPaymentCannotBeReversed(){var key=UUID.randomUUID().toString();payments.create(key,BigDecimal.ONE,"USD","DECLINED");assertThatThrownBy(()->payments.reverse(key)).hasMessageContaining("409");}
}
