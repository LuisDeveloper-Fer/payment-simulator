# ADR 001 — CONSISTENCY

Estado: aceptada · 2026-10-05

## Contexto
Una respuesta perdida puede causar que el cliente repita un pago ya procesado. El simulador reproduce esa incertidumbre y la resuelve con una identidad persistente y un estado consultable.

## Decisión
La base de datos arbitra la idempotencia. El insert corre en su propia transacción; ante una carrera se consulta al ganador después del rollback. El reverso bloquea la fila, permite APPROVED → REVERSED y conserva el resultado al repetirse.

## Alternativas
Separar más microservicios o incorporar un broker agregaría despliegue y operación fuera del objetivo. Concentrar todo en el controlador dificultaría probar fallos y razonar sobre el contrato. Se elige una aplicación pequeña con API, casos de uso y adaptadores diferenciados.

## Consecuencias
No mueve dinero ni implementa contabilidad. LOST_RESPONSE devuelve un 504 después del commit: modela incertidumbre, no un timeout TCP. La idempotencia dura mientras exista el registro. Las pruebas locales H2 no garantizan por sí solas el comportamiento en PostgreSQL.

## Validación
Concurrencia con ocho solicitudes produce una creación; conflicto de payload; reverso repetible y rechazo del reverso de un pago declinado.
