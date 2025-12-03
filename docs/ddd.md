# Plan: Identificación de Subdominios DDD para AstroBookings

El análisis del código revela una estructura modular con arquitectura hexagonal. Para el taller de DDD estratégico, propongo identificar **dos subdominios principales** que demuestren claramente los conceptos de bounded contexts, lenguaje ubicuo y separación de responsabilidades.

## Subdominios Propuestos

1. **📦 Sales (Ventas)** - Subdominio Core
   - Entidades: `Booking`, referencias a `Flight` y `Rocket`
   - Responsabilidades: reservas, cálculo de descuentos, control de capacidad, procesamiento de pagos
   - Servicios actuales involucrados: `BookingsService`

2. **✈️ Operations (Operaciones)** - Subdominio Core
   - Entidades: `Flight`, `Rocket`, `FlightStatus`
   - Responsabilidades: gestión de flota, programación de vuelos, ciclo de vida de vuelos, cancelaciones
   - Servicios actuales involucrados: `RocketsService`, `FlightsService`, `CancellationService`

## Estructura de Carpetas Propuesta

```
src/main/java/com/astrobookings/
├── shared/                          # Kernel compartido
│   ├── models/
│   │   ├── BusinessException.java
│   │   └── BusinessErrorCode.java
│   └── events/                      # Eventos de dominio (futura integración)
│       └── DomainEvent.java
│
├── sales/                           # Bounded Context: Ventas
│   ├── domain/
│   │   ├── models/
│   │   │   ├── Booking.java
│   │   │   └── CreateBookingCommand.java
│   │   ├── services/
│   │   │   └── BookingsService.java
│   │   └── ports/
│   │       ├── input/
│   │       │   └── BookingsUseCases.java
│   │       └── output/
│   │           ├── BookingRepository.java
│   │           └── PaymentGateway.java
│   └── infrastructure/
│       ├── persistence/
│       │   ├── BookingInMemoryRepository.java
│       │   └── PaymentConsoleGateway.java
│       └── presentation/
│           └── BookingsHandler.java
│
├── operations/                      # Bounded Context: Operaciones
│   ├── domain/
│   │   ├── models/
│   │   │   ├── Flight.java
│   │   │   ├── FlightStatus.java
│   │   │   ├── Rocket.java
│   │   │   ├── CreateFlightCommand.java
│   │   │   └── CreateRocketCommand.java
│   │   ├── services/
│   │   │   ├── FlightsService.java
│   │   │   ├── RocketsService.java
│   │   │   └── CancellationService.java
│   │   └── ports/
│   │       ├── input/
│   │       │   ├── FlightsUseCases.java
│   │       │   ├── RocketsUseCases.java
│   │       │   └── CancellationUseCases.java
│   │       └── output/
│   │           ├── FlightRepository.java
│   │           ├── RocketRepository.java
│   │           └── NotificationService.java
│   └── infrastructure/
│       ├── persistence/
│       │   ├── FlightInMemoryRepository.java
│       │   ├── RocketInMemoryRepository.java
│       │   └── NotificationConsoleService.java
│       └── presentation/
│           ├── FlightsHandler.java
│           ├── RocketsHandler.java
│           └── AdminHandler.java
│
├── Config.java                      # Composition root
└── AstroBookingsApp.java            # Entry point
```

## Lenguaje Ubicuo (Ubiquitous Language)

### 📦 Sales Context (Ventas)

| Inglés                  | Español             | Definición                                                 |
| ----------------------- | ------------------- | ---------------------------------------------------------- |
| **Booking**             | Reserva             | Compra confirmada de un asiento para un vuelo específico   |
| **Passenger**           | Pasajero            | Persona titular de una reserva                             |
| **Final Price**         | Precio Final        | Precio calculado tras aplicar descuentos                   |
| **Base Price**          | Precio Base         | Precio original del vuelo antes de descuentos              |
| **Discount**            | Descuento           | Reducción de precio basada en reglas de tiempo u ocupación |
| **Payment Transaction** | Transacción de Pago | Registro de un pago procesado                              |
| **Refund**              | Reembolso           | Devolución de dinero cuando se cancela una reserva         |
| **Available Seats**     | Plazas Disponibles  | Asientos libres calculados (capacidad - reservas)          |
| **Sold Out**            | Agotado             | Cuando todas las plazas están reservadas                   |

**Reglas de Negocio en Sales:**
1. **Early Bird**: >180 días antes → 10% descuento
2. **Last Month**: 7-30 días antes → 20% descuento
3. **Min Passengers Incentive**: Reserva que alcanza mínimo → 30% descuento
4. **Last Seat**: Última plaza disponible → Sin descuento
5. **Capacity Control**: No se puede reservar si `bookings >= capacity`
6. **Status Validation**: No se puede reservar en vuelos `CANCELLED` o `SOLD_OUT`

---

### ✈️ Operations Context (Operaciones)

| Inglés             | Español           | Definición                                                           |
| ------------------ | ----------------- | -------------------------------------------------------------------- |
| **Flight**         | Vuelo             | Viaje espacial programado en un cohete específico                    |
| **Rocket**         | Cohete            | Nave espacial utilizada para los vuelos                              |
| **Departure Date** | Fecha de Salida   | Momento del lanzamiento del vuelo                                    |
| **Flight Status**  | Estado del Vuelo  | Estado del ciclo de vida (SCHEDULED, CONFIRMED, SOLD_OUT, CANCELLED) |
| **Min Passengers** | Pasajeros Mínimos | Umbral necesario para confirmar un vuelo                             |
| **Capacity**       | Capacidad         | Máximo de pasajeros que puede transportar el cohete                  |
| **Speed**          | Velocidad         | Velocidad máxima del cohete                                          |
| **Confirmation**   | Confirmación      | Cambio de estado cuando se alcanza el mínimo de pasajeros            |
| **Cancellation**   | Cancelación       | Terminación de un vuelo por reservas insuficientes                   |

**Reglas de Negocio en Operations:**
1. **Rocket Capacity Limit**: 1-10 pasajeros por cohete
2. **Flight Scheduling**: Fecha de salida debe ser futura (máximo 1 año)
3. **Default Min Passengers**: Mínimo 5 pasajeros requeridos por defecto
4. **Auto-Cancellation**: Vuelos con `bookings < minPassengers` a 7 días se cancelan
5. **Status Transitions**: `SCHEDULED` → `CONFIRMED` (al alcanzar mínimo) → `SOLD_OUT` (al completar capacidad)

---

## Puntos de Integración entre Contextos

### Flight como Entidad Compartida

| Atributo        | Contexto Propietario | Contexto Consumidor | Uso                                                      |
| --------------- | -------------------- | ------------------- | -------------------------------------------------------- |
| `id`            | Operations           | Sales               | Clave de referencia para reservas                        |
| `rocketId`      | Operations           | Sales               | Para consultar capacidad                                 |
| `departureDate` | Operations           | Sales               | Para calcular descuentos                                 |
| `basePrice`     | Operations           | Sales               | Punto de partida para precio final                       |
| `status`        | Operations           | **Ambos**           | Sales lee para validar, Operations gestiona transiciones |
| `minPassengers` | Operations           | Sales               | Para regla de descuento y trigger de confirmación        |

### Flujo Actual (Acoplamiento)

```
┌─────────────────────────────────────────────────────────────────┐
│                     MONOLITO ACTUAL                             │
├─────────────────────────────────────────────────────────────────┤
│  BookingsService                                                │
│    ├── flightRepository.findById(flightId)  ← lee Flight        │
│    ├── rocketRepository.findById(rocketId)  ← lee Rocket        │
│    ├── Calcula descuento usando Flight.departureDate            │
│    ├── Calcula finalPrice desde Flight.basePrice                │
│    ├── Actualiza Flight.status (CONFIRMED, SOLD_OUT) ← ¡ESCRIBE!│
│    └── flightRepository.save(flight)                            │
├─────────────────────────────────────────────────────────────────┤
│  CancellationService                                            │
│    ├── flightRepository.findAll()           ← lee Flights       │
│    ├── bookingRepository.findByFlightId()   ← lee Bookings      │
│    ├── Actualiza Flight.status (CANCELLED)                      │
│    ├── paymentGateway.processRefund()       ← dispara reembolsos│
│    └── notificationService.notifyCancellation()                 │
└─────────────────────────────────────────────────────────────────┘
```

### Problema Identificado

**Sales escribe en la entidad Flight.** El `BookingsService` modifica `Flight.status` cuando:
- `bookings == minPassengers` → establece `CONFIRMED`
- `bookings == capacity` → establece `SOLD_OUT`

Esto crea **acoplamiento fuerte** entre contextos que debe resolverse.

---

## Context Map

```
┌───────────────────────────────────────────────────────────────────┐
│                        CONTEXT MAP                                │
├───────────────────────────────────────────────────────────────────┤
│                                                                   │
│   ┌─────────────────┐  Customer/Supplier  ┌─────────────────┐    │
│   │   OPERATIONS    │ ◄────────────────── │     SALES       │    │
│   │   (Upstream)    │                     │  (Downstream)   │    │
│   │                 │     Conformist      │                 │    │
│   │  • Flight       │ ──────────────────► │  • Booking      │    │
│   │  • Rocket       │                     │  • Discount     │    │
│   │  • FlightStatus │                     │  • Payment      │    │
│   └────────┬────────┘                     └────────┬────────┘    │
│            │                                       │              │
│            │         SHARED KERNEL                 │              │
│            │    ┌──────────────────┐               │              │
│            └────┤ BusinessException├───────────────┘              │
│                 │ BusinessErrorCode│                              │
│                 └──────────────────┘                              │
│                                                                   │
│   ┌─────────────────┐               ┌─────────────────┐          │
│   │    PAYMENTS     │               │  NOTIFICATIONS  │          │
│   │    (Generic)    │               │    (Generic)    │          │
│   │                 │               │                 │          │
│   │  • processPayment│              │  • notifyConfirm│          │
│   │  • refund       │               │  • notifyCancel │          │
│   └─────────────────┘               └─────────────────┘          │
│                                                                   │
└───────────────────────────────────────────────────────────────────┘
```

### Relaciones entre Contextos

| Relación              | Desde              | Hacia      | Patrón              | Justificación                                                      |
| --------------------- | ------------------ | ---------- | ------------------- | ------------------------------------------------------------------ |
| **Customer/Supplier** | Operations         | Sales      | Upstream/Downstream | Operations define el modelo Flight que Sales consume               |
| **Shared Kernel**     | Operations ↔ Sales | Shared     | Modelo compartido   | `BusinessException`, `BusinessErrorCode` usados por ambos          |
| **Conformist**        | Sales              | Operations | Adopta modelo       | Sales actualmente usa la entidad Flight de Operations directamente |
| **Open Host Service** | Operations         | Externos   | API publicada       | `FlightsHandler`, `RocketsHandler` exponen endpoints REST          |

---

## Propuestas de Refactorización

### 1. Anti-Corruption Layer (ACL) para Sales

Crear un Value Object `FlightReference` en el contexto Sales:

```java
// sales/domain/models/FlightReference.java
public record FlightReference(
    String flightId,
    LocalDateTime departureDate,
    double basePrice,
    int availableSeats,
    boolean isBookable
) {}
```

Esto traduce desde la entidad `Flight` de Operations, protegiendo a Sales de cambios internos.

### 2. Domain Events para Desacoplamiento

```
┌──────────────┐    BookingCreatedEvent    ┌──────────────┐
│    SALES     │ ─────────────────────────►│  OPERATIONS  │
│              │                           │              │
│              │◄───────────────────────── │              │
└──────────────┘   FlightConfirmedEvent    └──────────────┘
                   FlightCancelledEvent
```

- `BookingCreatedEvent` → Operations escucha para actualizar conteo de asientos
- `FlightConfirmedEvent` → Sales escucha para notificaciones
- `FlightCancelledEvent` → Sales dispara reembolsos

### 3. Separar Ownership de Status

- **Operations posee**: `SCHEDULED`, `CANCELLED`
- **Sales dispara transiciones** vía eventos, **Operations las aplica**

---

## Clasificación de Subdominios

| Subdominio        | Tipo    | Justificación                                           |
| ----------------- | ------- | ------------------------------------------------------- |
| **Operations**    | Core    | Diferenciador del negocio: gestión de vuelos espaciales |
| **Sales**         | Core    | Genera ingresos: reservas y pagos                       |
| **Payments**      | Generic | Podría ser servicio externo (Stripe, PayPal)            |
| **Notifications** | Generic | Podría ser servicio externo (SendGrid, SNS)             |

---

## Próximos Pasos

1. ✅ Documentar subdominios propuestos
2. ✅ Definir lenguaje ubicuo por contexto
3. ✅ Identificar puntos de integración
4. ✅ Crear Context Map
5. ⬜ Implementar estructura de carpetas propuesta
6. ⬜ Refactorizar código para separar contextos
7. ⬜ Introducir eventos de dominio para desacoplamiento
