# Andanza — Backend

API REST del e-commerce de calzado Andanza. Proyecto de Sena.

Repo poly-repo: este es el backend. El frontend (React) vive aparte, en [andanza-frontend](https://github.com/Kevin-richarzon-jimenez/andanza-frontend) — se comunican solo por API, no comparten código ni repo.

## Stack

- **Java 21** (LTS)
- **Spring Boot 4.1.1** + Maven (con Maven Wrapper, no hace falta tener Maven instalado)
- **Spring Data JPA** + driver de **PostgreSQL** — la base de datos es Postgres vía [Supabase](https://supabase.com), consumida directo por JDBC/JPA (sin usar la API/Auth propia de Supabase)
- **Spring Boot Validation** — validación de datos de entrada
- **Spring Boot DevTools** — reinicio automático al guardar cambios
- **Lombok** — reduce el boilerplate de getters/setters/constructores

## Requisitos

- Java 21
- Una base de datos Postgres corriendo (local, o las credenciales de Supabase del proyecto)

## Correr en desarrollo

Necesitás pasarle la conexión a la base de datos por variables de entorno (nunca hardcodeadas en el código):

```
DB_URL=jdbc:postgresql://localhost:5432/andanza DB_USERNAME=postgres DB_PASSWORD=postgres ./mvnw spring-boot:run
```

Si no pasás nada, por defecto intenta conectarse a `localhost:5432/andanza` con usuario/contraseña `postgres`/`postgres` — sirve para tener Postgres corriendo local (por ejemplo con Docker), o reemplazá esas variables con las credenciales reales de Supabase cuando las tengan.

La app levanta en `http://localhost:8080` (configurable con la variable `PORT`).

## Compilar

```
./mvnw clean package
```
Genera un `.jar` ejecutable en `target/`.

## Estructura

```
src/main/java/com/andanza/backend/
├── AndanzaBackendApplication.java   punto de entrada
└── ...                              controllers/, services/, repositories/, models/ se agregan a medida que se construye la API
```
