# spec.md — Especificación Técnica del Sistema

## 1. Contexto

Sistema de gestión para librería en Montevideo, Uruguay. El propietario importa libros
desde distintos países y necesita registrar los egresos (gastos e impuestos de importación)
y consultar reportes de ingresos que provienen de un sistema externo ya existente.

---

## 2. Entidades del Dominio (Code-First JPA)

### 2.1 `Pais`

Representa cada país desde el cual se importan libros.

```java
@Entity
@Table(name = "paises")
public class Pais {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;              // Ej: "Argentina", "Brasil", "España"

    @Column(name = "moneda_codigo", nullable = false, length = 10)
    private String monedaCodigo;        // Ej: "ARS", "BRL", "EUR"

    @Column(name = "moneda_nombre", nullable = false)
    private String monedaNombre;        // Ej: "Peso argentino", "Real", "Euro"
}
```

**Datos iniciales (Flyway):**

| Nombre | Código Moneda | Nombre Moneda |
|---|---|---|
| Argentina | ARS | Peso argentino |
| Brasil | BRL | Real brasileño |
| España | EUR | Euro |
| México | MXN | Peso mexicano |
| Colombia | COP | Peso colombiano |

---

### 2.2 `TipoCambio`

Registra el tipo de cambio usado al momento de registrar un egreso.
**Nunca se modifica retroactivamente** — es un historial inmutable.

```java
@Entity
@Table(name = "tipos_cambio")
public class TipoCambio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id", nullable = false)
    private Pais pais;

    @Column(name = "valor_usd", nullable = false, precision = 10, scale = 6)
    private BigDecimal valorUsd;        // 1 unidad moneda local = X USD
                                        // Ej: 1 ARS = 0.001100 USD

    @Column(name = "fecha_consulta", nullable = false)
    private LocalDateTime fechaConsulta;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private FuenteTipoCambio fuente;    // API o MANUAL
}

public enum FuenteTipoCambio {
    API,      // Obtenido automáticamente de la API externa
    MANUAL    // Ingresado manualmente como fallback
}
```

---

### 2.3 `Egreso`

Gasto o impuesto aplicado por un país al exportar libros hacia Uruguay.

```java
@Entity
@Table(name = "egresos")
public class Egreso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private String concepto;            // Ej: "Impuesto de exportación", "Arancel aduanero"

    @Column(name = "importe_moneda_local", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeMonedaLocal;  // Importe en la moneda del país

    @Column(name = "importe_usd", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeUsd;      // Calculado y guardado al momento del registro

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id", nullable = false)
    private Pais pais;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_cambio_id", nullable = false)
    private TipoCambio tipoCambio;      // Snapshot del tipo de cambio usado

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;    // Timestamp del momento del alta
}
```

**Fórmula de conversión:**
```
importeUsd = importeMonedaLocal × tipoCambio.valorUsd
```

---

### 2.4 `Ingreso` *(Solo lectura — sistema externo)*

Representa los registros de compras de libros del sistema externo.
Esta entidad **no tendrá operaciones de escritura** desde este sistema.

> ⚠️ Los nombres de tabla y columnas están pendientes de confirmar con la BD del cliente.
> Se usará `@Table(name = ...)` y `@Column(name = ...)` para mapear sin modificar la estructura existente.

```java
@Entity
@Table(name = "ingresos")   // ⚠️ Nombre a confirmar con cliente
@Immutable                  // Hibernate: solo lectura
public class Ingreso {
    @Id
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "numero_factura", nullable = false)
    private String numeroFactura;

    @Column(name = "importe_libro", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeLibro;

    @Column(name = "gasto_envio", nullable = false, precision = 12, scale = 2)
    private BigDecimal gastoEnvio;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;           // importeLibro + gastoEnvio

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id")       // ⚠️ Nombre a confirmar
    private Pais pais;
}
```

---

### 2.5 `Licencia`

Controla el acceso al sistema mediante una clave con fecha de vencimiento.

```java
@Entity
@Table(name = "licencias")
public class Licencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String clave;               // UUID o código alfanumérico generado

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoLicencia tipo;

    @Column(nullable = false)
    private Boolean activa;
}

public enum TipoLicencia {
    MENSUAL,        // 1 mes   — 10.000 UYU
    TRIMESTRAL,     // 3 meses — 27.000 UYU
    SEMESTRAL       // 6 meses — 50.000 UYU
}
```

---

## 3. DTOs

### 3.1 Request — Registrar Egreso

```java
public class EgresoRequestDto {
    @NotNull
    private LocalDate fecha;

    @NotBlank
    private String concepto;

    @NotNull @Positive
    private BigDecimal importeMonedaLocal;

    @NotNull
    private Long paisId;
}
```

### 3.2 Response — Egreso

```java
public class EgresoResponseDto {
    private Long id;
    private LocalDate fecha;
    private String concepto;
    private BigDecimal importeMonedaLocal;
    private String monedaCodigo;        // Ej: "BRL"
    private BigDecimal importeUsd;
    private String paisNombre;
    private LocalDateTime fechaRegistro;
}
```

### 3.3 Request — Filtros de Reportes

```java
public class ReporteFilterDto {
    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    private Long paisId;                // Opcional — null = todos los países
}
```

### 3.4 Response — Reporte de Egresos

```java
public class ReporteEgresosDto {
    private String paisNombre;
    private String monedaCodigo;
    private List<EgresoResponseDto> egresos;
    private BigDecimal totalMonedaLocal;
    private BigDecimal totalUsd;
}
```

### 3.5 Response — Reporte de Ingresos

```java
public class ReporteIngresosDto {
    private String paisNombre;
    private String monedaCodigo;
    private List<IngresoResponseDto> ingresos;
    private BigDecimal totalGeneral;
}

public class IngresoResponseDto {
    private LocalDate fecha;
    private String numeroFactura;
    private BigDecimal importeLibro;
    private BigDecimal gastoEnvio;
    private BigDecimal total;
}
```

---

## 4. Endpoints REST

### Países

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/paises` | Listar todos los países |

### Egresos

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/egresos` | Registrar un nuevo egreso |
| GET | `/api/egresos/reporte` | Reporte de egresos con filtros |

### Ingresos

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/ingresos/reporte` | Reporte de ingresos con filtros (solo lectura) |

### Licencia

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/licencia/estado` | Verificar si la licencia está vigente |
| POST | `/api/licencia/activar` | Activar una nueva licencia con clave |

---

## 5. Lógica de Conversión de Moneda

### Flujo al registrar un Egreso

```
1. Usuario envía EgresoRequestDto (con paisId e importeMonedaLocal)
2. Sistema consulta el país → obtiene monedaCodigo
3. Sistema llama a la API externa de tipo de cambio
   → Si éxito: guarda TipoCambio con fuente=API
   → Si falla:  busca el último TipoCambio del país en BD, fuente=MANUAL (fallback)
4. Calcula: importeUsd = importeMonedaLocal × tipoCambio.valorUsd
5. Persiste Egreso con importeUsd y referencia al TipoCambio usado
6. Retorna EgresoResponseDto
```

### API Externa

- **Servicio:** ExchangeRate-API (`https://v6.exchangerate-api.com`)
- **Endpoint:** `GET /v6/{API_KEY}/latest/USD`
- **Respuesta clave:** `conversion_rates.{MONEDA_CODIGO}`
- **Cálculo:** El API retorna cuántas unidades de moneda local equivalen a 1 USD.
  Para obtener cuántos USD equivale 1 unidad local: `valorUsd = 1 / conversion_rates.ARS`

---

## 6. Validación de Licencia

- Al inicio de cada request, un interceptor valida que exista una `Licencia` activa y vigente.
- Si `fechaVencimiento < LocalDate.now()` → la licencia se marca como `activa = false`.
- Si no hay licencia vigente → responde `HTTP 403` con mensaje claro para el usuario.
- La validación se omite para el endpoint `/api/licencia/activar`.

---

## 7. Diagrama de Base de Datos

```
paises
├── id (PK)
├── nombre
├── moneda_codigo
└── moneda_nombre

tipos_cambio
├── id (PK)
├── pais_id (FK → paises)
├── valor_usd
├── fecha_consulta
└── fuente (API | MANUAL)

egresos
├── id (PK)
├── fecha
├── concepto
├── importe_moneda_local
├── importe_usd
├── pais_id (FK → paises)
├── tipo_cambio_id (FK → tipos_cambio)
└── fecha_registro

ingresos  ← tabla del sistema externo (solo lectura)
├── id (PK)
├── fecha
├── numero_factura
├── importe_libro
├── gasto_envio
├── total
└── pais_id (FK → paises)

licencias
├── id (PK)
├── clave
├── fecha_inicio
├── fecha_vencimiento
├── tipo (MENSUAL | TRIMESTRAL | SEMESTRAL)
└── activa
```
