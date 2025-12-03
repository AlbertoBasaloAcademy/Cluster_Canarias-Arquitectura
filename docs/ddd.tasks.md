# DDD Subdominios - Plan de Implementación

**Objetivo**: Reorganizar el código en dos subdominios (Sales y Fleet) manteniendo la funcionalidad actual.

**Tiempo estimado**: 1 hora

**Alcance reducido**: Solo reorganización de carpetas y paquetes. Sin eventos de dominio ni ACL.

- [ ] Crear estructura de carpetas (5 min)
- [ ] Mover Shared Kernel (5 min)
- [ ] Mover Fleet - Modelos (10 min)
- [ ] Mover Fleet - Puertos y Servicios (10 min)
- [ ] Mover Fleet - Infraestructura (10 min)
- [ ] Mover Sales - Dominio (10 min)
- [ ] Mover Sales - Infraestructura (5 min)
- [ ] Actualizar Config y Factories (5 min)
- [ ] Compilar y probar (5 min)


---

## Tareas

### 1. Crear estructura de carpetas (5 min)

Crear los siguientes directorios vacíos:

```
src/main/java/com/astrobookings/
├── shared/
│   └── models/
├── sales/
│   ├── domain/
│   │   ├── models/
│   │   ├── services/
│   │   └── ports/
│   │       ├── input/
│   │       └── output/
│   └── infrastructure/
│       ├── persistence/
│       └── presentation/
└── fleet/
    ├── domain/
    │   ├── models/
    │   ├── services/
    │   └── ports/
    │       ├── input/
    │       └── output/
    └── infrastructure/
        ├── persistence/
        └── presentation/
        └── presentation/
```

---

### 2. Mover Shared Kernel (5 min)

Mover a `shared/models/`:
- `BusinessException.java`
- `BusinessErrorCode.java`

Actualizar package: `com.astrobookings.shared.models`

---

### 3. Mover contexto Fleet - Modelos (10 min)

Mover a `fleet/domain/models/`:
- `Rocket.java`
- `CreateRocketCommand.java`
- `Flight.java`
- `FlightStatus.java`
- `CreateFlightCommand.java`

Actualizar package: `com.astrobookings.fleet.domain.models`

---

### 4. Mover contexto Fleet - Puertos y Servicios (10 min)

Mover a `fleet/domain/ports/input/`:
- `RocketsUseCases.java`
- `FlightsUseCases.java`
- `CancellationUseCases.java`

Mover a `fleet/domain/ports/output/`:
- `RocketRepository.java`
- `FlightRepository.java`
- `NotificationService.java`

Mover a `fleet/domain/services/`:
- `RocketsService.java`
- `FlightsService.java`
- `CancellationService.java`

Actualizar packages correspondientes.

---

### 5. Mover contexto Fleet - Infraestructura (10 min)

Mover a `fleet/infrastructure/persistence/`:
- `RocketInMemoryRepository.java`
- `FlightInMemoryRepository.java`
- `NotificationConsoleService.java`

Mover a `fleet/infrastructure/presentation/`:
- `RocketsHandler.java`
- `FlightsHandler.java`
- `AdminHandler.java`

Actualizar packages correspondientes.

---

### 6. Mover contexto Sales - Dominio (10 min)

Mover a `sales/domain/models/`:
- `Booking.java`
- `CreateBookingCommand.java`

Mover a `sales/domain/ports/input/`:
- `BookingsUseCases.java`

Mover a `sales/domain/ports/output/`:
- `BookingRepository.java`
- `PaymentGateway.java`

Mover a `sales/domain/services/`:
- `BookingsService.java`

Actualizar packages correspondientes.

---

### 7. Mover contexto Sales - Infraestructura (5 min)

Mover a `sales/infrastructure/persistence/`:
- `BookingInMemoryRepository.java`
- `PaymentConsoleGateway.java`

Mover a `sales/infrastructure/presentation/`:
- `BookingsHandler.java`

Actualizar packages correspondientes.

---

### 8. Actualizar Config y Factories (5 min)

Actualizar imports en:
- `Config.java`
- `AstroBookingsApp.java`
- `PersistenceAdapterFactory.java`
- `UseCasesAdapterFactory.java`
- `BaseHandler.java`
- `ErrorResponse.java` y `ErrorResponseMapper.java`

Mover factories a la carpeta correspondiente o mantener en raíz.

---

### 9. Compilar y probar (5 min)

```bash
mvn clean compile
mvn clean package
java -jar target/astrobookings-1.0-SNAPSHOT.jar
```

Verificar con los archivos `.http` en `/e2e/`.

---

## Resultado Final

```
src/main/java/com/astrobookings/
├── AstroBookingsApp.java
├── Config.java
├── shared/
│   └── models/
│       ├── BusinessException.java
│       └── BusinessErrorCode.java
├── sales/
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
└── fleet/
    ├── domain/
    │   ├── models/
    │   │   ├── Flight.java
    │   │   ├── FlightStatus.java
    │   │   ├── Rocket.java
    │   │   ├── CreateFlightCommand.java
    │   │   └── CreateRocketCommand.java
    │   ├── services/
    │   │   ├── FlightsService.java
    │   │   ├── RocketsService.java
    │   │   └── CancellationService.java
    │   └── ports/
    │       ├── input/
    │       │   ├── FlightsUseCases.java
    │       │   ├── RocketsUseCases.java
    │       │   └── CancellationUseCases.java
    │       └── output/
    │           ├── FlightRepository.java
    │           ├── RocketRepository.java
    │           └── NotificationService.java
    └── infrastructure/
        ├── persistence/
        │   ├── FlightInMemoryRepository.java
        │   ├── RocketInMemoryRepository.java
        │   └── NotificationConsoleService.java
        └── presentation/
            ├── FlightsHandler.java
            ├── RocketsHandler.java
            └── AdminHandler.java
```

---

## Tips

- Usar refactoring del IDE (Move Class, Rename Package) para actualizar imports automáticamente
- Compilar después de cada paso para detectar errores temprano
- Si hay errores de import circular, revisar dependencias entre contextos
