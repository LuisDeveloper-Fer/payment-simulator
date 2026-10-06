# API — Payment Simulator

Base: http://localhost:8080. Content-Type: application/json.

| Método | Ruta | Contrato |
| --- | --- | --- |
| POST | `/api/payments` | 201 nuevo; 200 replay; 409 conflicto; 504 simulado |
| GET | `/api/payments/{key}` | Resultado durable |
| GET | `/api/payments` | Últimos 50 pagos |
| POST | `/api/payments/{key}/reversals` | Reverso idempotente; 409 si DECLINED |

## Request
```json
{
  "amount": 125.5,
  "currency": "PEN",
  "scenario": "LOST_RESPONSE"
}
```

## Response (campos relevantes)
```json
{
  "id": "payment-demo-001",
  "status": "APPROVED",
  "amount": 125.5,
  "currency": "PEN",
  "responseCode": "00"
}
```

## Errores
400 indica validación o formato inválido; 404 indica recurso inexistente. Los estados específicos se detallan en la tabla. ProblemDetail se usa para errores de negocio y validación donde aplica; autenticación puede devolver cuerpo vacío y WWW-Authenticate. Los clientes deben usar códigos, no parsear mensajes internos.

Idempotency-Key obligatorio: 1–64 caracteres alfanuméricos, guion o underscore. Reusar la clave requiere igual importe, moneda y escenario. LOST_RESPONSE confirma el pago y devuelve 504 solamente en su creación; consulta y replay devuelven el pago existente.
Importe positivo: máximo 9 enteros y 2 decimales; moneda PEN, USD o EUR.
