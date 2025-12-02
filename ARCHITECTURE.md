# AstroBookings - Arquitectura

## Introducción

AstroBookings es una aplicación de reservas de viajes espaciales implementada con una arquitectura de puertos y adaptadores (hexagonal ligera). Los servicios de dominio solo dependen de puertos definidos en el propio dominio y las dependencias concretas se resuelven en infraestructura mediante adaptadores (repositorios en memoria y gateways de consola). Utiliza Java 21, JDK HTTP Server y Jackson para JSON. La base de datos es en memoria (HashMap).

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
│   ├── presentation/          # Adaptadores de entrada (HTTP handlers)
│   │   ├── AdminHandler.java
│   │   ├── BookingHandler.java
│   │   ├── FlightHandler.java
│   │   ├── RocketHandler.java
│   │   └── models/            # Respuestas HTTP (ErrorResponse, mapper)
│   ├── domain/                # Lógica de negocio y puertos
│   │   ├── BookingService.java 
│   │   ├── CancellationService.java
│   │   ├── FlightService.java
│   │   ├── RocketService.java
│   │   ├── models/            # Entidades planas, comandos, errores
│   │   └── ports/             # Puertos: repositorios, gateways, notificaciones
│   └── infrastructure/        # Adaptadores de los puertos
│       ├── InfrastructureFactory.java  # Composition root que resuelve puertos
│       ├── *InMemoryRepository.java    # Adaptadores en memoria de repositorios
│       └── *Console*.java              # Adaptadores en consola de gateways y servicios
├── pom.xml
├── README.md
└── ARCHITECTURE.md
```

### Capas y Componentes:

A continuación se describe cómo encaja cada capa dentro del enfoque ports & adapters:

- **presentation (adaptadores de entrada)**: HTTP handlers basados en `com.sun.net.httpserver`. Cada handler crea el servicio correspondiente, obtiene las dependencias desde `InfrastructureFactory` y transforma JSON ↔ DTOs (`ErrorResponse`, `ErrorResponseMapper`).
- **domain (núcleo + puertos)**:
  - **Servicios**: `RocketService`, `FlightService`, `BookingService`, `CancellationService`. Todo el negocio sucede aquí.
  - **Modelos**: Entidades (`Rocket`, `Flight`, `Booking`), estados (`FlightStatus`), comandos (`CreateRocketCommand`, etc.), excepciones (`BusinessException`).
  - **Puertos**: Interfaces como `RocketRepository`, `FlightRepository`, `BookingRepository`, `PaymentGateway`, `NotificationService`. Los servicios solo conocen estos puertos.
- **infrastructure (adaptadores de salida)**:
  - **Repositorios en memoria**: `RocketInMemoryRepository`, `FlightInMemoryRepository`, `BookingInMemoryRepository` implementan los puertos de persistencia.
  - **Gateways simulados**: `PaymentConsoleGateway` y `NotificationConsoleService` cumplen los puertos externos.
  - **Infraestructura común**: `InfrastructureFactory` actúa como composición e inyección manual, exponiendo instancias singleton de los adaptadores.

## Flujo de Datos y Dependencias

El flujo de control va de los adaptadores de entrada hacia el dominio, y desde el dominio hacia los puertos de salida. Las dependencias se invierten: los servicios de dominio solo conocen interfaces (puertos), y `InfrastructureFactory` provee los adaptadores concretos.

### Crear Reserva (POST /bookings)
```
Presentation (BookingHandler)
  ↓ (crea BookingService y resuelve puertos via InfrastructureFactory)
Domain Service (BookingService)
  ├─ BookingRepository / FlightRepository / RocketRepository
  ├─ PaymentGateway
  └─ NotificationService
       ↓ (puertos se satisfacen con adaptadores concretos)
Infrastructure Adapters
  ├─ BookingInMemoryRepository / FlightInMemoryRepository / RocketInMemoryRepository
  ├─ PaymentConsoleGateway
  └─ NotificationConsoleService
       ↓
Domain Models
  ├─ Booking (con paymentTransactionId)
  └─ Flight (transiciones SCHEDULED → CONFIRMED / SOLD_OUT)
```

### Cancelar Vuelos (POST /admin/cancel-flights)
```
Presentation (AdminHandler)
  ↓ (crea CancellationService con puertos desde InfrastructureFactory)
Domain Service (CancellationService)
  ├─ FlightRepository / BookingRepository
  ├─ PaymentGateway (reembolsos)
  └─ NotificationService (avisos)
       ↓ (puertos ←→ adaptadores)
Infrastructure Adapters
  ├─ FlightInMemoryRepository (actualiza estado a CANCELLED)
  ├─ BookingInMemoryRepository (obtiene reservas a reembolsar)
  ├─ PaymentConsoleGateway (simula reembolso)
  └─ NotificationConsoleService (avisa a pasajeros)
       ↓
Domain Models
  ├─ Flight (transición SCHEDULED → CANCELLED)
  └─ Booking (marca paymentTransactionId reembolsado)
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
