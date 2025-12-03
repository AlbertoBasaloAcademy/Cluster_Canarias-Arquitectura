## Plan: Refactor dominio a DDD táctico

Pasos para pasar del modelo anémico actual a un modelo rico, reforzando invariantes en entidades/VOs y manteniendo la orquestación en servicios de dominio, respetando la arquitectura hexagonal descrita en `ARCHITECTURE.md` y el guion de `6-domain-tactic.md`.

### Steps

1. Documentar estado actual del modelo  
   - Revisar `Flight`, `Booking`, `Rocket` en `src/main/java/com/astrobookings/domain/models/` para listar setters públicos, ausencia de validaciones y reglas implementadas en servicios.  
   - Mapear en un cuadro rápido qué reglas se aplican hoy en `FlightsService`, `BookingsService`, `CancellationService` y no en las entidades.

2. Definir agregados e identificadores explícitos  
   - Confirmar agregados: `Rocket`, `Flight`, `Booking` (como indica `6-domain-tactic.md`) y que las referencias entre ellos sean siempre por ID, no por objeto.  
   - Introducir (sin aún usarlos en todos lados) `FlightId`, `RocketId`, `BookingId` como VOs simples en `domain/models` para envolver los `String` IDs y facilitar su uso posterior.

3. Enriquecer `Rocket` con invariantes básicos  
   - En `Rocket` (`domain/models/Rocket.java`) introducir un método estático `create(...)` que valide nombre no vacío y capacidad dentro de rango permitido (ya validado hoy en `RocketsService`).  
   - Reemplazar constructores abiertos y setters de `capacity` por campos inmutables o setters restringidos; centralizar la validación de capacidad en `Rocket`.  
   - Adaptar `RocketsService` a utilizar `Rocket.create(...)` en vez de construir un `Rocket` “libre” y validarlo allí.

4. Enriquecer `Flight` con lógica de ciclo de vida y validaciones  
   - En `Flight` (`domain/models/Flight.java`) añadir un `Flight.create(...)` que valide: fecha de salida futura, dentro del rango de negocio, precio base > 0, `minPassengers` dentro del rango actual usado en `FlightsService`.  
   - Hacer `setStatus(...)` privado o de paquete y exponer métodos de negocio: `schedule()`, `confirm(int currentPassengers)`, `cancelDueToLowDemand(int currentPassengers, LocalDate now)`, `markSoldOut()`.  
   - Mover al interior de `Flight` las reglas hoy en `FlightsService` y `BookingsService` que deciden cuándo pasa de `SCHEDULED` → `CONFIRMED` → `SOLD_OUT` o `CANCELLED`, dejando a los servicios solo la orquestación (lectura del repositorio, contadores, etc.).  
   - Introducir el VO `Capacity` para encapsular el rango permitido de capacidad de los cohetes y reutilizarlo donde aplique.

5. Enriquecer `Booking` con factoría y estados válidos  
   - En `Booking` (`domain/models/Booking.java`) crear `Booking.create(...)` que reciba datos completos (flightId, pasajero, precio calculado, id de pago) y garantice que no exista una reserva sin precio positivo ni sin pago asociado cuando el negocio lo requiera.  
   - Eliminar o restringir setters que permitan modificar libremente precio, id de vuelo y transacción de pago; preferir campos finales + getters.  
   - Añadir comportamiento como `markRefunded()` o equivalente, para que `CancellationService` no tenga que manipular flags o campos crudos al aplicar devoluciones.  
   - Valorar introducción de VOs como `Money`, `PassengerName`, `PaymentTransactionId` para encapsular validaciones de formato/rangos.

6. Extraer y/o formalizar política de descuentos  
   - Analizar el cálculo de precio/discount actual en `BookingsService` (reglas por cercanía de fecha, ocupación, etc.).  
   - Crear una clase o interfaz de dominio, por ejemplo `DiscountPolicy` o `PricingService` en `domain`, que reciba un contexto (`DiscountContext` con fecha de vuelo, días restantes, ocupación, capacidad, precio base) y devuelva un `Money` o descuento.  
   - Hacer que `BookingsService` delegue el cálculo de precio a esta política, y que luego llame a `Booking.create(...)` con el resultado, manteniendo la orquestación (cargar vuelo/cohete, procesar pago, guardar, notificar).

7. Recentrar `FlightsService` alrededor del `Flight` enriquecido  
   - Reemplazar la creación/validación de vuelos basada en setters por llamadas a `Flight.create(...)`.  
   - Hacer que las operaciones que cambian estado de vuelos (`createFlight`, listados filtrados, etc.) utilicen los nuevos métodos de negocio del `Flight` (p.ej. confirmar o cancelar), evitando manipular el `FlightStatus` directamente fuera de la entidad.  
   - Mantener en `FlightsService` solo la responsabilidad de interacción con repositorios y composición de respuestas hacia los puertos de entrada.

8. Recentrar `BookingsService` alrededor de `Booking` y `Flight` ricos  
   - Al crear reservas, usar `Flight` para comprobar disponibilidad/capacidad (p.ej. método `canAcceptBooking(...)` o `reserveSeat(...)`) en lugar de manualmente contar y comparar en el servicio.  
   - Llamar a `Booking.create(...)` después de que la política de precios calcule el importe final, en lugar de construir `Booking` con setters dispersos.  
   - Seguir usando puertos de salida (`PaymentGateway`, `NotificationService`, `BookingRepository`, `FlightRepository`) pero reaccionando a los resultados de métodos de dominio (p.ej. si `Flight.confirm(...)` indica transición, enviar notificación de confirmación).

9. Recentrar `CancellationService` en comportamiento de `Flight` y `Booking`  
   - Reemplazar condiciones de cancelación manuales (estado, fecha, nº pasajeros) por una llamada a algo como `flight.shouldBeCancelled(bookingsCount, now)` o `flight.cancelDueToLowDemand(...)`.  
   - Al procesar devoluciones, usar comportamiento de `Booking` (p.ej. `markRefunded()` o un método que encapsule el concepto de devolución), complementado por el uso de `PaymentGateway.refund(...)`.  
   - Mantener el servicio como dominio orquestador entre agregados y puertos, no como dueño de las reglas de cuándo se cancela un vuelo o cómo se marca una reserva devuelta.

10. Revisar límites de transacción y consistencia entre agregados  
   - Revisar puntos donde hoy se modifican `Flight` y `Booking` dentro del mismo flujo (especialmente en `BookingsService` y `CancellationService`) y documentar cómo se querría manejar en un entorno con base de datos real (eventual consistency, eventos de dominio).  
   - Introducir (aunque sea solo a nivel conceptual o de comentarios/documentación) eventos como `FlightConfirmed`, `FlightCancelled`, `BookingCreated`, `BookingRefunded` que podrían usarse para separar transacciones entre agregados manteniendo la regla “una transacción por agregado”.

11. Ajustar documentación y ejemplos de uso  
   - Actualizar `ARCHITECTURE.md` con una breve sección que describa el modelo rico: responsabilidades de `Flight`, `Booking`, `Rocket`, VOs clave y la distinción entre servicios de dominio que orquestan y entidades que encapsulan reglas.  
   - Añadir a `docs/6-domain-tactic.md` o a un nuevo doc referencias concretas a las clases y métodos introducidos (por ejemplo, mencionar `Flight.confirm`, `Booking.create`, `DiscountPolicy`) para alinear la teoría con la implementación.

### Further Considerations

1. Profundidad de Value Objects: en este taller solo se implementará explícitamente `Capacity` como ejemplo; otros posibles VOs (fechas, cantidades adicionales, etc.) se mencionan solo como idea para explorar después.  
2. Eventos de dominio: valorar si se implementan como simples objetos y llamadas síncronas por ahora, dejando abierta la opción de evolucionar hacia mensajería asíncrona.  
3. Pruebas: planificar refactor apoyándose en tests de unidad/servicio para `Flight`, `Booking`, `BookingsService` y `CancellationService` que aseguren que las reglas de negocio no cambian al moverlas a las entidades.
