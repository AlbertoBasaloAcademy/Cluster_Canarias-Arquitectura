# AstroBookings - Arquitectura DDD Modular

## Introducción

AstroBookings es una aplicación de reservas de viajes espaciales implementada con **Strategic Domain-Driven Design (DDD)** y **arquitectura hexagonal modular**. La aplicación está organizada en dos subdominios (bounded contexts) claramente diferenciados:

- **Fleet** (Supporting Subdomain): Gestión de cohetes y vuelos
- **Sales** (Core Domain): Gestión de reservas, descuentos y pagos

Cada módulo mantiene su propia arquitectura hexagonal interna, con puertos y adaptadores bien definidos. La comunicación entre módulos se realiza a través de puertos explícitos que actúan como Anti-Corruption Layers.

## Evolución Arquitectónica

El proyecto sigue una evolución a través de ramas:
- **0-legacy**: Código monolítico inicial
- **1-responsibility**: Separación por responsabilidades
- **2-dependencies**: Inversión de dependencias
- **3-ports-adapters**: Puertos y adaptadores básicos
- **4-application**: Puertos de entrada/salida + factories de configuración
- **5-ddd-modules** (actual): Subdominios estratégicos con bounded contexts

**Características principales**:
- Gestión de cohetes, vuelos y reservas
- Control de capacidad y estados de vuelos
- Cancelación automática de vuelos con devoluciones
- Procesamiento de pagos simulado (gateway externo)
- Notificaciones por email simuladas

## Tecnologías

- **Java**: 21
- **Build**: Maven 3.x
- **HTTP Server**: JDK HTTP Server (com.sun.net.httpserver)
- **JSON**: Jackson 2.15.2
- **Database**: In-memory (HashMap)
- **External Services**: Simulated (console logs)

## Endpoints

| Method | Path                    | Description                                | Módulo |
| ------ | ----------------------- | ------------------------------------------ | ------ |
| GET    | `/rockets`              | List all rockets                           | Fleet  |
| POST   | `/rockets`              | Create rocket                              | Fleet  |
| GET    | `/flights`              | List available flights (filter by status)  | Fleet  |
| POST   | `/flights`              | Create flight                              | Fleet  |
| GET    | `/bookings`             | List bookings (filter by flight/passenger) | Sales  |
| POST   | `/bookings`             | Create booking (processes payment)         | Sales  |
| POST   | `/admin/cancel-flights` | Trigger cancellation check                 | Sales  |

### Respuestas
- JSON format
- HTTP status codes: 200 (OK), 201 (Created), 400 (Bad Request), 402 (Payment Required), 404 (Not Found), 500 (Internal Error)

### Pruebas E2E

- Usar RestClient para verificar endpoints
- Escenarios definidos en [carpeta e2e](./e2e/)
- Alternativamente probar usando curl o Postman

## Estructura de Módulos

```
src/main/java/com/astrobookings/
├── AstroBookingsApp.java           # Entry point: crea servidor HTTP
├── Config.java                     # Composition root: ensambla módulos
│
├── shared/                         # Kernel compartido
│   ├── domain/
│   │   ├── BusinessException.java
│   │   └── BusinessErrorCode.java
│   └── infrastructure/             # Infraestructura compartida
│       └── presentation/
│           ├── BaseHandler.java
│           └── models/
│               ├── ErrorResponse.java
│               └── ErrorResponseMapper.java
│
├── fleet/                          # Módulo Fleet (Supporting Subdomain)
│   ├── domain/
│   │   ├── models/                 # Entidades y Value Objects
│   │   │   ├── Rocket.java
│   │   │   ├── Flight.java
│   │   │   ├── FlightStatus.java
│   │   │   ├── CreateRocketCommand.java
│   │   │   └── CreateFlightCommand.java
│   │   ├── ports/
│   │   │   ├── input/              # Puertos de entrada (use cases)
│   │   │   │   ├── RocketsUseCases.java
│   │   │   │   └── FlightsUseCases.java
│   │   │   └── output/             # Puertos de salida (repositorios)
│   │   │       ├── RocketRepository.java
│   │   │       └── FlightRepository.java
│   │   ├── RocketsService.java     # Domain service
│   │   └── FlightsService.java     # Domain service
│   └── infrastructure/
│       ├── persistence/            # Adaptadores de salida
│       │   ├── RocketInMemoryRepository.java
│       │   └── FlightInMemoryRepository.java
│       ├── presentation/           # Adaptadores de entrada (HTTP handlers)
│       │   ├── RocketsHandler.java
│       │   └── FlightsHandler.java
│       └── FleetFactory.java       # Factory del módulo
│
└── sales/                          # Módulo Sales (Core Domain)
    ├── domain/
    │   ├── models/
    │   │   ├── Booking.java
    │   │   └── CreateBookingCommand.java
    │   ├── ports/
    │   │   ├── input/
    │   │   │   ├── BookingsUseCases.java
    │   │   │   └── CancellationUseCases.java
    │   │   │   └── output/
    │   │   │       ├── BookingRepository.java
    │   │   │       ├── PaymentGateway.java
    │   │   │       ├── NotificationService.java
    │   │   │       └── FlightInfoProvider.java    # Puerto hacia Fleet
    │   │   ├── BookingsService.java
    │   │   └── CancellationService.java
    └── infrastructure/
        ├── persistence/
        │   ├── BookingInMemoryRepository.java
        │   ├── PaymentConsoleGateway.java
        │   └── NotificationConsoleService.java
        ├── presentation/
        │   ├── BookingsHandler.java
        │   └── AdminHandler.java
        ├── adapters/
        │   └── FleetAdapter.java              # Anti-Corruption Layer
        └── SalesFactory.java
```

## Arquitectura de Módulos

### Fleet Module (Supporting Subdomain)

**Responsabilidades:**
- Gestión del inventario de cohetes (capacidad, velocidad)
- Programación de vuelos (fechas, precios base)
- Control de estados operativos (SCHEDULED, CONFIRMED, SOLD_OUT, CANCELLED)
- Verificación de disponibilidad y capacidad

**Arquitectura Hexagonal Interna:**
```
┌────────────────────────────────────────┐
│         Fleet Module                   │
│                                        │
│  ┌──────────────────────────────────┐ │
│  │   Presentation (Input Adapters)  │ │
│  │   - RocketsHandler               │ │
│  │   - FlightsHandler               │ │
│  └─────────────┬────────────────────┘ │
│                │                       │
│  ┌─────────────▼────────────────────┐ │
│  │      Domain (Ports + Services)   │ │
│  │   Input Ports:                   │ │
│  │   - RocketsUseCases              │ │
│  │   - FlightsUseCases              │ │
│  │                                  │ │
│  │   Services:                      │ │
│  │   - RocketsService               │ │
│  │   - FlightsService               │ │
│  │                                  │ │
│  │   Output Ports:                  │ │
│  │   - RocketRepository             │ │
│  │   - FlightRepository             │ │
│  └──────────────┬───────────────────┘ │
│                 │                      │
│  ┌──────────────▼──────────────────┐  │
│  │ Persistence (Output Adapters)   │  │
│  │ - RocketInMemoryRepository      │  │
│  │ - FlightInMemoryRepository      │  │
│  └─────────────────────────────────┘  │
└────────────────────────────────────────┘
```

### Sales Module (Core Domain)

**Responsabilidades:**
- Creación de reservas (validación, cálculo de precio final)
- Políticas de descuentos dinámicos (precedencia, antelación, ocupación)
- Procesamiento de pagos
- Notificaciones de confirmación
- Cancelaciones comerciales (por falta de aforo mínimo)
- Devoluciones de pagos

**Arquitectura Hexagonal Interna:**
```
┌────────────────────────────────────────────┐
│         Sales Module                       │
│                                            │
│  ┌──────────────────────────────────────┐ │
│  │   Presentation (Input Adapters)      │ │
│  │   - BookingsHandler                  │ │
│  │   - AdminHandler                     │ │
│  └─────────────┬────────────────────────┘ │
│                │                           │
│  ┌─────────────▼──────────────────────┐   │
│  │      Domain (Ports + Services)     │   │
│  │   Input Ports:                     │   │
│  │   - BookingsUseCases               │   │
│  │   - CancellationUseCases           │   │
│  │                                    │   │
│  │   Services:                        │   │
│  │   - BookingsService                │   │
│  │   - CancellationService            │   │
│  │                                    │   │
│  │   Output Ports:                    │   │
│  │   - BookingRepository              │   │
│  │   - PaymentGateway                 │   │
│  │   - NotificationService            │   │
│  │   - FlightInfoProvider ─────────┐  │   │
│  └────────────┬────────────────────┼──┘   │
│               │                    │       │
│  ┌────────────▼──────────────┐    │       │
│  │ Persistence Adapters       │    │       │
│  │ - BookingInMemoryRepo      │    │       │
│  │ - PaymentConsoleGateway    │    │       │
│  │ - NotificationConsoleSvc   │    │       │
│  └────────────────────────────┘    │       │
│               ┌────────────────────┘       │
│  ┌────────────▼──────────────┐            │
│  │ Fleet Adapter (ACL)        │            │
│  │ Implementa:                │            │
│  │ FlightInfoProvider         │            │
│  └────────────┬───────────────┘            │
└───────────────┼────────────────────────────┘
                │
                │ USA
                ▼
        ┌───────────────────┐
        │   Fleet Module    │
        │  (Repositories)   │
        └───────────────────┘
```

## Comunicación entre Módulos

### Context Mapping

La relación entre módulos sigue el patrón **Customer-Supplier**:
- **Sales** (Customer) necesita información de **Fleet** (Supplier)
- La comunicación se realiza a través de una abstracción: `FlightInfoProvider`
- **FleetAdapter** actúa como **Anti-Corruption Layer**

```
┌─────────────────────────────────────────┐
│            Fleet Module                 │
│         (Supplier/Upstream)             │
│                                         │
│  Expone:                                │
│  - FlightRepository                     │
│  - RocketRepository                     │
└──────────────┬──────────────────────────┘
               │
               │ Acceso directo desde FleetAdapter
               ▼
┌──────────────────────────────────────────┐
│        FleetAdapter (ACL)                │
│    (en Sales Infrastructure)             │
│                                          │
│  Traduce:                                │
│  Flight → FlightInfo (DTO)               │
│  Implementa: FlightInfoProvider          │
└──────────────┬───────────────────────────┘
               │
               │ Implementa puerto
               ▼
┌──────────────────────────────────────────┐
│         FlightInfoProvider               │
│       (Puerto en Sales Domain)           │
│                                          │
│  Operaciones:                            │
│  - getFlightById(id)                     │
│  - getRocketCapacity(flightId)           │
│  - canAcceptPassengers(id)               │
│  - confirmFlightIfMinReached(id, count)  │
│  - markFlightSoldOut(id)                 │
│  - cancelFlightIfLowDemand(...)          │
│  - getFlightsForCancellation(...)        │
└──────────────┬───────────────────────────┘
               │
               │ Usado por
               ▼
┌──────────────────────────────────────────┐
│           Sales Module                   │
│        (Customer/Downstream)             │
│                                          │
│  Servicios:                              │
│  - BookingsService                       │
│  - CancellationService                   │
└──────────────────────────────────────────┘
```

### FlightInfoProvider (Anti-Corruption Layer)

Este puerto define el contrato en términos del dominio Sales:

```java
public interface FlightInfoProvider {
  FlightInfo getFlightById(String flightId);
  Capacity getRocketCapacityForFlight(String flightId);
  boolean canAcceptPassengers(String flightId);
  boolean confirmFlightIfMinReached(String flightId, int passengerCount);
  void markFlightSoldOut(String flightId);
  boolean cancelFlightIfLowDemand(String flightId, int currentPassengers, LocalDateTime cutoffDate);
  List<FlightInfo> getFlightsForCancellation(LocalDateTime cutoffDate, int minPassengers);
  
  record FlightInfo(
      String id,
      String rocketId,
      LocalDateTime departureDate,
      double basePrice,
      String status,
      int minPassengers,
      Capacity capacity
  ) {}
}
```

**Beneficios:**
- Sales no conoce las entidades internas de Fleet (`Flight`, `Rocket`)
- Sales trabaja con DTOs (`FlightInfo`) en su propio lenguaje
- Cambios en Fleet no afectan a Sales (siempre que se mantenga el contrato)

## Flujo de Datos y Dependencias

### Inicialización (Composition Root)

```java
// Config.java
public class Config {
  // 1. Fleet se crea primero (sin dependencias externas)
  static final FleetFactory fleetFactory = new FleetFactory();
  
  // 2. Sales se crea después, inyectando Fleet
  static final SalesFactory salesFactory = new SalesFactory(fleetFactory);
}
```

### Diagrama de Configuración

```
AstroBookingsApp.main()
  │
  ├─► Config
  │     │
  │     ├─► FleetFactory
  │     │     ├─ RocketInMemoryRepository
  │     │     ├─ FlightInMemoryRepository
  │     │     ├─ RocketsService(rocketRepo)
  │     │     ├─ FlightsService(flightRepo, rocketRepo)
  │     │     ├─ RocketsHandler(rocketsUseCases)
  │     │     └─ FlightsHandler(flightsUseCases)
  │     │
  │     └─► SalesFactory(fleetFactory)
  │           ├─ BookingInMemoryRepository
  │           ├─ PaymentConsoleGateway
  │           ├─ NotificationConsoleService
  │           ├─ FleetAdapter(flightRepo, bookingRepo)  ← Conecta con Fleet
  │           ├─ BookingsService(bookingRepo, flightInfoProvider, payment, notification)
  │           ├─ CancellationService(flightInfoProvider, bookingRepo, payment, notification)
  │           ├─ BookingsHandler(bookingsUseCases)
  │           └─ AdminHandler(cancellationUseCases)
  │
  └─► HttpServer
        ├── /rockets     → fleetFactory.getRocketsHandler()
        ├── /flights     → fleetFactory.getFlightsHandler()
        ├── /bookings    → salesFactory.getBookingsHandler()
        └── /admin/...   → salesFactory.getAdminHandler()
```

### Flujo: Crear Reserva (POST /bookings)

```
HTTP Request
  ↓
BookingsHandler (Sales)
  ↓ parseJSON → CreateBookingCommand
BookingsService.createBooking(command)
  ↓
  ├─► flightInfoProvider.getFlightById(flightId)
  │     ↓
  │   FleetAdapter
  │     └─► flightRepository.findById(id)    [Fleet] → Flight aggregate + Capacity snapshot
  │
  ├─► calculateDiscount(flightInfo, currentBookings)
  ├─► paymentGateway.processPayment(finalPrice)
  ├─► bookingRepository.save(booking)         [Sales]
  │
  ├─► flightInfoProvider.confirmFlightIfMinReached(id, passengerCount)
  │     ↓
  │   FleetAdapter → flight.confirmIfMinReached(...) + save
  └─► flightInfoProvider.markFlightSoldOut(id) (cuando aplica)
        ↓
      FleetAdapter → flight.markSoldOut() + save
  ↓
HTTP Response (201 Created)
```

### Flujo: Cancelar Vuelos (POST /admin/cancel-flights)

```
HTTP Request
  ↓
AdminHandler (Sales)
  ↓
CancellationService.cancelFlights()
  ↓
  ├─► flightInfoProvider.getFlightsForCancellation(cutoff, minPassengers)
  │     ↓
  │   FleetAdapter
  │     └─► flightRepository.findAll() + filtrado    [Fleet]
  │
  ├─► Para cada vuelo:
  │     ├─► bookingRepository.findByFlightId(id)     [Sales]
  │     ├─► flightInfoProvider.cancelFlightIfLowDemand(id, passengers, cutoff)
  │     ├─► paymentGateway.processRefund(txId)
  │     └─► notificationService.notifyCancellation(...)
  │
  └─► return count
  ↓
HTTP Response (200 OK)
```

## Reglas de Negocio

### Descuentos (Sales Domain)

Precedencia estricta (solo se aplica uno):
1. **Última plaza**: 0% descuento
2. **Una plaza para mínimo**: 30% descuento
3. **Más de 6 meses antelación**: 10% descuento
4. **Entre 1 semana y 1 mes**: 20% descuento
5. **Resto**: 0% descuento

### Estados de Vuelo (Fleet Domain)

- **SCHEDULED**: Creado, esperando reservas
- **CONFIRMED**: Alcanzó mínimo de pasajeros
- **SOLD_OUT**: Alcanzó capacidad máxima
- **CANCELLED**: Cancelado (comercial u operativo)

Transiciones manejadas automáticamente por `BookingsService` al crear reservas.

### Cancelación Comercial (Sales Domain)

Un vuelo se cancela automáticamente si:
- Estado: `SCHEDULED`
- Faltan 7 días o menos para la salida
- No alcanzó el mínimo de pasajeros (5 por defecto)

Acciones:
1. Cambiar estado a `CANCELLED` (via FlightInfoProvider)
2. Procesar devolución total a todos los pasajeros
3. Enviar notificación de cancelación

## Modelo Táctico Rich

- **Capacity (VO compartido)**: encapsula el rango permitido (1..10) y centraliza las validaciones de aforo. Lo usan `Rocket`, `Flight` y `BookingsService` para evitar números mágicos.
- **Flight como agregado**: se crea a través de `Flight.schedule(...)` y protege sus invariantes (`confirmIfMinReached`, `markSoldOut`, `cancelDueToLowDemand`, `canAcceptNewPassenger`). Ningún servicio cambia `FlightStatus` directamente.
- **Booking como agregado rico**: `Booking.create(...)` valida pasajero, precio y transacción antes de persistir, eliminando setters públicos.
- **Servicios de dominio delgados**: `BookingsService` y `CancellationService` solo orquestan pagos, repositorios y notificaciones. Toda transición de vuelo pasa por `FlightInfoProvider`, que delega en el agregado de Fleet.

## Workflow de Desarrollo y Ejecución

```bash
# Compilar
mvn clean compile

# Empaquetar
mvn clean package

# Ejecutar
java -jar target/astrobookings-1.0-SNAPSHOT.jar

# Server: http://localhost:8080
```

## Beneficios de esta Arquitectura

### Alta Cohesión
- Cada módulo agrupa responsabilidades relacionadas
- Cambios en descuentos solo afectan a `sales`
- Cambios en programación solo afectan a `fleet`

### Bajo Acoplamiento
- Módulos se comunican a través de puertos bien definidos
- `FlightInfoProvider` aísla Sales de cambios en Fleet
- Posibilidad deextraer módulos a servicios separados

### Alineación con el Negocio
- La estructura refleja el lenguaje ubiquo
- Subdominios claramente identificables
- Facilita comunicación con expertos de dominio

### Evolutibilidad
- Cada módulo puede evolucionar independientemente
- Nuevas features se ubican claramente en un módulo
- Preparado para Event-Driven Architecture

## Próximos Pasos

1. **Eventos de Dominio**: Comunicación asíncrona
   - `FlightConfirmed`
   - `FlightCancelled`
   - `BookingCreated`

2. **Módulos Maven Separados**: Forzar límites físicos

3. **Persistencia Real**: Bases de datos separadas por módulo

4. **Observabilidad**: Métricas y trazas por subdomain

5. **API Gateway**: Punto de entrada único para todos los módulos
