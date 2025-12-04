---
title: "Scalable Architecture"
description: "La arquitectura evoluciona con el proyecto."
author: "Alberto Basalo"
url: "8-scalable-architecture.md"
marp: true
theme: ab
---

# 8 Scalable Architecture

## 1. CONNECT

- ¿Cómo elegir la arquitectura adecuada para mi proyecto?

> Objetivo: comparar arquitecturas comunes y sus trade-offs para tomar decisiones informadas.

---

## 2. CONCEPTS

- **Escalabilidad por complejidad funcional**:
  - La complejidad funcional viene del dominio y los requisitos.
  - La complejidad aumenta con el tiempo y el tamaño del proyecto.

- **Escalabilidad de rendimiento**:
  - Escalar verticalmente: mejorar hardware.
  - Escalar horizontalmente: añadir más nodos.
  - Escalar por uso: separar responsabilidades.
---
## 3. CONCRETE PRACTICES

| Monolito Layered                      | Monolito modular                     | CQRS                             | Microservicios                   |
| :------------------------------------ | :----------------------------------- | :------------------------------- | :------------------------------- |
| Presentation → Business → Persistence | Módulos de dominio + infraestructura | Cambios y consultas por separado | Programas con datos propios      |
| Separación por tecnología             | Separación por funcionalidad         | Separación por uso medible       | Separación por desintegrador     |
| Acoplamiento y rigidez                | Disciplina y verbosidad              | Consistencia de datos            | Infraestructura técnica y humana |
| Mayoría de proyectos                  | Proyectos complejos                  | Ingesta o consulta masivas       | Grandes proyectos                |
---
## 4. CONCLUSIONS

- No existe una arquitectura perfecta.
- Elegir según las necesidades del proyecto y el equipo.
- Revisar y adaptar la arquitectura conforme el proyecto evoluciona.

> No es magia, es tecnología.
> > Alberto Basalo
