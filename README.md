# MovieMatch

Aplicacion Java para recomendacion de peliculas usando MySQL y JDBC.

## Requisitos

- Java 17 o superior
- NetBeans (recomendado para este proyecto)
- MySQL Server 8+

## Configuracion de base de datos

1. Ejecuta el script:
   - `sql/moviematch_schema.sql`
2. Verifica que exista la base `moviematch`.
3. Revisa las credenciales en `src/main/java/moviematch/util/ConexionBD.java`.

Credenciales actuales:
- Host: `localhost`
- Puerto: `3306`
- Base: `moviematch`
- Usuario: `root`
- Contrasena: `root#$#212`

Usuario de prueba para login:
- Usuario: `demo`
- Contrasena: `demo123`

## Como abrir y ejecutar en NetBeans

1. Abre NetBeans.
2. Ve a **File > Open Project**.
3. Selecciona la carpeta `MovieMatch` (donde esta `pom.xml`).
4. Ejecuta la clase principal:
   - `moviematch.vista.VentanaLogin`

## Estructura del proyecto

`src/main/java/moviematch/`

- `modelo/` -> Clases de datos (Usuario, Pelicula, Genero, etc.)
- `dao/` -> Acceso a datos (CRUD con JDBC y PreparedStatement)
- `servicio/` -> Logica de negocio (ServicioRecomendacion)
- `util/` -> Utilidades (ConexionBD)
- `vista/` -> Interfaz grafica en Swing

`sql/`
- `moviematch_schema.sql` -> Script de creacion de tablas y datos de ejemplo

## Funcionalidades principales

- Login de usuario
- CRUD basico para usuarios, peliculas y calificaciones (via DAO)
- Gestion de preferencias por genero
- Lista personal (FAVORITE / WATCH_LATER)
- Recomendaciones de peliculas por:
  - generos preferidos
  - popularidad
  - promedio de calificaciones
  - exclusiones (ya calificadas o ya en lista)

## Nota

Este proyecto esta optimizado para trabajar en NetBeans.
