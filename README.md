# Andanza — Backend

API REST del e-commerce de calzado Andanza. Proyecto de Sena.

Este repositorio es el backend. El frontend (React) vive aparte, en [andanza-frontend](https://github.com/Kevin-richarzon-jimenez/andanza-frontend), y ambos se comunican solo por API.

## Stack

- **Java 21** (LTS)
- **Spring Boot 4.1.1** con Maven (incluye Maven Wrapper: no hace falta instalar Maven)
- **Spring Web** — API REST
- **Spring Data JPA** + driver de **PostgreSQL** — la base de datos es Postgres, alojada en [Supabase](https://supabase.com) y consumida directamente por JDBC/JPA
- **Spring Boot Validation** — validación de los datos de entrada
- **Spring Boot Actuator** — health check en `/actuator/health`
- **springdoc-openapi** — documentación interactiva de la API (Swagger UI)
- **Lombok** y **DevTools**

## Requisitos

- Java 21
- La base de datos PostgreSQL del proyecto en Supabase

## Configuración

La conexión se define con `SUPABASE_DB_URL`, `SUPABASE_DB_USERNAME` y `SUPABASE_DB_PASSWORD`. No hay fallback a PostgreSQL local.

Para conectarse a otra base, como la de Supabase, se crea un perfil local:

1. Copiar `src/main/resources/application-local.properties.example` como `src/main/resources/application-local.properties`. Este archivo está en `.gitignore`: nunca se sube al repositorio.
2. Completar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con las credenciales reales del Session Pooler.

Si el sistema ya tiene definida alguna de esas variables de entorno, esa tiene prioridad sobre el archivo.

**Supabase:** este backend usa Spring Data JPA sobre PostgreSQL. Debes usar las credenciales del *Session Pooler*, que se encuentran en el dashboard del proyecto: botón **Connect** → **Direct connection** → pestaña **Session pooler**. La conexión directa solo funciona por IPv6, que la mayoría de las redes no soporta. La API key de Supabase no reemplaza la contraseña PostgreSQL y no se usa para JPA.

## Ejecutar

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

La API queda en `http://localhost:8080` (el puerto se cambia con la variable `PORT`). Si las credenciales se pasan por variables de entorno en lugar del perfil local, se omite `-Dspring-boot.run.profiles=local`.

- Estado de la app y de la conexión a la base de datos: `http://localhost:8080/actuator/health`
- Documentación interactiva de la API (Swagger UI): `http://localhost:8080/swagger-ui.html`

## Compilar

```
./mvnw clean package
```

Genera un `.jar` ejecutable en `target/`.

## API

Todos los endpoints cuelgan de `/api/v1`. El detalle de cada uno (campos, validaciones y respuestas) está en Swagger UI.

| Recurso | Endpoints |
|---|---|
| Catálogo | `GET /catalog/products` (filtros por query params) |
| Carrito | `POST /cart/totals` |
| Autenticación | `POST /auth/login`, `POST /auth/register`, `PUT /auth/password` |
| Direcciones | `POST /account/addresses`, `PUT /account/addresses/{id}` |
| Favoritos | `POST /favorites` |
| Comentarios | `POST /comments` |
| Contacto | `POST /contact-messages`, `POST /newsletter-subscriptions` |
| Administración | `POST /admin/products`, `POST` y `DELETE /admin/categories`, `PUT /admin/inventory`, `PUT /admin/users/{id}`, `PUT /admin/comments/{id}` |

### Formato de errores

Todos los errores responden con la misma estructura. `errors` lista los campos con problemas y va vacío cuando el error no corresponde a un campo puntual.

```json
{
  "timestamp": "2026-09-22T20:01:58.970Z",
  "status": 400,
  "error": "Datos inválidos",
  "message": "Revisa los campos del formulario",
  "errors": [
    { "field": "email", "message": "El correo es obligatorio" }
  ]
}
```

| Código | Cuándo |
|---|---|
| `400` | Datos inválidos o cuerpo que no es JSON |
| `404` | La ruta no existe |
| `405` | Método HTTP no permitido en esa ruta |
| `415` | Tipo de contenido no soportado |
| `422` | Regla de negocio incumplida (por ejemplo, una talla no disponible) |
| `500` | Error inesperado |

## Estructura

El código está organizado por dominio: cada carpeta trae su controller, service y DTOs juntos.

```
src/main/java/com/andanza/backend/
├── AndanzaBackendApplication.java   punto de entrada
├── auth/, cart/, catalog/, ...      un paquete por dominio
├── admin/<dominio>/                 endpoints de administración, agrupados por dominio
├── exception/                       BusinessException, GlobalExceptionHandler, ErrorResponse
├── validation/                      validaciones personalizadas reutilizables
├── common/                          DTOs usados por más de un dominio
└── config/                          configuración de la app (OpenAPI)
```

## Convenciones

- El código (clases, variables, archivos, endpoints) va en inglés; los mensajes que ve el usuario, en español.
- Los endpoints van en kebab-case bajo `/api/v1`; los recursos, en plural y sin verbos (el verbo es el método HTTP). Los paquetes de dominio van en singular.
- Las ramas llevan prefijo por tipo (`feature/`, `fix/`, `refactor/`, `chore/`, `docs/`) y no se comitea directo a `main`: todo cambio entra por pull request.
- Los commits siguen [Conventional Commits](https://www.conventionalcommits.org/) y los pull requests se mergean con *Squash and merge*.

## Roadmap

- Persistencia en Postgres con Spring Data JPA (hoy los datos de ejemplo viven en memoria; los puntos pendientes están marcados con `TODO (BD)` en el código).
- Autenticación y autorización de los endpoints (marcado con `TODO (auth)`).
- Tests de la lógica de negocio.
- Despliegue en un host con soporte para Java (por ejemplo, Render o Railway). Con Supabase, probar primero la conexión directa si el host tiene IPv6; si no, usar Session Pooler.
