# AstroBookings - Arquitectura

## Introducción

AstroBookings es una aplicación de reservas de viajes espaciales implementada con arquitectura en capas. Utiliza Java 21, JDK HTTP Server y Jackson para JSON. La base de datos es en memoria (HashMap).

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
│   ├── presentation/ # HTTP Handlers 
│   │   └─ models/    # Http Responses
│   ├── business/     # Services & Gateways
│   │   └─ models/    # DTOs and Exceptions
│   └── persistence/  # Repositories & models
│       └─ models/    # Data models
├── pom.xml
├── README.md
└── ARCHITECTURE.md
```

### Capas:
- **presentation**: 
  - HTTP handlers (RocketHandler, FlightHandler, BookingHandler, AdminHandler)
  - HTTP response models (ErrorResponse)
- **business**: 
  - Logic Services (FlightService, BookingService, CancellationService) + Gateways (PaymentGateway, NotificationService)
  - DTOs and Exceptions (CreateRocketCommand, CreateFlightCommand, CreateBookingCommand, ValidationException, NotFoundException)
- **persistence**: 
  - In-memory repositories(RocketRepository, FlightRepository, BookingRepository) 
  - Data models (Rocket, Flight, Booking, FlightStatus)

## Flujo de Datos

### Crear Reserva (POST /bookings)
```
Presentation Layer
  └─ BookingHandler
       ↓
     Business Layer
       └─ BookingService
            ├─ PaymentGateway (process payment)
            └─ NotificationService (if flight confirmed)
                 ↓
               Persistence Layer
                 ├─ BookingRepository (save booking)
                 └─ FlightRepository (update flight status)
                      ↓
                    Model Layer
                      ├─ Booking (with paymentTransactionId)
                      └─ Flight (status: SCHEDULED → CONFIRMED)
```

### Cancelar Vuelos (POST /admin/cancel-flights)
```
Presentation Layer
  └─ AdminHandler
       ↓
     Business Layer
       └─ FlightCancellationService
            ├─ PaymentGateway (process refunds)
            └─ NotificationService (send cancellation emails)
                 ↓
               Persistence Layer
                 ├─ FlightRepository (find & update to CANCELLED)
                 └─ BookingRepository (get bookings for refunds)
                      ↓
                    Model Layer
                      ├─ Flight (status: SCHEDULED → CANCELLED)
                      └─ Booking (refunded via paymentTransactionId)
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

