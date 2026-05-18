# Documento Ejecutivo de Diseño
## Integración de API de Tasas de Cambio — Exchange Rates

**Proyecto:** Demo Gestión de Productos  
**Fecha:** 2026-05-17  
**Versión:** 1.0  
**Estado:** Aprobado para implementación

---

## 1. Resumen Ejecutivo

Se integrará la API pública **open.er-api.com** (gratuita, sin API key) para agregar capacidad de **conversión de precios en tiempo real** a la plataforma de gestión de productos. La integración demostrará el uso completo del stack técnico existente: `WebClient` reactivo, caché Redis, mensajería RabbitMQ, y un nuevo endpoint documentado en Swagger.

---

## 2. Objetivo

Demostrar de forma técnicamente completa cómo un backend Spring Boot consume, procesa y reacciona ante datos de una API REST externa, activando lógica de negocio a través de mensajería asíncrona.

---

## 3. Stack Tecnológico Involucrado

| Componente | Rol en la integración |
|---|---|
| `WebClient` (WebFlux) | Consumo reactivo de `open.er-api.com` |
| **Redis** | Caché de tasas de cambio (TTL: 1 hora) |
| **RabbitMQ** | Publicación de eventos de variación de tasa |
| `@Scheduled` | Job periódico de consulta a la API externa |
| **MySQL/JPA** | Registro histórico de variaciones significativas |
| **Swagger/OpenAPI** | Documentación del nuevo endpoint público |

---

## 4. Arquitectura de la Integración

```
                     ┌─────────────────────────────────────────────────────┐
                     │              BACKEND Spring Boot                     │
                     │                                                       │
  open.er-api.com ──►│  ExchangeRateClient (WebClient)                      │
  (API Gratuita)     │         │                                             │
                     │         ▼                                             │
                     │  ExchangeRateService                                  │
                     │    ├── Guarda en Redis (TTL 1h)                       │
                     │    ├── Compara con tasa anterior                      │
                     │    └── Si variación > 2% ──► RabbitMQ Publisher      │
                     │                                       │               │
                     │  ExchangeRateConsumer ◄───────────────┘               │
                     │    └── Registra evento en BD (tabla rate_change_log)  │
                     │                                                       │
                     │  ExchangeRateController (nuevo endpoint)              │
                     │    GET /api/v1/exchange-rates/latest                  │
                     │    GET /api/v1/productos/{id}/precio-convertido       │
                     └─────────────────────────────────────────────────────┘
                                      │
                                      ▼
                             Angular Frontend
                          (muestra precio en moneda
                           seleccionada por el usuario)
```

---

## 5. Flujo de Datos Detallado

### 5.1 Job Programado (cada 30 minutos)

```
1. @Scheduled llama a ExchangeRateService.actualizarTasas()
2. ExchangeRateClient hace GET https://open.er-api.com/v6/latest/USD
3. Respuesta JSON: { "rates": { "EUR": 0.92, "MXN": 17.10, ... } }
4. Se compara con la tasa anterior guardada en Redis
5a. Si variación ≤ 2%  → Solo actualiza Redis (sin eventos)
5b. Si variación > 2%  → Actualiza Redis + publica evento en RabbitMQ
6. Consumer RabbitMQ recibe el evento y persiste en rate_change_log
```

### 5.2 Endpoint de Precio Convertido

```
GET /api/v1/productos/{id}/precio-convertido?moneda=EUR

1. Obtiene Producto por id (con caché Redis existente)
2. Lee tasa de Redis (sin llamar a la API externa)
3. Calcula: precioConvertido = producto.precio * tasa
4. Devuelve ProductoPrecioConvertidoResponse
```

---

## 6. Componentes a Crear

### 6.1 Capa de Infraestructura (nueva)

| Archivo | Descripción |
|---|---|
| `infrastructure/exchange/ExchangeRateClient.java` | `WebClient` que consume `open.er-api.com` |
| `infrastructure/exchange/dto/ExchangeRateApiResponse.java` | DTO de la respuesta de la API |

### 6.2 Dominio (nuevo módulo)

| Archivo | Descripción |
|---|---|
| `exchange/domain/model/RateChangeLog.java` | Entidad JPA para historial de variaciones |
| `exchange/domain/repository/RateChangeLogRepository.java` | Repositorio JPA |
| `exchange/domain/service/ExchangeRateService.java` | Lógica de negocio principal |

### 6.3 Mensajería (nuevo)

| Archivo | Descripción |
|---|---|
| `common/config/RabbitMQConfig.java` | Queue, Exchange y Binding |
| `exchange/messaging/ExchangeRateEventPublisher.java` | Publica evento de variación |
| `exchange/messaging/ExchangeRateEventConsumer.java` | Consume y persiste el evento |
| `exchange/messaging/dto/RateChangeEvent.java` | DTO del mensaje RabbitMQ |

### 6.4 API (nuevo)

| Archivo | Descripción |
|---|---|
| `exchange/api/ExchangeRateController.java` | Controlador con 2 endpoints |
| `exchange/api/dto/ExchangeRateResponse.java` | DTO de respuesta de tasas |
| `exchange/api/dto/ProductoPrecioConvertidoResponse.java` | DTO de precio convertido |

### 6.5 Configuración

| Archivo | Descripción |
|---|---|
| `common/config/WebClientConfig.java` | Bean `WebClient` para la API externa |
| `common/config/SchedulingConfig.java` | `@EnableScheduling` |

---

## 7. Nuevos Endpoints (Swagger)

```
GET  /api/v1/exchange-rates/latest
     → Devuelve las tasas actuales cacheadas en Redis
     → Response: { "base": "USD", "rates": { "EUR": 0.92, "MXN": 17.10 }, "lastUpdated": "..." }

GET  /api/v1/productos/{id}/precio-convertido?moneda=EUR
     → Devuelve el precio del producto convertido a la moneda solicitada
     → Response: { "productoId": 1, "nombre": "...", "precioOriginal": 100.00,
                   "precioConvertido": 92.00, "moneda": "EUR", "tasa": 0.92 }
```

---

## 8. Modelo de Datos Nuevo

```sql
CREATE TABLE rate_change_log (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    moneda_base   VARCHAR(3)     NOT NULL,   -- e.g. "USD"
    moneda_destino VARCHAR(3)    NOT NULL,   -- e.g. "EUR"
    tasa_anterior DECIMAL(10,6)  NOT NULL,
    tasa_nueva    DECIMAL(10,6)  NOT NULL,
    variacion_pct DECIMAL(5,2)   NOT NULL,
    registered_at DATETIME       NOT NULL
);
```

---

## 9. Configuración de RabbitMQ

```yaml
# application-local.yml (a agregar)
rabbitmq:
  exchange-rates:
    queue: exchange-rate-alerts
    exchange: exchange-rate-events
    routing-key: rate.change.alert
```

---

## 10. Cambios en application-local.yml

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

exchange-rate:
  api:
    base-url: https://open.er-api.com/v6/latest
    base-currency: USD
  schedule:
    cron: "0 */30 * * * *"   # cada 30 minutos
  alert:
    threshold-pct: 2.0        # umbral de variación para publicar evento
  cache:
    ttl-minutes: 60
  supported-currencies:
    - EUR
    - MXN
    - GBP
    - COP
```

---

## 11. Cambios en docker-compose.yml

Se añadirá el servicio `rabbitmq` con management UI:

```yaml
rabbitmq:
  image: rabbitmq:3-management-alpine
  container_name: demo-rabbitmq
  ports:
    - "5672:5672"
    - "15672:15672"    # RabbitMQ Management UI
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
```

---

## 12. Frontend Angular — Cambios

Se agregará un **selector de moneda** en la lista de productos que llama al nuevo endpoint y muestra los precios convertidos.

| Componente | Cambio |
|---|---|
| `productos-list` | Agregar dropdown de moneda (USD/EUR/MXN/GBP) |
| `exchange-rate.service.ts` | Nuevo service que consume el endpoint |
| `productos.service.ts` | Nuevo método `getPrecioConvertido(id, moneda)` |

---

## 13. Criterios de Éxito

- [ ] `WebClient` consume `open.er-api.com` sin errores (con manejo de timeout y fallback)
- [ ] Las tasas se almacenan en Redis y se recuperan sin llamar a la API externa en cada request
- [ ] Al superar el umbral de variación, se publica un mensaje en RabbitMQ visible en el Management UI
- [ ] El evento es consumido y persiste en la tabla `rate_change_log`
- [ ] Los 2 nuevos endpoints aparecen documentados en Swagger
- [ ] El frontend muestra precios convertidos según la moneda seleccionada
- [ ] Los tests unitarios cubren `ExchangeRateService` y el consumer

---

## 14. Riesgos y Mitigaciones

| Riesgo | Mitigación |
|---|---|
| API externa no disponible | Implementar `fallback` en `WebClient` retornando tasas desde Redis (si existen) |
| RabbitMQ no levantado en local | El publisher usará `try-catch` y logeará el error sin bloquear el flujo principal |
| Tasas desactualizadas | El TTL de Redis de 1h garantiza que nunca se sirvan tasas de más de 1 hora |

---

## 15. Orden de Implementación

```
Fase 1 — Infraestructura base
  1. Agregar RabbitMQ a docker-compose.yml
  2. Crear WebClientConfig.java
  3. Crear RabbitMQConfig.java
  4. Actualizar application-local.yml

Fase 2 — Consumo de API y caché
  5. ExchangeRateClient.java (WebClient)
  6. ExchangeRateService.java (lógica + Redis)
  7. SchedulingConfig + @Scheduled en el service

Fase 3 — Mensajería
  8. RateChangeEvent.java (DTO)
  9. ExchangeRateEventPublisher.java
  10. ExchangeRateEventConsumer.java + RateChangeLog entity

Fase 4 — API REST
  11. ExchangeRateController.java + DTOs
  12. Documentación Swagger

Fase 5 — Frontend
  13. exchange-rate.service.ts
  14. Selector de moneda en productos-list

Fase 6 — Tests
  15. ExchangeRateServiceTest.java
  16. ExchangeRateEventConsumerTest.java
```

---

*Documento generado el 2026-05-17 | Proyecto: Demo Gestión de Productos*
