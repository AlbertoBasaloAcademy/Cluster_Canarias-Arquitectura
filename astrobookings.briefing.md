# AstroBookings — Briefing

App de reservas para viajes espaciales. Gestiona cohetes, vuelos y reservas con control de capacidad, precios y estados.

## Objetivo
- Ofrecer vuelos en cohetes
- Reservar viajes para un pasajero
- Controlar capacidad, descuentos y estados
- Notificar confirmación/cancelación
- Procesar pagos y devoluciones

## Entidades

- `Rocket`
    - Nombre (obligatorio), capacidad (máx. 10), velocidad (opcional)

- `Flight`
    - Fecha futura, precio base, mínimo pasajeros (5 default)
    - Estados: `SCHEDULED`, `CONFIRMED`, `SOLD_OUT`, `CANCELLED`
    - Cambio automático según reservas

- `Booking`
    - Pasajero + precio final
    - Solo si vuelo no lleno ni cancelado

## Lógica

- No superar capacidad cohete → `SOLD_OUT` al límite
- Alcanza mínimo pasajeros → `CONFIRMED` + notificar
- Falta 1 semana sin mínimo → `CANCELLED` + notificar + devolver pago

- Descuentos (precedencia, solo uno)
    1. Última plaza → 0%
    2. Falta 1 para mínimo → 30%
    3. Más 6 meses antes → 10%
    4. Entre 1 mes y 1 semana → 20%
    5. Resto → 0%

## Funcionalidades

- Listar y crear `Rocket` con validación
- Listar `Flight` futuros (filtro estado)
- Crear `Flight` con fecha futura y precio > 0 → `SCHEDULED`
- Crear `Booking` con descuentos + procesar pago
- Consultar `Booking` por vuelo/pasajero
- Cancelar `Flight` → `CANCELLED` + notificar + devolver pago
