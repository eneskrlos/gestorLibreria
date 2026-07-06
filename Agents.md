# Agents.md

> ⚠️ **LECTURA OBLIGATORIA**
> Antes de ejecutar cualquier tarea, lee este archivo completo.
> Luego lee los archivos en el siguiente orden:
> 1. `spec/CLAUDE.md`
> 2. `spec/spec.md`
> 3. `spec/plan.md`
>
> No escribas ninguna línea de código hasta haber leído los cuatro archivos.

---

## 1. Contexto del Proyecto

Sistema web desarrollado en **Java 17 con Spring Boot 3.x** para el propietario de una
librería en Montevideo, Uruguay. El cliente importa libros desde el exterior y necesita:

- Consultar reportes de ingresos provenientes de un sistema externo ya existente (solo lectura).
- Registrar egresos (gastos e impuestos de importación por país) con conversión automática a USD.
- Consultar reportes de egresos filtrados por fecha y/o país.

El sistema convive con una aplicación externa del cliente que comparte la misma base de datos
PostgreSQL. Este sistema **no modifica ni inserta ingresos** — solo los lee para reportes.

**Stack:**
- Java 17 + Spring Boot 3.x
- PostgreSQL 15 (Docker, puerto 5434)
- Spring Data JPA / Hibernate — Code-First (`ddl-auto=update`)
- Flyway — datos iniciales (países)
- Maven
- Git Flow (ya inicializado en el repositorio)

**Ubicación de documentos de especificación:**
```
spec/
├── CLAUDE.md   ← Contexto técnico completo
├── spec.md     ← Entidades, DTOs, endpoints, reglas de negocio
└── plan.md     ← Fases de implementación con criterios de aceptación
```

---

## 2. Reglas que Debes Seguir Sin Excepción

### Regla 1 — Ajuste estricto a la especificación

- Implementa **únicamente** lo que está definido en `spec/spec.md` y `spec/plan.md`.
- No agregues campos, endpoints, entidades ni funcionalidades que no estén especificados.
- Si algo no está claro en la especificación, **detente y pregunta** antes de asumir.
- No cambies nombres de clases, paquetes, tablas o columnas definidos en `spec/spec.md`.

### Regla 2 — Una tarea a la vez

- Ejecuta **una sola tarea** por turno, exactamente la que se te indique.
- Al terminar una tarea, **detente completamente** y espera confirmación antes de continuar.
- No anticipes tareas siguientes aunque parezcan obvias o relacionadas.
- No avances a la siguiente fase sin que el desarrollador lo apruebe explícitamente.

### Regla 3 — Tests obligatorios

- Toda clase de servicio (`Service`) debe tener su test unitario correspondiente.
- Todo endpoint (`Controller`) debe tener su test de integración.
- No se da por cerrada una tarea que incluya lógica de negocio sin su test.
- Los tests deben cubrir tanto el caso exitoso como los casos de error principales.
- Usa JUnit 5 y Mockito para tests unitarios. `@SpringBootTest` para integración.

### Regla 4 — Git Flow

- Git Flow ya está inicializado en el repositorio (`git flow init` ejecutado).
- Cada tarea del `plan.md` se implementa en su propia **feature branch**.
- Nomenclatura de branches: `feature/fase-{N}-{descripcion-corta}`
  Ejemplos: `feature/fase-0-docker`, `feature/fase-1-entidades`, `feature/fase-3-tipo-cambio`
- Al terminar la tarea, realiza el commit en la feature branch con un mensaje descriptivo.
- **No hagas merge a develop** sin la aprobación explícita del desarrollador.
- Formato del mensaje de commit:
  ```
  feat(fase-N): descripción corta de lo implementado

  - Detalle 1
  - Detalle 2
  ```

### Regla 5 — Privacidad y Seguridad

- Nunca escribas credenciales, API keys ni contraseñas directamente en el código fuente.
- Toda configuración sensible va en `application.properties` usando variables con valores
  de ejemplo, o en variables de entorno.
- El archivo `application.properties` con valores reales **no debe commitearse**.
  Usa `application.properties.example` como plantilla commiteada.
- No expongas stack traces en las respuestas REST al cliente — solo mensajes controlados.
- Los mensajes de error deben estar en español y ser comprensibles para un usuario no técnico.

---

## 3. Formato de Reporte al Terminar Cada Tarea

Al completar cada tarea, genera un reporte con exactamente esta estructura:

```
## ✅ Tarea completada: [nombre de la tarea]
**Fase:** [número y nombre de la fase]
**Branch:** [nombre de la feature branch usada]

### Qué se implementó
- [elemento 1]
- [elemento 2]

### Archivos creados o modificados
- `ruta/archivo.java` — [descripción breve]
- `ruta/archivo.java` — [descripción breve]

### Tests incluidos
- `ruta/TestClase.java` — [qué cubre]

### Comando para verificar
[comando bash para verificar que la tarea funciona correctamente]

### Criterio de aceptación
[copiar textualmente el criterio de aceptación del plan.md para esta tarea]

---
⏸️ **Esperando confirmación para continuar con la siguiente tarea.**
```

---

## 4. Comandos Útiles del Proyecto

### Docker — Base de datos

```bash
# Levantar el contenedor de PostgreSQL
docker-compose up -d

# Verificar que el contenedor está corriendo
docker ps | grep libreria_postgres

# Ver logs del contenedor
docker logs libreria_postgres

# Detener el contenedor
docker-compose down

# Detener y eliminar el volumen (¡borra todos los datos!)
docker-compose down -v

# Conectarse a la base de datos desde terminal
docker exec -it libreria_postgres psql -U libreria_user -d libreria_db
```

### Maven — Build y Tests

```bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar todos los tests
./mvnw test

# Ejecutar la aplicación
./mvnw spring-boot:run

# Empaquetar el proyecto
./mvnw clean package -DskipTests
```

### Git Flow

```bash
# Iniciar una nueva feature (una por tarea)
git flow feature start fase-{N}-{descripcion-corta}

# Finalizar una feature (solo con aprobación del desarrollador)
git flow feature finish fase-{N}-{descripcion-corta}

# Ver el estado actual de branches
git branch -a

# Ver el log resumido
git log --oneline --graph --all
```

---

## 5. Pendientes Externos (No Bloquean el Desarrollo)

Estos puntos están documentados en `spec/CLAUDE.md` y afectan principalmente la Fase 6:

- Estructura de la tabla de ingresos del sistema externo del cliente (BD PostgreSQL).
- API Key definitiva para el servicio de tipo de cambio (ExchangeRate-API).
- Lista completa de países y monedas que el cliente usa actualmente.

> Si una tarea depende de alguno de estos pendientes, indícalo en el reporte
> y espera instrucciones antes de continuar.
