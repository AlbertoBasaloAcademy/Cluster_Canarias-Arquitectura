
# Arquitecturas de aplicaciones: Tabla Comparativa



| Aspecto          | Monolito Layered                                 | Monolito modular                                            | CQRS                                                             | Microservicios                                                         |
| :--------------- | :----------------------------------------------- | :---------------------------------------------------------- | :--------------------------------------------------------------- | :--------------------------------------------------------------------- |
| **Definición**   | App por capas; UI→dominio→persistencia           | App modular; módulos + interfaces internas                  | Comandos vs queries; optimiza cada lado                          | Servicios pequeños e independientes; datos propios                     |
| **Ventajas**     | Simple; bajo coste; fácil depuración             | Mantenible; separación lógica; facilita extracción          | Escala lectura/escritura; modelos optimizados                    | Escalado por servicio; despliegues independientes; tolerancia a fallos |
| **Riesgos**      | Difícil de cambiar a gran escala; acoplamiento   | Requiere disciplina; riesgo de desorden                     | Más complejo; consistencia eventual; sincronización eventos      | Complejidad distribuida; coste infra; transacciones complejas          |
| **Escalado**     | Vertical; horizontal limitado                    | Vertical + algo horizontal; extracción posible              | Escala lecturas y escrituras por separado; read-models cachés    | Escala por servicio, independiente                                     |
| **Consistencia** | ACID local; transacciones simples                | ACID local por módulo                                       | Consistencia eventual entre write/read models                    | Consistencia eventual; sagas/compensaciones                            |
| **Testing**      | Simple; pruebas integradas sencillas             | Muy simple; pruebas de módulo                               | Complejo; pruebas de sincronización y eventos                    | Muy complejo; pruebas contractuales y E2E                              |
| **Impacto**      | Poco; equipos colaboran en mismo código          | Facilita equipos por módulo; menos colisiones               | Puede ser intra-equipo o multi-equipo; no exige reorg            | Alto; equipos autónomos; procesos de integración                       |
| **Indicada**     | Proyectos pequeños/medianos; MVP; entrega rápida | Monolito con límites; extracción futura; equipos crecientes | Lecturas/escrituras desbalanceadas; read-models / event sourcing | Despliegues independientes; escalado por dominio; equipos autónomos    |
