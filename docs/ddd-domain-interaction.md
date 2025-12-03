# DDD - Comunicación entre Subdominios

## Problema Actual: Acoplamiento Directo

En el código actual, **Sales escribe directamente en entidades de Fleet**, violando los límites del bounded context.

### Punto de Acoplamiento 1: BookingsService modifica Flight

```java
// BookingsService.java (Sales) - líneas 76-82
if (currentBookings >= capacity) {
  flight.setStatus(FlightStatus.SOLD_OUT);        // ❌ Sales escribe en Fleet
} else if (currentBookings >= flight.getMinPassengers()...) {
  flight.setStatus(FlightStatus.CONFIRMED);       // ❌ Sales escribe en Fleet
}
flightRepository.save(flight);                    // ❌ Sales persiste entidad de Fleet
```

### Punto de Acoplamiento 2: Sales lee directamente de Fleet

```java
// BookingsService.java (Sales) - líneas 42-58
Flight flight = flightRepository.findById(...);   // ❌ Acceso directo a Fleet
Rocket rocket = rocketRepository.findById(...);   // ❌ Acceso directo a Fleet
int capacity = rocket.getCapacity();              // ❌ Usa modelo de Fleet
```

### Punto de Acoplamiento 3: CancellationService cruza dominios

```java
// CancellationService.java (Fleet)
List<Booking> bookings = bookingRepository.findByFlightId(...);  // ❌ Lee de Sales
paymentGateway.processRefund(...);                               // ❌ Usa servicio de Sales
```

---

## Solución: Puertos de Comunicación entre Contextos

La forma más sencilla de desacoplar sin introducir eventos de dominio es crear **puertos de servicio** que definan contratos claros entre contextos.

### Diagrama de Comunicación

```
┌─────────────────────────────────────────────────────────────────┐
│                    COMUNICACIÓN ENTRE CONTEXTOS                 │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌───────────────┐                     ┌───────────────┐      │
│   │     FLEET     │                     │     SALES     │      │
│   │               │                     │               │      │
│   │ FlightStatus  │◄─── consulta ───────│ BookingsService│     │
│   │   UseCases    │     availability    │               │      │
│   │               │                     │               │      │
│   │               │◄─── notifica ───────│               │      │
│   │               │   onBookingCreated  │               │      │
│   │               │                     │               │      │
│   │ Cancellation  │──── consulta ──────►│ BookingQuery  │      │
│   │   Service     │   countByFlightId   │    Port       │      │
│   │               │                     │               │      │
│   └───────────────┘                     └───────────────┘      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Implementación

### 1. Puerto de Fleet para Sales (consulta disponibilidad)

Sales necesita saber si puede reservar en un vuelo. Fleet expone esta información:

```java
// fleet/domain/ports/input/FlightStatusUseCases.java
public interface FlightStatusUseCases {
    
    /**
     * Obtiene información de disponibilidad de un vuelo.
     * Sales usa esto para validar y calcular precios.
     */
    FlightAvailability getAvailability(String flightId);
    
    /**
     * Notifica que se ha creado una reserva.
     * Fleet actualiza el estado del vuelo si corresponde.
     */
    void onBookingCreated(String flightId);
}
```

### 2. Value Object de disponibilidad (Shared o en Fleet)

```java
// fleet/domain/models/FlightAvailability.java
public record FlightAvailability(
    String flightId,
    double basePrice,
    LocalDateTime departureDate,
    int availableSeats,
    int minPassengers,
    int currentBookings,
    boolean isBookable
) {
    public boolean isLastSeat() {
        return availableSeats == 1;
    }
    
    public boolean reachesMinimum() {
        return currentBookings + 1 == minPassengers;
    }
    
    public long daysUntilDeparture() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), departureDate);
    }
}
```

### 3. Servicio de Fleet que implementa el puerto

```java
// fleet/domain/services/FlightStatusService.java
public class FlightStatusService implements FlightStatusUseCases {
    
    private final FlightRepository flightRepository;
    private final RocketRepository rocketRepository;
    private final BookingQueryPort bookingQuery;  // Puerto hacia Sales
    private final NotificationService notificationService;
    
    @Override
    public FlightAvailability getAvailability(String flightId) {
        Flight flight = flightRepository.findById(flightId);
        Rocket rocket = rocketRepository.findById(flight.getRocketId());
        int currentBookings = bookingQuery.countByFlightId(flightId);
        int availableSeats = rocket.getCapacity() - currentBookings;
        
        boolean isBookable = flight.getStatus() != FlightStatus.CANCELLED 
                          && flight.getStatus() != FlightStatus.SOLD_OUT
                          && availableSeats > 0;
        
        return new FlightAvailability(
            flightId,
            flight.getBasePrice(),
            flight.getDepartureDate(),
            availableSeats,
            flight.getMinPassengers(),
            currentBookings,
            isBookable
        );
    }
    
    @Override
    public void onBookingCreated(String flightId) {
        Flight flight = flightRepository.findById(flightId);
        Rocket rocket = rocketRepository.findById(flight.getRocketId());
        int currentBookings = bookingQuery.countByFlightId(flightId);
        
        if (currentBookings >= rocket.getCapacity()) {
            flight.setStatus(FlightStatus.SOLD_OUT);
            flightRepository.save(flight);
        } else if (currentBookings >= flight.getMinPassengers() 
                   && flight.getStatus() == FlightStatus.SCHEDULED) {
            flight.setStatus(FlightStatus.CONFIRMED);
            flightRepository.save(flight);
            notificationService.notifyConfirmation(flightId, currentBookings);
        }
    }
}
```

### 4. Puerto de Sales para Fleet (consulta reservas)

Fleet necesita saber cuántas reservas hay para un vuelo:

```java
// sales/domain/ports/input/BookingQueryPort.java
public interface BookingQueryPort {
    
    /**
     * Cuenta las reservas de un vuelo.
     * Fleet usa esto para determinar estados.
     */
    int countByFlightId(String flightId);
    
    /**
     * Lista las reservas de un vuelo.
     * Fleet usa esto para procesar reembolsos.
     */
    List<BookingInfo> findByFlightId(String flightId);
}

// Value object para no exponer la entidad Booking
public record BookingInfo(
    String bookingId,
    String passengerName,
    double finalPrice,
    String paymentTransactionId
) {}
```

### 5. BookingsService refactorizado (Sales)

```java
// sales/domain/services/BookingsService.java
public class BookingsService implements BookingsUseCases {
    
    private final BookingRepository bookingRepository;
    private final PaymentGateway paymentGateway;
    private final FlightStatusUseCases flightStatus;  // Puerto hacia Fleet
    
    public Booking createBooking(CreateBookingCommand command) {
        // 1. Consultar disponibilidad a Operations
        FlightAvailability availability = flightStatus.getAvailability(command.flightId());
        
        if (!availability.isBookable()) {
            throw new BusinessException(BusinessErrorCode.VALIDATION, 
                "Flight is not available for booking");
        }
        
        // 2. Calcular descuento (lógica de Sales)
        double discount = calculateDiscount(availability);
        double finalPrice = availability.basePrice() * (1 - discount);
        
        // 3. Procesar pago
        String transactionId = paymentGateway.processPayment(finalPrice);
        
        // 4. Crear reserva
        Booking booking = new Booking();
        booking.setFlightId(command.flightId());
        booking.setPassengerName(command.passengerName());
        booking.setFinalPrice(finalPrice);
        booking.setPaymentTransactionId(transactionId);
        Booking savedBooking = bookingRepository.save(booking);
        
        // 5. Notificar a Operations
        flightStatus.onBookingCreated(command.flightId());
        
        return savedBooking;
    }
    
    private double calculateDiscount(FlightAvailability availability) {
        if (availability.isLastSeat()) {
            return 0.0;
        } else if (availability.reachesMinimum()) {
            return 0.3;
        } else if (availability.daysUntilDeparture() > 180) {
            return 0.1;
        } else if (availability.daysUntilDeparture() >= 7 
                   && availability.daysUntilDeparture() <= 30) {
            return 0.2;
        }
        return 0.0;
    }
}
```

---

## Configuración (Config.java)

```java
// Config.java
public class Config {
    
    public void configure() {
        // Adaptadores de persistencia
        BookingRepository bookingRepo = new BookingInMemoryRepository();
        FlightRepository flightRepo = new FlightInMemoryRepository();
        RocketRepository rocketRepo = new RocketInMemoryRepository();
        PaymentGateway paymentGw = new PaymentConsoleGateway();
        NotificationService notificationSvc = new NotificationConsoleService();
        
        // Puerto de Sales para Fleet
        BookingQueryPort bookingQuery = new BookingQueryAdapter(bookingRepo);
        
        // Servicio de Fleet (necesita puerto de Sales)
        FlightStatusUseCases flightStatus = new FlightStatusService(
            flightRepo, rocketRepo, bookingQuery, notificationSvc
        );
        
        // Servicio de Sales (necesita puerto de Fleet)
        BookingsUseCases bookings = new BookingsService(
            bookingRepo, paymentGw, flightStatus
        );
        
        // ... registrar handlers
    }
}
```

---

## Resumen de Cambios

| Antes (Acoplado)                   | Después (Desacoplado)                              |
| ---------------------------------- | -------------------------------------------------- |
| Sales accede a `FlightRepository`  | Sales usa `FlightStatusUseCases.getAvailability()` |
| Sales accede a `RocketRepository`  | Datos incluidos en `FlightAvailability`            |
| Sales modifica `Flight.status`     | Sales llama `flightStatus.onBookingCreated()`      |
| Sales persiste `Flight`            | Fleet gestiona sus propias entidades               |
| Fleet accede a `BookingRepository` | Fleet usa `BookingQueryPort`                       |

---

## Beneficios

1. **Límites claros**: Cada contexto gestiona sus propias entidades
2. **Contratos explícitos**: Los puertos definen qué información se comparte
3. **Testabilidad**: Fácil mockear puertos en tests unitarios
4. **Evolución independiente**: Cambios internos no afectan al otro contexto
5. **Sin infraestructura adicional**: No requiere message broker ni eventos asíncronos

---

## Alternativa Futura: Eventos de Dominio

Para mayor desacoplamiento, se podrían introducir eventos:

```java
// Evento publicado por Sales
public record BookingCreatedEvent(String flightId, String bookingId) {}

// Evento publicado por Fleet  
public record FlightConfirmedEvent(String flightId, int passengers) {}
public record FlightCancelledEvent(String flightId, List<String> bookingIds) {}
```

Esto permitiría comunicación asíncrona y mayor independencia, pero añade complejidad de infraestructura (event bus, handlers, etc.).
