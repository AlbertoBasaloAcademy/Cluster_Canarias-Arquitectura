
# Arquitecturas de aplicaciones: Tabla Comparativa



| Aspecto          | Monolito Layered                        | Monolito modular                              | CQRS                                          | Microservicios                                   |
| :--------------- | :-------------------------------------- | :-------------------------------------------- | :-------------------------------------------- | :----------------------------------------------- |
| **Definición**   | Presentation → Business → Persistence   | Módulos de dominio + infraestructura          | Cambios y consultas por separado              | Programas con datos propios                      |
| **Ventajas**     | Simple; separación por tecnología       | Mantenible; separación por funcionalidad      | Eficiente; separación por uso                 | Independiente: separación por servicio           |
| **Riesgos**      | Acoplamiento y rigidez                  | Disciplina y verbosidad                       | Consistencia y sincronización de datos        | Infraestructura técnica y humana                 |
| **Escalado**     | Vertical; horizontal limitado           | Vertical con extracción posible               | Escala lecturas y escrituras por separado     | Escala por servicio, independiente               |
| **Consistencia** | ACID local; transacciones simples       | ACID local por módulo                         | Consistencia eventual entre write/read models | Consistencia eventual; sagas/compensaciones      |
| **Testing**      | Simple; pruebas integradas sencillas    | Muy simple; pruebas de módulo                 | Complejo; pruebas de sincronización y eventos | Muy complejo; pruebas contractuales y E2E        |
| **Impacto**      | Poco; equipos colaboran en mismo código | Positivo equipos por módulo; menos colisiones | Medio; no exige reorganización                | Alto; equipos autónomos; procesos de integración |
| **Indicada**     | Mayoría de proyectos                    | Proyectos funcionalmente complejos            | Ingesta o consulta masivas                    | Grandes proyectos                                |

