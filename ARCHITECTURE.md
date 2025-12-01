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
│   ├── business/     # Services & Gateways
│   └── persistence/     # Repositories & models
│       └─ models/    # Data models
├── pom.xml
├── README.md
└── ARCHITECTURE.md
```

### Capas:
- **presentation**: 
  - HTTP handlers (RocketHandler, FlightHandler, BookingHandler, AdminHandler)
- **business**: 
  - Logic Services (FlightService, BookingService, CancellationService) + Gateways (PaymentGateway, NotificationService)
- **persistence**: 
  - In-memory repositories(RocketRepository, FlightRepository, BookingRepository) + Data models (Rocket, Flight, Booking, FlightStatus)

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

## Refactor de responsabilidades

Para corregir la mezcla de responsabilidades se definió el siguiente enfoque iterativo (sin introducir factories ni nuevas abstracciones innecesarias):

- **DTOs de entrada en negocio**: se agregaron records en `src/main/java/com/astrobookings/business/models` (`CreateRocketCommand`, `CreateFlightCommand`, `CreateBookingCommand`). Se usan como contrato de entrada para los servicios, pero su validación se reparte así: la capa de presentación verifica estructura y tipos básicos antes de construir el DTO; la capa de negocio valida los valores (capacidad, fechas, disponibilidad, etc.).
- **Servicios como únicos dueños de repos**: los handlers ya no instancian ni llaman repositorios. Cada servicio (`RocketService`, `FlightService`, `BookingService`, `CancellationService`) encapsula su acceso a `persistence` y devuelve modelos de dominio; la serialización vuelve a la capa de presentación.
- **Excepciones de negocio**: se creó la jerarquía `business/exception` con `BusinessException` + especializaciones (`ValidationException`, `NotFoundException`, `PaymentException`, `CapacityException`). Los servicios lanzan estas excepciones cuando una regla de negocio falla.
- **Respuestas de error unificadas**: `presentation/ErrorResponseMapper` traduce cualquier excepción de negocio en `{ "code": "...", "message": "..." }` y asigna automáticamente el HTTP status (400 validación, 404 not found, 402 pago, 409 capacidad, 500 interno). `BaseHandler` centraliza el envío de estas respuestas.
- **Handlers más delgados**: cada handler se limita a: parsear JSON → validar estructura → construir DTO → invocar servicio → serializar respuesta. También comparten utilidades de parsing en `BaseHandler` (lectura de body, parseo de query strings y manejo de errores).

Este refactor apunta a que futuras funcionalidades sigan el mismo patrón: cualquier nueva entrada debe definir su DTO en `business/models`, validar estructura en la capa de presentación, realizar validaciones de valor en el servicio correspondiente y usar la jerarquía de excepciones para mantener las respuestas homogéneas.
---