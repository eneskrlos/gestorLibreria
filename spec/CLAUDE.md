# CLAUDE.md — Contexto del Proyecto: Sistema de Gestión de Librería

## Descripción General

Sistema web desarrollado en Java (Spring Boot) para el propietario de una librería en Montevideo, Uruguay.
El cliente importa libros desde el exterior y necesita gestionar los egresos (gastos e impuestos de importación)
y consultar reportes de ingresos provenientes de un sistema externo ya existente.

> ⚠️ Este sistema NO reemplaza al sistema existente del cliente. Convive con él compartiendo la misma base de datos PostgreSQL.

---

## Alcance — 3 Funcionalidades Únicas

| # | Funcionalidad | Tipo |
|---|---|---|
| 1 | Reporte de Ingresos | Solo lectura — datos del sistema externo |
| 2 | Registrar Egresos | Alta de gastos e impuestos por país |
| 3 | Reporte de Egresos | Consulta filtrada por fecha y/o país |

---

## Stack Tecnológico

- **Lenguaje:** Java 17
- **Framework:** Spring Boot 3.x
- **Base de datos:** PostgreSQL 15 (contenedor Docker, puerto 5434)
- **ORM:** Spring Data JPA / Hibernate (Code-First con ddl-auto=update)
- **Migraciones:** Flyway (para datos iniciales de países y tipos de cambio)
- **API tipo de cambio:** ExchangeRate-API (opción B — automática) con fallback manual
- **Build:** Maven
- **Contenedor DB:** Docker Compose

---

## Reglas de Negocio Críticas

### Ingresos
- El sistema es **solo lectura** respecto a los ingresos.
- Los ingresos fueron registrados por el sistema externo del cliente.
- La estructura exacta de la tabla de ingresos del cliente está **pendiente de recibir**.
- Por ahora se modela una entidad `Ingreso` con los campos definidos en la reunión.
- Campos esperados: fecha, número de factura, importe del libro, gasto de envío, total, país.

### Egresos
- El usuario ingresa: fecha, concepto, importe en moneda local, país.
- Al guardar, el sistema consulta automáticamente el tipo de cambio via API externa.
- El importe en USD se calcula y se persiste en el momento del registro.
- El tipo de cambio usado se guarda junto al egreso (histórico inmutable).
- Si la API falla, se usa el último tipo de cambio disponible en la tabla `tipos_cambio`.

### Reportes
- Filtros: fecha inicio + fecha fin (obligatorio) y país (opcional).
- Resultados agrupados por país.
- Reporte de egresos muestra: fecha, concepto, importe moneda local, importe USD.
- Reporte de ingresos muestra: fecha, número de factura, importe libro, gasto envío, total.

### Licencias
- El sistema requiere una licencia activa para funcionar.
- Tipos: MENSUAL (10.000 UYU), TRIMESTRAL (27.000 UYU), SEMESTRAL (50.000 UYU).
- Al iniciar, el sistema valida si existe una licencia vigente. Si no, bloquea el acceso.

---

## Estructura de Paquetes (Spring Boot)

```
com.libreria
├── config/          # Configuración Spring, CORS, seguridad básica
├── controller/      # REST Controllers
├── dto/             # DTOs de request y response
├── entity/          # Entidades JPA
├── repository/      # Spring Data Repositories
├── service/         # Lógica de negocio
│   └── exchange/    # Servicio de tipo de cambio (API + fallback)
└── exception/       # Manejo global de errores
```

---

## Variables de Entorno (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5434/libreria_db
spring.datasource.username=libreria_user
spring.datasource.password=libreria_pass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# API Tipo de cambio
exchange.api.url=https://v6.exchangerate-api.com/v6/{API_KEY}/latest/USD
exchange.api.key=TU_API_KEY_AQUI

# Licencia
licencia.validacion.activa=true
```

---

## Consideraciones de UX

- El usuario final es el propietario de la librería, mayor de 60 años.
- La interfaz debe ser **simple, clara y con textos grandes**.
- Evitar tecnicismos en los mensajes al usuario.
- Los mensajes de error deben ser comprensibles para un usuario no técnico.

---

## Pendientes Externos (No bloquean el desarrollo inicial)

- [ ] Estructura de la BD del sistema externo del cliente (tabla de ingresos).
- [ ] API Key definitiva para el servicio de tipo de cambio.
- [ ] Lista completa de países y monedas que usa el cliente actualmente.
- [ ] Confirmación del nombre exacto de las tablas del sistema externo.
