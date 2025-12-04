---
title: "Scalable Services"
description: "Construcción de servicios escalables y resilientes."
author: "Alberto Basalo"
url: "7-scalable-services.md"
marp: true
theme: ab
---

# 7 Scalable Services

Construcción de servicios escalables y resilientes.

## 1. CONNECT

- ¿Cómo escalar?

- O mejor... ¿por qué escalar?

> Objetivo: entender cuándo y cómo construir servicios.

---
## 2. CONCEPT

### Conceptos clave

- **Servicio**: unidad de desarrollo, despliegue y ejecución autónoma.
  - Equipos de desarrollo independientes.
  - Ciclos de vida propios.
  - Ejecución paralela, distribuida, rendundante y optimizada.
---
- **Desintegradores**: factores que promueven la separación de un módulo.
  - Funcionales:
    - Cambios frecuentes.
    - Complejidad/bloqueo
  - Técnicos:
    - Demanda hardware específica
    - Disponibilidad crítica.

---

### Ideas fundamentales

- **Centralizar vs Distribuir**
  - Repartir los problemas no es buena idea
  - Complejidad:
    - Esencial: inevitable parte del problema.
    - Accidental: añadida por la solución.

- **Cohesión vs Flexibilidad**
  - Valorar la homogeneidad.
  - Valorar la independencia.

---

## 3. CONCRETE PRACTICES

- Discutir los posibles desintegradores en el sistema actual.
  
- Proponer servicios basados en esos desintegradores.

- Command Query Responsibility Segregation (CQRS)
  
- Microservicios

---

## 4. CONCLUSION

- La desintegración añade complejidad.
- Solo es recomendable cuando los beneficios superan los costes.
- Evaluar cuidadosamente los desintegradores.
  
> Retrasa la desintegración hasta que sea inevitable.