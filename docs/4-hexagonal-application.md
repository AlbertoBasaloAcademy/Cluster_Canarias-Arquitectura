---
title: "hexagonal application"
description: "La independencia del dominio."
author: "Alberto Basalo"
url: "4-hexagonal-application.md"
marp: true
theme: ab
---
# 4 Hexagonal application

La aplicación es un intermediario entre el dominio y el usuario.

## 1. CONNECT

- ¿Qué ocurre con la presentación?

- ¿Puede cambiar o coexistir con otras presentaciones?

> Objetivo: Todo lo que no es dominio son detalles.
---
## 2. CONCEPT 

### Conceptos clave:

- **Driving Adapters**: adaptadores de entrada (presentación).
- **Driven Adapters**: adaptadores de salida (persistencia).

### Ideas fundamentales:

- La presentación también es un detalle.
- Pasamos a una visión concéntrica
---
## 3. CONCRETE PRACTICE 

Partimos de capas de dominio y persistencia con ports y adapters. 

Implementar la **Arquitectura Hexagonal** completa para la capa de aplicación.

---

- Capa de aplicación 
    - [ ] Se crea una nueva carpeta `application`.
    - [ ] Se definen _use cases_ a partir de los _services_ de `domain`.
    - [ ] Se extraen y publican _ports_ de los _use cases_.
- Capa de presentación
    - [ ] Desaparece como tal.
    - [ ] Los _handlers_ se mueven a `infrastructure` 
- Capa de infrastructure
    - [ ] Los handlers dependen de los _ports_ de `application`.
    - [ ] Los handlers invocan a los _use cases_ 
      - (como antes hacían con los _services_).
---
- Configuración global
    - [ ] Se necesita un clase _configuration_ para la inyección de dependencias.
    - [ ] Opcionalmente agrupa los _adapters_ de `infrastructure` en `persistence` y `presentation`.
---

## 4. CONCLUSIONS 

- De capas apiladas a concéntricas
- Dominio en el centro, infraestructura en el exterior
- La aplicación en el medio

> ¿Cómo de independiente es tu lógica de negocio?