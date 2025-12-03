# 6. Plan de refactor DDD táctico

Plan de implementación para pasar del modelo anémico actual a un modelo rico en el dominio de AstroBookings, alineado con `ARCHITECTURE.md` y el guion `6-domain-tactic.md`.

## 1. Estado actual y objetivos

- Identificar dónde está hoy la lógica de negocio (principalmente en servicios de dominio) y qué entidades son solo "bolsas de datos".
- Definir qué responsabilidades deben moverse a `Rocket`, `Flight`, `Booking` y a nuevos Value Objects.
- Mantener la arquitectura hexagonal: servicios de dominio siguen implementando puertos de entrada y orquestan, pero no contienen toda la lógica.

## 2. Agregados e identificadores

1. Confirmar agregados y límites:
   - `Rocket` como agregado de Fleet.
   - `Flight` como agregado de Fleet, con referencia a `Rocket` por ID.
   - `Booking` como agregado de Sales, con referencia a `Flight` por ID.
2. Introducir Value Objects de identificación en `domain/models`:
   - `RocketId`, `FlightId`, `BookingId` (wrappers de `String`).
3. Mantener referencias entre agregados siempre por ID, no por objeto.

## 3. Enriquecer entidades de Fleet

### 3.1. `Rocket`

1. Añadir factoría estática `Rocket.create(...)` en `Rocket.java` que:
   - Valide nombre no vacío.
   - Valide capacidad en el rango permitido.
2. Centralizar la validación de capacidad en `Rocket` y restringir setters:
   - Evitar modificar capacidad sin pasar por reglas de dominio.
3. Adaptar `RocketsService` para usar `Rocket.create(...)` en lugar de construir y validar manualmente.

### 3.2. `Flight`

1. Añadir factoría `Flight.create(...)` en `Flight.java` que valide:
   - Fecha de salida futura y dentro del rango de negocio.
   - Precio base > 0.
   - `minPassengers` en el rango actual.
   - Estado inicial `SCHEDULED`.
2. Encapsular transiciones de estado:
   - Hacer `setStatus(...)` privado o de paquete.
   - Añadir métodos de negocio:
     - `confirm(int currentPassengers)`.
     - `cancelDueToLowDemand(int currentPassengers, LocalDate now)`.
     - `markSoldOut()`.
3. Mover a `Flight` las reglas hoy en `FlightsService` y `BookingsService` sobre:
   - Cuándo pasa de `SCHEDULED` a `CONFIRMED`.
   - Cuándo el vuelo se considera `SOLD_OUT`.
   - Cuándo debe cancelarse por baja demanda.
4. Introducir un VO de cantidad `Capacity` para encapsular el rango permitido de capacidad de los cohetes y reutilizarlo donde sea necesario.

## 4. Enriquecer entidades de Sales

### 4.1. `Booking`

1. Crear factoría `Booking.create(...)` en `Booking.java` que garantice:
   - `flightId` válido (no nulo/ vacío) y coherente con el contexto.
   - Datos de pasajero obligatorios.
   - Precio positivo.
   - Presencia de identificador de pago cuando el negocio lo requiera.
2. Reducir/set restringir mutabilidad:
   - Preferir campos finales + getters.
   - Eliminar setters que permitan dejar reservas en estados inválidos.
3. Añadir comportamiento de ciclo de vida:
   - `markRefunded()` (o similar) para representar devoluciones.
4. Opcional: introducir VOs como:
   - `Money` para precios.
   - `PassengerName` / `Passenger`.
   - `PaymentTransactionId`.

### 4.2. Política de descuentos y precios

1. Analizar el cálculo de precio actual en `BookingsService`.
2. Extraer una política de descuentos en el dominio:
   - Interfaz o clase `DiscountPolicy` / `PricingService`.
   - VO `DiscountContext` (fecha de vuelo, días restantes, ocupación, capacidad, precio base).
3. Hacer que `BookingsService` delegue el cálculo de precio en esta política y luego llame a `Booking.create(...)`.

## 5. Recentrar servicios de dominio

### 5.1. `FlightsService`

1. Usar `Flight.create(...)` para crear vuelos en lugar de construir con setters.
2. Usar los nuevos métodos de negocio de `Flight` para cambiar estados:
   - Confirmar vuelos.
   - Marcar como llenos.
   - Cancelar por baja demanda.
3. Mantener responsabilidad de:
   - Acceso a repositorios.
   - Construcción de respuestas para los puertos de entrada.

### 5.2. `BookingsService`

1. Delegar en `Flight` la comprobación de disponibilidad/capacidad:
   - Métodos como `canAcceptBooking(...)` o `reserveSeat(...)`.
2. Delegar el cálculo de precio en la política de descuentos.
3. Crear reservas con `Booking.create(...)` en lugar de setters dispersos.
4. Mantener:
   - Orquestación de repositorios (`FlightRepository`, `BookingRepository`).
   - Interacción con `PaymentGateway` y `NotificationService`.

### 5.3. `CancellationService`

1. Delegar en `Flight` la decisión de cancelación:
   - Usar `cancelDueToLowDemand(...)` o `shouldBeCancelled(...)`.
2. Usar `Booking` para representar devoluciones:
   - Llamar a `markRefunded()` (o similar) antes/después de invocar `PaymentGateway.refund(...)`.
3. Mantener la orquestación entre agregados y puertos de salida.

## 6. Transacciones, consistencia y eventos

1. Revisar dónde se modifican `Flight` y `Booking` en el mismo flujo:
   - Especialmente en `BookingsService` y `CancellationService`.
2. Documentar estrategia de consistencia eventual para un futuro con BD real:
   - Una transacción por agregado (regla del guion táctico).
3. Definir eventos de dominio conceptuales:
   - `FlightConfirmed`, `FlightCancelled`, `BookingCreated`, `BookingRefunded`.
   - Usarlos como base para separar responsabilidades y futuros flujos asíncronos.

## 7. Documentación y pruebas

1. Actualizar `ARCHITECTURE.md` con una sección de "Modelo Rico":
   - Responsabilidades de `Rocket`, `Flight`, `Booking`.
   - Principales Value Objects.
   - Distinción clara entre entidades con comportamiento y servicios orquestadores.
2. Alinear `docs/6-domain-tactic.md` con los nombres concretos:
   - Mencionar métodos como `Flight.confirm`, `Flight.cancelDueToLowDemand`, `Booking.create`, `DiscountPolicy`.
3. Plan de pruebas:
   - Añadir o adaptar tests de unidad para `Flight`, `Booking` y los servicios de dominio.
   - Verificar que las invariantes se respetan y que el comportamiento observable (endpoints) se mantiene.
