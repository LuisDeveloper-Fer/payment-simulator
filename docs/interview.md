# Demostración en cinco minutos

## Problema — 30 segundos
Una respuesta perdida puede causar que el cliente repita un pago ya procesado. El simulador reproduce esa incertidumbre y la resuelve con una identidad persistente y un estado consultable.

## Experimento — 2 minutos
Usa LOST_RESPONSE: el servicio guarda APPROVED y devuelve 504. Consulta por clave y repite el mismo request: obtendrás el mismo pago. Después solicita un reverso.

## Decisión — 1 minuto
La base de datos arbitra la idempotencia. El insert corre en su propia transacción; ante una carrera se consulta al ganador después del rollback. El reverso bloquea la fila, permite APPROVED → REVERSED y conserva el resultado al repetirse.

## Discusión
- ¿Qué operación es atómica?
- ¿Qué ocurre entre confirmar una escritura y enviar una respuesta?
- ¿Qué impide agotar recursos?
- ¿Qué cambia al ejecutar dos réplicas?
- ¿Qué mide el dashboard y qué no permite concluir?

## Límites que conviene explicar
No mueve dinero ni implementa contabilidad. LOST_RESPONSE devuelve un 504 después del commit: modela incertidumbre, no un timeout TCP. La idempotencia dura mientras exista el registro. Las pruebas locales H2 no garantizan por sí solas el comportamiento en PostgreSQL.
