# AstroBookings - Arquitectura

## Introducción

AstroBookings es una aplicación de reservas de viajes espaciales implementada con una **arquitectura hexagonal simplificada** (puertos y adaptadores). En esta versión no existe capa de aplicación separada: los servicios de dominio implementan directamente los puertos de entrada (use cases). Las dependencias concretas se resuelven mediante factories en infraestructura y se inyectan desde una clase `Config` central.

El proyecto sigue una evolución arquitectónica a través de ramas:
- **0-legacy**: Código monolítico inicial
- **1-responsibility**: Separación por responsabilidades
- **2-dependencies**: Inversión de dependencias
- **3-ports-adapters**: Puertos y adaptadores básicos
- **4-application** (actual): Puertos de entrada/salida + factories de configuración

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

| Method | Path                    | Description                                |
| ------ | ----------------------- | ------------------------------------------ |
| GET    | `/rockets`              | List all rockets                           |
| POST   | `/rockets`              | Create rocket                              |
| GET    | `/flights`              | List available flights (filter by status)  |
| POST   | `/flights`              | Create flight                              |
| GET    | `/bookings`             | List bookings (filter by flight/passenger) |
| POST   | `/bookings`             | Create booking (processes payment)         |
| POST   | `/admin/cancel-flights` | Trigger cancellation check                 |

### Respuestas
- JSON format
- HTTP status codes:
  -  (200 (Consulta Ok), 201 (Creación), 400 (Entrada incorrecta), 402 (Pago Requerido),  404 (No encontrado), 500 (Error interno))

### Pruebas E2E

- Usar RestClient para verificar endpoints
- Escenarios definidos en [carpeta e2e](./e2e/)
- Alternativamente probar usando curl o Postman

## Estructura de Carpetas

```
├── src/main/java/com/astrobookings/
│   ├── AstroBookingsApp.java       # Entry point: crea servidor HTTP y registra handlers
│   ├── Config.java                 # Composition root: instancia adaptadores y servicios
│   ├── domain/                     # Núcleo de negocio + puertos
│   │   ├── BookingsService.java    # Implementa BookingsUseCases
│   │   ├── CancellationService.java# Implementa CancellationUseCases
│   │   ├── FlightsService.java     # Implementa FlightsUseCases
│   │   ├── RocketsService.java     # Implementa RocketsUseCases
│   │   ├── models/                 # Entidades, comandos, errores
│   │   │   ├── Booking.java
│   │   │   ├── Flight.java
│   │   │   ├── Rocket.java
│   │   │   ├── FlightStatus.java
│   │   │   ├── Create*Command.java # Comandos de creación
│   │   │   ├── BusinessException.java
│   │   │   └── BusinessErrorCode.java
│   │   └── ports/
│   │       ├── input/              # Puertos de entrada (use cases)
│   │       │   ├── BookingsUseCases.java
│   │       │   ├── CancellationUseCases.java
│   │       │   ├── FlightsUseCases.java
│   │       │   └── RocketsUseCases.java
│   │       └── output/             # Puertos de salida (repositorios, gateways)
│   │           ├── BookingRepository.java
│   │           ├── FlightRepository.java
│   │           ├── RocketRepository.java
│   │           ├── PaymentGateway.java
│   │           └── NotificationService.java
│   └── infrastructure/
│       ├── persistence/            # Adaptadores de salida
│       │   ├── PersistenceAdapterFactory.java  # Factory de adaptadores de persistencia
│       │   ├── BookingInMemoryRepository.java
│       │   ├── FlightInMemoryRepository.java
│       │   ├── RocketInMemoryRepository.java
│       │   ├── PaymentConsoleGateway.java
│       │   └── NotificationConsoleService.java
│       └── presentation/           # Adaptadores de entrada (HTTP handlers)
│           ├── UseCasesAdapterFactory.java  # Factory de servicios de dominio
│           ├── AdminHandler.java
│           ├── BookingsHandler.java
│           ├── FlightsHandler.java
│           ├── RocketsHandler.java
│           ├── BaseHandler.java    # Clase base con utilidades HTTP/JSON
│           └── models/
│               ├── ErrorResponse.java
│               └── ErrorResponseMapper.java
├── pom.xml
├── README.md
└── ARCHITECTURE.md
```

### Capas y Componentes:

A continuación se describe cómo encaja cada capa dentro del enfoque ports & adapters:

- **Config (composition root)**: Clase central que ensambla la aplicación. Obtiene los adaptadores de salida desde `PersistenceAdapterFactory` y crea los servicios de dominio mediante `UseCasesAdapterFactory`, inyectando las dependencias. Los handlers reciben los use cases ya configurados.

- **infrastructure/presentation (adaptadores de entrada)**: HTTP handlers basados en `com.sun.net.httpserver`. Cada handler recibe un puerto de entrada (use case) por constructor y transforma JSON ↔ DTOs. `UseCasesAdapterFactory` crea los servicios de dominio que implementan los use cases.

- **domain (núcleo + puertos)**:
  - **Servicios**: `RocketsService`, `FlightsService`, `BookingsService`, `CancellationService`. Implementan los puertos de entrada (use cases) y contienen toda la lógica de negocio.
  - **Modelos**: Entidades (`Rocket`, `Flight`, `Booking`), estados (`FlightStatus`), comandos (`CreateRocketCommand`, etc.), excepciones (`BusinessException`, `BusinessErrorCode`).
  - **Puertos de entrada** (`ports/input`): Interfaces `RocketsUseCases`, `FlightsUseCases`, `BookingsUseCases`, `CancellationUseCases`. Definen las operaciones que el dominio expone.
  - **Puertos de salida** (`ports/output`): Interfaces `RocketRepository`, `FlightRepository`, `BookingRepository`, `PaymentGateway`, `NotificationService`. Los servicios solo conocen estos puertos.

- **infrastructure/persistence (adaptadores de salida)**:
  - **Repositorios en memoria**: `RocketInMemoryRepository`, `FlightInMemoryRepository`, `BookingInMemoryRepository` implementan los puertos de persistencia.
  - **Gateways simulados**: `PaymentConsoleGateway` y `NotificationConsoleService` cumplen los puertos externos.
  - **Factory de persistencia**: `PersistenceAdapterFactory` expone instancias singleton de todos los adaptadores de salida.

## Flujo de Datos y Dependencias

El flujo de control va de los adaptadores de entrada hacia el dominio, y desde el dominio hacia los puertos de salida. Las dependencias se invierten: los servicios de dominio solo conocen interfaces (puertos), y `Config` + factories proveen los adaptadores concretos.

### Diagrama de Configuración e Inyección
```
AstroBookingsApp.main()
  ↓
Config (composition root)
  ├─ PersistenceAdapterFactory → crea adaptadores de salida (singletons)
  │   ├─ RocketInMemoryRepository
  │   ├─ FlightInMemoryRepository
  │   ├─ BookingInMemoryRepository
  │   ├─ PaymentConsoleGateway
  │   └─ NotificationConsoleService
  └─ UseCasesAdapterFactory → crea servicios de dominio (inyecta puertos)
      ├─ RocketsService(rocketRepository)
      ├─ FlightsService(flightRepository, rocketRepository)
      ├─ BookingsService(bookingRepo, flightRepo, rocketRepo, paymentGw, notificationSvc)
      └─ CancellationService(flightRepo, bookingRepo, paymentGw, notificationSvc)
  ↓
HttpServer registra handlers con use cases inyectados
  ├─ RocketsHandler(RocketsUseCases)
  ├─ FlightsHandler(FlightsUseCases)
  ├─ BookingsHandler(BookingsUseCases)
  └─ AdminHandler(CancellationUseCases)
```

### Crear Reserva (POST /bookings)
```
HTTP Request → BookingsHandler
  ↓ (parseJSON → CreateBookingCommand)
BookingsHandler.handlePost()
  ↓ (llama al puerto de entrada)
BookingsUseCases.createBooking(command)
  ↓ (implementado por)
BookingsService
  ├─ flightRepository.findById() → FlightInMemoryRepository
  ├─ rocketRepository.findById() → RocketInMemoryRepository
  ├─ calculateDiscount() (lógica de negocio)
  ├─ paymentGateway.processPayment() → PaymentConsoleGateway
  ├─ bookingRepository.save() → BookingInMemoryRepository
  ├─ flightRepository.save() (actualiza estado CONFIRMED/SOLD_OUT)
  └─ notificationService.notify*() → NotificationConsoleService
  ↓
HTTP Response ← BookingsHandler.sendJsonResponse()
```

### Cancelar Vuelos (POST /admin/cancel-flights)
```
HTTP Request → AdminHandler
  ↓
AdminHandler.handlePost()
  ↓ (llama al puerto de entrada)
CancellationUseCases.cancelFlights()
  ↓ (implementado por)
CancellationService
  ├─ flightRepository.findScheduledBefore() → FlightInMemoryRepository
  │   (busca vuelos SCHEDULED a menos de 1 semana sin mínimo de pasajeros)
  ├─ Para cada vuelo a cancelar:
  │   ├─ bookingRepository.findByFlightId() → BookingInMemoryRepository
  │   ├─ paymentGateway.refund() → PaymentConsoleGateway
  │   ├─ flightRepository.save() (estado → CANCELLED)
  │   └─ notificationService.notifyCancellation() → NotificationConsoleService
  ↓
HTTP Response ← AdminHandler.sendJsonResponse({cancelledFlights: n})
```

### Crear Cohete (POST /rockets)
```
HTTP Request → RocketsHandler
  ↓ (parseJSON → CreateRocketCommand)
RocketsHandler.handlePost()
  ↓
RocketsUseCases.saveRocket(command)
  ↓ (implementado por)
RocketsService
  ├─ validate(command) (nombre obligatorio, capacidad ≤ 10)
  └─ rocketRepository.save() → RocketInMemoryRepository
  ↓
HTTP Response ← RocketsHandler.sendJsonResponse()
```

### Crear Vuelo (POST /flights)
```
HTTP Request → FlightsHandler
  ↓ (parseJSON → CreateFlightCommand)
FlightsHandler.handlePost()
  ↓
FlightsUseCases.createFlight(command)
  ↓ (implementado por)
FlightsService
  ├─ validate() (fecha futura, precio > 0)
  ├─ rocketRepository.findById() → RocketInMemoryRepository
  └─ flightRepository.save() → FlightInMemoryRepository (estado SCHEDULED)
  ↓
HTTP Response ← FlightsHandler.sendJsonResponse()
```


## Workflow de desarrollo y ejecución

```bash
# Compilar
mvn clean compile

# Empaquetar
mvn clean package

# Ejecutar
java -jar target/astrobookings-1.0-SNAPSHOT.jar

# Server: http://localhost:8080
```
