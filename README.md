# Andanza — Backend

API REST del e-commerce de calzado Andanza. Proyecto de Sena.

Repo poly-repo: este es el backend. El frontend (React) vive aparte, en [andanza-frontend](https://github.com/Kevin-richarzon-jimenez/andanza-frontend) — se comunican solo por API, no comparten código ni repo.

## Stack

- **Java 21** (LTS)
- **Spring Boot 4.1.1** + Maven (con Maven Wrapper, no hace falta tener Maven instalado)
- **Spring Data JPA** + driver de **PostgreSQL** — la base de datos es Postgres vía [Supabase](https://supabase.com), consumida directo por JDBC/JPA (sin usar la API/Auth propia de Supabase)
- **Spring Boot Validation** — validación de datos de entrada
- **Spring Boot DevTools** — reinicio automático al guardar cambios
- **Spring Boot Actuator** — expone `/actuator/health` para verificar que la app (y su conexión a la base de datos) está funcionando
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

**Alternativa para no repetir el comando largo cada vez:** copiá `src/main/resources/application-local.properties.example` como `application-local.properties` (en la misma carpeta) y completalo con tus credenciales reales — ese archivo está en `.gitignore`, nunca se comitea. Después corré:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

y usa automáticamente esos valores en vez de los del comando de variables de entorno.

**Conectando a Supabase:** sacá las credenciales del **Session Pooler**, no de la conexión directa — en el dashboard del proyecto, botón "Connect" → "Direct connection" → pestaña "Session pooler" (la conexión directa requiere IPv6, que la mayoría de redes no tiene; el detalle completo, con ejemplo, está en `application-local.properties.example`).

La app levanta en `http://localhost:8080` (configurable con la variable `PORT`). Para confirmar que la conexión a la base de datos funciona, abrí `http://localhost:8080/actuator/health` — debería responder `{"status":"UP", ...}` con el detalle de la base de datos incluido.

## Compilar

```
./mvnw clean package
```
Genera un `.jar` ejecutable en `target/`.

## Despliegue

Todavía no está desplegado (pendiente elegir host — algo con soporte para Java, ej. Render o Railway; Vercel no sirve para esto, es para el frontend). Cuando se despliegue, probar la conexión a Supabase por **conexión directa** primero, no por Session Pooler — es la opción recomendada por Supabase para apps persistentes, y a diferencia del desarrollo local, es probable que el host en la nube sí tenga soporte IPv6. Si falla, recién ahí usar Session Pooler como en local.

## Estructura

```
src/main/java/com/andanza/backend/
├── AndanzaBackendApplication.java   punto de entrada
└── ...                              controllers/, services/, repositories/, models/ se agregan a medida que se construye la API
```
