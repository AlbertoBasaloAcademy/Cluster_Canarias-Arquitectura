---
title: "hexagonal domain"
description: "La independencia del dominio."
author: "Alberto Basalo"
url: "3-hexagonal-domain.md"
marp: true
theme: ab
---

# 3 Hexagonal domain

El dominio es independiente de los detalles.

## 1. CONNECT 

- ¿De quién depende la lógica de negocio?

- ¿Qué consecuencias tiene esta dependencia?

> Objetivo: entender la necesidad de proteger la lógica de negocio.
---
## 2. CONCEPT 

### Conceptos clave:

- **Domain**: núcleo con la lógica de negocio.
- **Details**: el resto de la solución.
- **Ports**: interfaces de comunicación.
- **Adapters**: implementaciones concretas.

### Ideas fundamentales:

- La lógica de negocio es independiente de los detalles.
- El dominio expone sus necesidades de uso mediante ports.
- La solución implementa los ports mediante adapters.
---
## 3. CONCRETE PRACTICE 
Partimos de capas de responsabilidad única y persistencia abstracta.

Aplicar el patrón **ports y adapters** en persistencia.

> Objetivo: patrón ports y adapters y familiaridad con hexagonal architecture.

---

- Capa de negocio 
    - [ ] La capa `business` se renombra como `domain`
    - [ ] Las _interfaces_ de `infrastructure` pasan a ser _ports_ del `domain`.
    - [ ] Los _services_ dependen de los _ports_ en el constructor.

---
- Capa de persistencia
    - [ ] La capa `persistence` se renombra como `infrastructure`
    - [ ] Pierde sus _interfaces_ y depende de los _ports_ de `domain`.
    - [ ] Las implementaciones se quedan tal cual como _adapters_ de los _ports_.
    - [ ] Se crea una _factoría_ que proporciona _adapters_.

---
- Capa de presentación
    - [ ] No es necesario renombrar la capa `presentation` por el momento
    - [ ] Depende de la capa de `domain` y de la _factoría_ de `infrastructure`.
    - [ ] Usa la _factoría_ de `infrastructure` para obtener _adapters_.
    - [ ] Envía los _adapters_ a los _services_ de  `domain`.

---

## 4. CONCLUSIONS 

- Los patrones aportan claridad y consistencia.
- El objetivo es fomentar la flexibilidad y _testability_.
- La inversión del control permite la independencia de la lógica de negocio.

- ¿Cómo de independiente es tu lógica de negocio?
