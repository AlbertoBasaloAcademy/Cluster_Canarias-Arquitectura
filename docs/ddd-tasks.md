# Plan de Implementación: Refactor a Módulos DDD

Este plan está diseñado para ser ejecutado en **1 hora**. El objetivo es separar físicamente el código en dos subdominios (`fleet` y `sales`) y establecer una comunicación explícita entre ellos.

## Lista de Tareas

1.  **Crear Estructura de Paquetes**:
    *   Crear los paquetes raíz: `com.astrobookings.fleet`, `com.astrobookings.sales`, `com.astrobookings.shared`.

2.  **Mover Kernel Compartido**:
    *   Mover excepciones (`BusinessException`, `BusinessErrorCode`) y utilidades comunes a `com.astrobookings.shared`.

3.  **Migrar Subdominio Fleet**:
    *   Mover entidades `Rocket`, `Flight` y `FlightStatus` a `fleet.domain`.
    *   Mover puertos `RocketRepository`, `FlightRepository` y servicios `RocketsService`, `FlightsService` a `fleet`.
    *   Mover sus adaptadores (Handlers y Repositorios en memoria) a `fleet.infrastructure`.

4.  **Migrar Subdominio Sales**:
    *   Mover entidad `Booking` a `sales.domain`.
    *   Mover puertos `BookingRepository` y servicio `BookingsService` a `sales`.
    *   Mover sus adaptadores a `sales.infrastructure`.

5.  **Definir Contrato de Comunicación (Sales)**:
    *   En `sales.domain.ports.out`, crear una interfaz `FlightInfoProvider` (o `FlightGateway`) con el método `getFlightStatus(String flightId)`.
    *   *Objetivo*: Que `BookingsService` no dependa de `FlightRepository`.

6.  **Implementar Adaptador de Comunicación**:
    *   En `sales.infrastructure`, crear `FlightAdapter` que implemente `FlightInfoProvider`.
    *   Este adaptador inyectará `fleet.application.FlightsUseCases` (o el servicio) para obtener los datos.

7.  **Refactorizar BookingService**:
    *   Reemplazar la dependencia de `FlightRepository` por `FlightInfoProvider`.
    *   Actualizar la lógica de validación para usar este nuevo puerto.

8.  **Mover Cancelación (Opcional/Avanzado)**:
    *   Mover `CancellationService` a `sales` (Cancelación Comercial).
    *   Definir un puerto en `sales` para notificar a `fleet` (`FlightCancellationNotifier`) y que `fleet` actualice el estado del vuelo.

9.  **Recablear la Aplicación (Composition Root)**:
    *   Actualizar `Config.java` y `AstroBookingsApp.java`.
    *   Instanciar primero el módulo `Fleet`.
    *   Instanciar el módulo `Sales` inyectándole el adaptador que conecta con `Fleet`.

10. **Verificación Final**:
    *   Corregir imports rotos (IDE assist).
    *   Ejecutar `mvn clean compile` y los tests E2E para asegurar que la refactorización no rompió la funcionalidad.
