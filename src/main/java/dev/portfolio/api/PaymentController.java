package dev.portfolio.api;
import dev.portfolio.application.Payments;
import dev.portfolio.domain.Payment;
import dev.portfolio.infrastructure.PaymentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
@RestController @RequestMapping("/api/payments")
public class PaymentController {
 private final Payments service;private final PaymentRepository repo;
 public PaymentController(Payments service,PaymentRepository repo){this.service=service;this.repo=repo;}
 public record Request(@NotNull @DecimalMin("0.01") @Digits(integer=9,fraction=2) BigDecimal amount,@NotNull @Pattern(regexp="PEN|USD|EUR") String currency,@NotNull @Pattern(regexp="APPROVED|DECLINED|LOST_RESPONSE") String scenario){}
 @PostMapping public ResponseEntity<?> create(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody Request body){
  if(!key.matches("[a-zA-Z0-9_-]{1,64}"))throw new IllegalArgumentException("Idempotency-Key must be 1-64 alphanumeric, underscore or hyphen characters");
  var result=service.create(key,body.amount(),body.currency(),body.scenario());
  if(body.scenario().equals("LOST_RESPONSE")&&!result.replay())return ResponseEntity.status(504).body(ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT,"Simulated lost provider response. Query payment by key before retrying."));
  return ResponseEntity.status(result.replay()?200:201).location(URI.create("/api/payments/"+key)).header("Idempotent-Replayed",Boolean.toString(result.replay())).body(result.payment());
 }
 @GetMapping("/{id}") public Payment get(@PathVariable String id){return repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 @GetMapping public List<Payment> list(){return repo.findAll(org.springframework.data.domain.PageRequest.of(0,50,org.springframework.data.domain.Sort.by("createdAt").descending())).getContent();}
 @PostMapping("/{id}/reversals") public Payment reverse(@PathVariable String id){return service.reverse(id);}
}
