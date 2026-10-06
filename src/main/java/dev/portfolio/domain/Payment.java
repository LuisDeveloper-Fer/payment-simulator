package dev.portfolio.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity
public class Payment {
 @Id public String id;
 @Column(nullable=false,precision=19,scale=2) public BigDecimal amount;
 @Column(nullable=false) public String currency;
 @Column(nullable=false) public String scenario;
 @Column(nullable=false) public String status;
 @Column(nullable=false) public String responseCode;
 public Instant createdAt;
 public Instant reversedAt;
 @Version public long version;
 protected Payment(){}
 public Payment(String id,BigDecimal amount,String currency,String scenario){this.id=id;this.amount=amount;this.currency=currency;this.scenario=scenario;createdAt=Instant.now();status=scenario.equals("DECLINED")?"DECLINED":"APPROVED";responseCode=scenario.equals("DECLINED")?"51":"00";}
}
