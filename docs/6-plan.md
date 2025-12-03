# 6 Tactical Refactor Plan

## 1. Contexto y objetivos
- **Situacion actual:** El modelo de dominio descrito en `ARCHITECTURE.md` y el codigo bajo `src/main/java/com/astrobookings` mantiene entidades anemicas (`Flight`, `Rocket`, `Booking`) y servicios con reglas mezcladas.
- **Objetivo:** Aplicar el guion de `docs/6-domain-tactic.md` para evolucionar los bounded contexts Fleet y Sales hacia un modelo rico que encapsule invariantes, reduzca duplicidad y prepare la extraccion de modulos Maven independientes.
- **Criterios de exito:**
  1. Entidades claves exponen comportamiento de negocio y ocultan mutaciones internas.
  2. Value Object `Capacity` controla los limites de aforo y reemplaza primitivos dispersos.
  3. Servicios de dominio solo orquestan uso de agregados y puertos.
  4. Flujos descritos en `ARCHITECTURE.md` se mantienen funcionales (Smoke y E2E `./e2e/*.http`).

## 2. Principios tacticos
- **Modelo rico:** Mutaciones de estado via metodos de negocio (`confirm`, `markSoldOut`, `cancelDueToLowDemand`).
- **Value Objects:** Uso de tipos especificos para invariantes compartidas (`Capacity`).
- **Agregados consistentes:** Transacciones limitadas a una raiz (`Flight`, `Booking`).
- **Servicios delgados:** Validaciones y reglas residir segun pertenece a entidad o VO.

## 3. Roadmap de refactor

### Fase 0 · Preparacion (Fleet + Sales)
1. Inventariar usos de capacidad y estados en `FlightsService`, `RocketsService`, `BookingsService`, `CancellationService`, `FleetAdapter`.
2. Cubrir comportamiento actual con pruebas rapidas (unitarias si existen, sino smoke manual via `mvn test` + `e2e/*.http`).
3. Documentar supuestos de negocio (minimo pasajeros, umbrales de descuento) para detectar regresiones.

### Fase 1 · Value Object `Capacity` (Fleet)
1. Crear `Capacity` dentro de `com.astrobookings.fleet.domain.models`.
   - Reglas: rango 1..10 (segun servicios actuales), metodos `from(int raw)`, `ensureCanBoard(int currentPassengers)`, `maxPassengers()`.
2. Reemplazar `int capacity` de `Rocket` por `Capacity`.
   - Ajustar `CreateRocketCommand`, `RocketInMemoryRepository`, `RocketsService` y DTOs expuestos a handlers.
3. Propagar `Capacity` hacia `Flight` (campo derivado via `rocketId`).
   - Extender `Flight` para conocer su `Capacity` efectiva o exponerla via colaboracion `Rocket` en `FleetAdapter`.
4. Validar que `FleetAdapter#getRocketCapacityForFlight` y `BookingsService` utilicen `Capacity` en lugar de enteros.

### Fase 2 · Enriquecer `Flight` (Fleet)
1. Transformar `Flight` en agregado con metodos:
   - `confirmIfMinReached(int passengers)` → cambia a `CONFIRMED` y retorna boolean para notificacion.
   - `markSoldOut()` → asegura transicion unica.
   - `cancelDueToLowDemand(int currentPassengers, LocalDateTime cutoff)` → valida reglas comerciales.
   - `canAcceptNewPassenger(Capacity capacity, int currentPassengers)` → encapsula chequeos previos a reserva.
2. Despublicar setters que no aporten (mantener constructor privado + factory `Flight.schedule(...)`).
3. Ajustar `FlightsService` para delegar validaciones de fechas y minimos a `Flight` (factory valida).
4. Actualizar `FlightInMemoryRepository` y `FleetHandler` para trabajar con nuevo API (serializacion via getters especificos o DTOs).

### Fase 3 · Enriquecer `Booking` (Sales)
1. Introducir `BookingId` opcional (VO) o al menos factory estatica `Booking.create(flightId, passengerName, Money price, String transactionId)`.
2. Logica dentro de `Booking` para:
   - Validar precio positivo y pasajero no vacio.
   - Exponer metodos de lectura inmutables (sin setters publicos).
3. Ajustar `BookingRepository`, `BookingsHandler` y mapeos JSON.

### Fase 4 · Afinar servicios de dominio
1. **BookingsService**:
   - Sustituir listas y contadores por consultas encapsuladas (`Capacity.ensureCanBoard` + `Flight.canAcceptNewPassenger`).
   - Delegar calculo de descuentos en metodo privado o en nuevo VO `DiscountPolicy` (opcional si tiempo lo permite).
   - Usar respuesta de `Flight.confirmIfMinReached` para disparar `notificationService.notifyConfirmation`.
2. **CancellationService**:
   - Utilizar `Flight.cancelDueToLowDemand` para centralizar condiciones de corte antes de actualizar estado.
   - Encapsular refund/notification en metodos auxiliares para mantener claridad.
3. **FlightsService / RocketsService**:
   - Usar factories de entidades y Value Objects; eliminar setters.

### Fase 5 · Integracion y verificacion
1. Actualizar `ARCHITECTURE.md` y `docs/6-domain-tactic.md` con nuevos bloques tacticos (separado de este plan).
2. Ejecutar `mvn test` y flujos E2E (`RestClient` scripts en `e2e/`).
3. Revisar logs para confirmar
   - Cambio automatico a `CONFIRMED`/`SOLD_OUT` via metodos ricos.
   - Cancelaciones disparadas por `Flight.cancelDueToLowDemand`.
4. Preparar PR destacando como el nuevo modelo reduce acoplamiento y facilita paso a modulos Maven.

## 4. Riesgos y mitigaciones
- **Serializacion JSON:** Cambios en getters/setters pueden romper Jackson. Mitigar con constructors sin-args + anotaciones `@JsonCreator` o DTOs en capa presentacion.
- **Duplicacion de reglas:** Coordinar trabajos en Fleet y Sales para no reescribir invariantes (definir owner claro por agregado).
- **Sincronizacion de ID y capacidad:** Al introducir `Capacity`, garantizar migracion de datos en repos in-memory para evitar nulls.

## 5. Backlog extendido (post-plan)
1. VO adicionales: `Money`, `PassengerName`, `FlightDate`.
2. Eventos de dominio (`FlightConfirmed`, `BookingCreated`).
3. Extraccion de modulos Maven para Fleet y Sales.
