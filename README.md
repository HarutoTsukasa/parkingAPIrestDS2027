# parkingAPIrestDS — API REST para parqueadero (MVP)

Prototipo en Java + Spring Boot + MySQL para ingreso, salida y disponibilidad de plazas, con tarifas por fracción de 15 minutos y por tipo de vehículo (carro, moto, bicicleta), en pesos colombianos (COP). Pensado para ejecutarse desde **Spring Tools Suite (STS)** o línea de comandos.

---

## 1. Requisitos

| Componente | Versión mínima | Notas |
|---|---|---|
| Spring Tools Suite | 4.20+ o 5.x | Eclipse-based, incluye Spring Boot Dashboard |
| JDK | 17+ | `pom.xml` declara `<java.version>17</java.version>` |
| Maven | 3.10+ | Incluye `mvnw` (wrapper) |
| MySQL | 8.0+ | Servidor local en `localhost:3306` |

---

## 2. Importar en STS

1. **File > Import...** > **Maven > Existing Maven Projects** > *Next*.
2. *Root Directory*: carpeta raíz donde está `pom.xml`.
3. Marca `parkingAPIrestDS` > *Finish*.
4. Si hay error de JDK: **Properties > Java Build Path > Libraries** > verifica el *JRE System Library* sea JDK 17+.

Atajo: **Spring Boot Dashboard** permite arrancar con doble clic una vez importado.

---

## 3. Preparar MySQL

### 3.1 Base de datos

`createDatabaseIfNotExist=true` en la URL JDBC crea `parquing_ds_db_2027` en el primer arranque. Valores por defecto:

- Host: `localhost:3306`
- Base: `parquing_ds_db_2027`
- Usuario: `root`
- Contraseña: `root`
- Zona horaria: `America/Bogota`

Si usas otras credenciales, edita `src/main/resources/application.properties` o pásalas como variables de entorno (sección 4).

### 3.2 Siembra inicial de plazas

`PlazaInitializer` crea automáticamente las plazas al arrancar **si la tabla `plaza` está vacía**. Defaults:

- 25 plazas de CARRO (números 1–25)
- 20 plazas de MOTO (26–45)
- 5 plazas de BICICLETA (46–50)

Total: 50 plazas. Configurable en `application.properties` con `parking.espacios.iniciales.*`.

**No necesitas ejecutar SQL manual** para la siembra inicial. Solo necesitas la base creada.

### 3.3 Migración desde una versión anterior (sin `tipo`)

Si ya tenías tablas creadas sin la columna `tipo`, Hibernate no puede añadir `tipo NOT NULL` sobre filas existentes. Ejecuta:

```sql
USE parquing_ds_db_2027;
TRUNCATE TABLE vehiculo;
TRUNCATE TABLE plaza;
```

Y reinicia la aplicación. `PlazaInitializer` repobla las 50 plazas.

---

## 4. Ejecutar desde STS

### Opción A — Spring Boot Dashboard

1. Abre **Spring Boot Dashboard** (`Window > Show View > Other... > Spring > Spring Boot`).
2. Pulsa ▶ sobre `parkingAPIrestDS`.
3. Consola: `Started ParkingApIrestDsApplication in X.XXX seconds`.
4. API en `http://localhost:8080`.

### Opción B — Run As

Clic derecho en `ParkingApIrestDsApplication.java` > **Run As > Spring Boot App**.

### Opción C — Terminal

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### Variables de entorno opcionales

```bash
DB_HOST=localhost DB_PORT=3306 DB_NAME=parquing_ds_db_2027 \
DB_USER=root DB_PASSWORD=root SERVER_PORT=8080 \
./mvnw spring-boot:run
```

Las tarifas y los espacios iniciales también se pueden sobrescribir por entorno (`PARKING_TARIFA_TIPOS_CARRO_HORA`, etc.), pero por claridad edítalos directamente en `application.properties`.

---

## 5. Tarifas y cálculo

### Configuración

| Propiedad | Valor | Descripción |
|---|---|---|
| `parking.tarifa.fraccion-minutos` | `15` | Duración de una fracción (minutos) |
| `parking.tarifa.tipos.carro.hora` | `6000` | Tarifa/hora carro (informativa) |
| `parking.tarifa.tipos.carro.fraccion` | `1500` | Tarifa/fracción carro (autoritativa) |
| `parking.tarifa.tipos.moto.hora` | `3000` | Tarifa/hora moto |
| `parking.tarifa.tipos.moto.fraccion` | `750` | Tarifa/fracción moto |
| `parking.tarifa.tipos.bicicleta.hora` | `1000` | Tarifa/hora bicicleta |
| `parking.tarifa.tipos.bicicleta.fraccion` | `250` | Tarifa/fracción bicicleta |

### Fórmula

```
segundos_permanencia = fechaSalida - fechaIngreso
segundos_por_fraccion = fraccion_minutos × 60
fracciones = ceil(segundos_permanencia / segundos_por_fraccion)
total_cop = fracciones × tarifa_fraccion(tipo)
```

- Se calcula sobre **segundos**, no sobre minutos truncados.
- Redondeo monetario `HALF_UP` a peso entero (una sola vez al final).
- Si la permanencia es 0 segundos, se cobran 0 fracciones. Cualquier permanencia ≥ 1 segundo cobra al menos 1 fracción.

### Tabla de verificación

| Permanencia | seg | fracciones | total carro | total moto | total bici |
|---|---|---|---|---|---|
| 5 s | 5 | 1 | 1500 | 750 | 250 |
| 14:59 | 899 | 1 | 1500 | 750 | 250 |
| 15:00 | 900 | 1 | 1500 | 750 | 250 |
| 15:01 | 901 | **2** | **3000** | **1500** | **500** |
| 30:00 | 1800 | 2 | 3000 | 1500 | 500 |
| 1 h | 3600 | 4 | 6000 | 3000 | 1000 |
| 1 h 00:01 | 3601 | **5** | **7500** | **3750** | **1250** |
| 2 h 15:00 | 8100 | 9 | 13500 | 6750 | 2250 |

Si en 15:01 con un carro obtienes `1500` en vez de `3000`, tu cálculo está truncando segundos.

---

## 6. Endpoints

| Método | Ruta | Cuerpo | Éxito | Errores |
|---|---|---|---|---|
| `POST` | `/api/vehiculos/ingreso` | `{"placa":"ABC123","tipo":"CARRO"}` | `201 Created` | `400` placa/tipo inválido, `409` placa duplicada, `409` sin plazas del tipo |
| `POST` | `/api/vehiculos/salida/{placa}` | — | `200 OK` | `404` vehículo no registrado |
| `GET` | `/api/plazas/disponibilidad` | — | `200 OK` | — |

**`tipo`** acepta: `CARRO`, `MOTO`, `BICICLETA` (mayúsculas exactas).

---

## 7. Ejemplos de uso

### 7.1 Ingreso de carro

```bash
curl -X POST http://localhost:8080/api/vehiculos/ingreso \
  -H "Content-Type: application/json" \
  -d '{"placa":"ABC123","tipo":"CARRO"}'
```

Respuesta `201 Created`:

```json
{
  "placa": "ABC123",
  "tipo": "CARRO",
  "plazaNumero": 1,
  "fechaIngreso": "2026-11-11T10:15:30",
  "fechaSalida": null,
  "fraccionesCobradas": 0,
  "tarifaHoraCop": 6000,
  "tarifaFraccionCop": 1500,
  "totalCop": 0
}
```

### 7.2 Ingreso de moto

```bash
curl -X POST http://localhost:8080/api/vehiculos/ingreso \
  -H "Content-Type: application/json" \
  -d '{"placa":"XYZ12D","tipo":"MOTO"}'
```

Respuesta asigna una plaza en el rango 26–45.

### 7.3 Disponibilidad

```bash
curl http://localhost:8080/api/plazas/disponibilidad
```

Respuesta `200 OK`:

```json
{
  "totalPlazas": 50,
  "libres": 48,
  "ocupadas": 2,
  "libresPorTipo": {"CARRO": 24, "MOTO": 19, "BICICLETA": 5},
  "ocupadasPorTipo": {"CARRO": 1, "MOTO": 1, "BICICLETA": 0},
  "plazasLibres": [
    {"numero": 2, "tipo": "CARRO"},
    {"numero": 3, "tipo": "CARRO"}
  ],
  "plazasOcupadas": [
    {"numero": 1, "tipo": "CARRO"},
    {"numero": 26, "tipo": "MOTO"}
  ]
}
```

### 7.4 Salida y facturación

Permanencia ejemplo: 1 h 5 min 30 s (3930 s = 5 fracciones de carro → 5 × 1500 = 7500 COP).

```bash
curl -X POST http://localhost:8080/api/vehiculos/salida/ABC123
```

Respuesta `200 OK`:

```json
{
  "placa": "ABC123",
  "tipo": "CARRO",
  "plazaNumero": 1,
  "fechaIngreso": "2026-11-11T10:15:00",
  "fechaSalida": "2026-11-11T11:20:30",
  "fraccionesCobradas": 5,
  "tarifaHoraCop": 6000,
  "tarifaFraccionCop": 1500,
  "totalCop": 7500
}
```

### 7.5 Casos de error

| Caso | Petición | Respuesta |
|---|---|---|
| Placa duplicada | `POST /api/vehiculos/ingreso` con placa ya ingresada | `409 Conflict` |
| Placa con formato inválido | `POST /api/vehiculos/ingreso` con `"abc"` | `400 Bad Request` |
| `tipo` ausente o inválido | `{"placa":"ABC123"}` o `{"tipo":"TRACTOMULA"}` | `400 Bad Request` |
| Sin plazas del tipo pedido | `POST /api/vehiculos/ingreso` con todas las plazas del tipo ocupadas | `409 Conflict` |
| Salida de placa inexistente | `POST /api/vehiculos/salida/ZZZ999` | `404 Not Found` |

---

## 8. Estructura

```
parkingAPIrestDS/
├── mvnw / mvnw.cmd
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/htx/app/apirest/parking/
    │   │   ├── ParkingApIrestDsApplication.java
    │   │   ├── config/
    │   │   │   ├── TarifasProperties.java
    │   │   │   ├── EspaciosInicialesProperties.java
    │   │   │   └── PlazaInitializer.java
    │   │   ├── controller/
    │   │   │   ├── PlazaController.java
    │   │   │   └── VehiculoController.java
    │   │   ├── service/
    │   │   │   ├── ParkingService.java
    │   │   │   └── ParkingServiceImpl.java
    │   │   ├── repository/
    │   │   │   ├── PlazaRepository.java
    │   │   │   └── VehiculoRepository.java
    │   │   ├── model/
    │   │   │   ├── Plaza.java
    │   │   │   ├── TipoVehiculo.java
    │   │   │   └── Vehiculo.java
    │   │   ├── dto/
    │   │   │   ├── DisponibilidadResponse.java
    │   │   │   ├── ErrorResponse.java
    │   │   │   ├── IngresoRequest.java
    │   │   │   ├── PlazaResumenResponse.java
    │   │   │   └── SalidaResponse.java
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java
    │   │       ├── PlazaOcupadaException.java
    │   │       ├── VehiculoNoRegistradoException.java
    │   │       └── VehiculoYaRegistradoException.java
    │   └── resources/
    │       └── application.properties
    └── test/java/com/htx/app/apirest/parking/
        └── ParkingApIrestDsApplicationTests.java
```

---

## 9. Concurrencia

`PlazaRepository.findLibresPorTipoWithLock` aplica `@Lock(PESSIMISTIC_WRITE)` sobre la consulta filtrada por estado y tipo. Evita que dos ingresos simultáneos lean la misma plaza como libre. No lo elimines si vas a probar con peticiones concurrentes.

---

## 10. Tests

```bash
./mvnw test
```

`contextLoads()` requiere MySQL corriendo y credenciales válidas. Si prefieres omitir:

```bash
./mvnw -DskipTests package
```

---

## 11. Empaquetar

```bash
./mvnw clean package
java -jar target/parkingAPIrestDS-0.0.1-SNAPSHOT.jar
```

---

## 12. Problemas comunes

| Síntoma | Causa | Solución |
|---|---|---|
| `Unsupported class file major version` | STS sobre JDK < 17 | Configurar JDK 17+ en `Window > Preferences > Java > Installed JREs` |
| `Could not resolve dependencies` | `pom.xml` con artefactos inexistentes | Usar el `pom.xml` de esta entrega |
| `Column 'tipo' cannot be null` al arrancar | Tablas antiguas sin `tipo` | `TRUNCATE TABLE vehiculo; TRUNCATE TABLE plaza;` y reiniciar |
| `Communications link failure` | MySQL apagado | Verificar servicio y `application.properties` |
| Primer ingreso devuelve `409 No hay plazas...` | `PlazaInitializer` no corrió (tabla no vacía) o tipo sin plazas | Verificar tabla `plaza` (`SELECT tipo, COUNT(*) FROM plaza GROUP BY tipo;`) |
| `IllegalStateException: Tarifa por fracción debe ser > 0` al arrancar | Propiedad mal configurada | Revisar `application.properties` |
| `tipo` inválido da `400` | Enum inválido o case incorrecto | Enviar `CARRO`, `MOTO` o `BICICLETA` en mayúsculas |
| Lombok no genera getters en STS | Falta el plugin | Ejecutar `java -jar lombok.jar` apuntando a STS, reiniciar |

---

## 13. Notas del MVP

- Placas únicas a nivel global: un carro y una moto no pueden compartir placa. La realidad colombiana lo cumple (formatos distintos); el sistema no valida formato por tipo.
- No se modela historial de salidas: el vehículo se elimina al salir y su plaza se libera.
- No hay endpoint para agregar plazas después de la siembra inicial. Para hacerlo, `TRUNCATE TABLE plaza;` (con `vehiculo` vacío) y reiniciar.
- No se usa Spring Security (fuera del alcance).
- `ddl-auto=update`: Hibernate crea/actualiza el esquema. En producción: `validate` + Flyway/Liquibase.
- Si la tarifa cambia entre ingreso y salida, prevalece la tarifa vigente **al momento del cobro** (la del arranque actual), no la del ingreso.