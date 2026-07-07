# plan.md — Plan de Implementación

## Metodología: Spec Driven Development

Cada fase se completa y valida antes de avanzar a la siguiente.
No se escribe código de una fase sin haber cerrado la anterior.

---

## Fase 0 — Infraestructura y Configuración Base
**Objetivo:** Tener el proyecto Spring Boot corriendo y conectado a la BD en Docker.

- [x] Levantar contenedor PostgreSQL con Docker Compose (puerto 5434)
- [x] Crear proyecto Spring Boot con Maven
      Dependencias: Spring Web, Spring Data JPA, PostgreSQL Driver, Flyway, Lombok, Validation
- [x] Configurar `application.properties` con conexión a la BD
- [x] Verificar conexión exitosa a la base de datos
- [x] Crear script Flyway `V1__insert_paises.sql` con datos iniciales de países

**Criterio de aceptación:** La aplicación arranca sin errores y se conecta a PostgreSQL.

---

## Fase 1 — Entidades y Base de Datos (Code-First)
**Objetivo:** Generar el esquema de BD a partir de las entidades JPA definidas en spec.md.

- [x] Crear entidad `Pais`
- [x] Crear entidad `TipoCambio` + enum `FuenteTipoCambio`
- [x] Crear entidad `Egreso`
- [x] Crear entidad `Ingreso` (inmutable — solo lectura)
- [x] Crear entidad `Licencia` + enum `TipoLicencia`
- [x] Verificar que Hibernate genera las tablas correctamente en la BD
- [x] Verificar que Flyway inserta los países iniciales

**Criterio de aceptación:** Las 5 tablas existen en PostgreSQL con sus relaciones y datos iniciales.

---

## Fase 2 — Repositorios y Capa de Datos
**Objetivo:** Definir todas las consultas necesarias para los reportes y operaciones.

- [x] `PaisRepository` — findAll, findById
- [x] `TipoCambioRepository` — findTopByPaisOrderByFechaConsultaDesc (último tipo de cambio por país)
- [x] `EgresoRepository` — query de reporte con filtros fecha y país opcional, agrupado por país
- [x] `IngresoRepository` — query de reporte con filtros fecha y país opcional, agrupado por país (solo lectura)
- [x] `LicenciaRepository` — findByClave, findFirstByActivaTrue

**Criterio de aceptación:** Tests unitarios de repositorios pasan correctamente.

---

## Fase 3 — Servicio de Tipo de Cambio
**Objetivo:** Integrar la API externa con fallback a BD.

- [x] Crear `ExchangeRateApiClient` (RestTemplate o WebClient)
      → Consume `ExchangeRate-API` y retorna el valor USD para una moneda dada
- [x] Crear `TipoCambioService`
      → `obtenerTipoCambio(Pais pais)`: intenta API, si falla usa último registro en BD
      → `guardarTipoCambio(Pais, valor, fuente)`: persiste el tipo de cambio consultado
- [x] Manejar correctamente el caso de fallo de API (timeout, sin conexión, moneda no disponible)
- [x] Loggear cuando se usa el fallback manual

**Criterio de aceptación:** El servicio convierte correctamente ARS, BRL y EUR a USD.
El fallback funciona cuando la API no está disponible.

---

## Fase 4 — Módulo de Egresos
**Objetivo:** Implementar el alta de egresos con conversión automática.

- [x] Crear `EgresoService`
      → `registrarEgreso(EgresoRequestDto)`: orquesta la conversión y persistencia
      → Flujo: obtener país → obtener tipo de cambio → calcular USD → persistir egreso
- [x] Crear `EgresoController` con endpoint `POST /api/egresos`
- [x] Validar todos los campos del `EgresoRequestDto`
- [x] Retornar `EgresoResponseDto` con los datos completos incluyendo importe en USD

**Criterio de aceptación:** Se puede registrar un egreso via API REST y el importe en USD
se calcula y persiste correctamente.

---

## Fase 5 — Reporte de Egresos
**Objetivo:** Implementar el reporte filtrado y agrupado por país.

- [x] Crear query JPQL en `EgresoRepository` con parámetros fecha_inicio, fecha_fin, pais_id (nullable)
- [x] Agregar método en `EgresoService` que agrupa los resultados por país
- [x] Calcular subtotales por país (total moneda local, total USD)
- [x] Crear endpoint `GET /api/egresos/reporte` en `EgresoController`
      → Parámetros: `fechaInicio`, `fechaFin`, `paisId` (opcional)
- [x] Retornar lista de `ReporteEgresosDto` agrupada por país

**Criterio de aceptación:** El reporte retorna egresos correctamente filtrados, agrupados
por país, con subtotales en moneda local y USD.

---

## Fase 6 — Reporte de Ingresos
**Objetivo:** Implementar el reporte de ingresos desde el sistema externo (solo lectura).

> ⚠️ Esta fase puede requerir ajuste cuando se reciba la estructura de BD del cliente.
> Se implementa con la estructura definida en spec.md y se adapta al recibir la BD real.

- [x] Crear query en `IngresoRepository` con los mismos filtros que egresos
- [ ] Crear `IngresoService` con método de reporte agrupado por país
- [ ] Crear endpoint `GET /api/ingresos/reporte` en `IngresoController`
- [ ] Verificar que no existe ningún endpoint de escritura para ingresos

**Criterio de aceptación:** El reporte retorna ingresos correctamente filtrados y agrupados.
No existe ningún endpoint POST/PUT/DELETE para ingresos.

---

## Fase 7 — Sistema de Licencias
**Objetivo:** Implementar el control de acceso por licencia.

- [ ] Crear `LicenciaService`
      → `validarLicencia()`: verifica si existe licencia activa y vigente
      → `activarLicencia(clave)`: activa una licencia por su clave
      → `verificarVencimiento()`: marca como inactiva si venció
- [ ] Crear `LicenciaController`
      → `GET /api/licencia/estado`
      → `POST /api/licencia/activar`
- [ ] Crear `LicenciaInterceptor`
      → Intercepta todos los requests excepto `/api/licencia/activar`
      → Si no hay licencia vigente → retorna HTTP 403 con mensaje claro
- [ ] Registrar el interceptor en la configuración de Spring MVC

**Criterio de aceptación:** Sin licencia activa, todos los endpoints retornan 403.
Con licencia activa, el sistema funciona normalmente.
Una licencia vencida se marca automáticamente como inactiva.

---

## Fase 8 — Endpoint de Países
**Objetivo:** Exponer los países para poblar los selectores del frontend.

- [ ] Crear `PaisController` con `GET /api/paises`
- [ ] Retornar lista de países con id, nombre, monedaCodigo, monedaNombre

**Criterio de aceptación:** El endpoint retorna todos los países registrados.

---

## Fase 9 — Manejo Global de Errores
**Objetivo:** Respuestas de error consistentes y comprensibles.

- [ ] Crear `GlobalExceptionHandler` con `@RestControllerAdvice`
- [ ] Manejar: validaciones (`MethodArgumentNotValidException`), entidad no encontrada, error de API de cambio, licencia vencida
- [ ] Todos los errores retornan JSON con estructura: `{ "error": "...", "mensaje": "..." }`
- [ ] Los mensajes deben ser comprensibles para un usuario no técnico

**Criterio de aceptación:** Ningún error expone stack traces al cliente.
Todos los errores tienen un mensaje claro en español.

---

## Fase 10 — Pruebas y Ajuste Final
**Objetivo:** Validar el sistema completo de extremo a extremo.

- [ ] Prueba completa del flujo de egresos: alta → conversión → reporte
- [ ] Prueba del reporte de ingresos con distintos filtros
- [ ] Prueba del sistema de licencias: sin licencia, con licencia vigente, con licencia vencida
- [ ] Prueba del fallback de tipo de cambio (simular fallo de API)
- [ ] Ajuste de la entidad `Ingreso` si se recibe la BD del cliente
- [ ] Revisión general de mensajes de error

---

## Resumen de Fases

| Fase | Descripción | Depende de |
|---|---|---|
| 0 | Infraestructura y configuración | — |
| 1 | Entidades y BD Code-First | Fase 0 |
| 2 | Repositorios | Fase 1 |
| 3 | Servicio tipo de cambio | Fase 2 |
| 4 | Alta de egresos | Fase 3 |
| 5 | Reporte de egresos | Fase 4 |
| 6 | Reporte de ingresos | Fase 2 |
| 7 | Sistema de licencias | Fase 2 |
| 8 | Endpoint países | Fase 1 |
| 9 | Manejo de errores | Fase 4, 5, 6 |
| 10 | Pruebas y ajuste final | Todas |

---

## Notas Finales

- La **Fase 6** (ingresos) puede quedar pendiente hasta recibir la estructura de BD del cliente,
  sin bloquear el resto del desarrollo.
- El proyecto puede entregarse funcional sin la Fase 6 si la BD del cliente demora.
- Cada fase debe commitearse por separado con mensajes descriptivos.
