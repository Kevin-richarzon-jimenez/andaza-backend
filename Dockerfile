# Etapa 1: compilar con el Maven Wrapper (no hace falta Maven instalado)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# mvnw está guardado en git sin permiso de ejecución.
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q -DskipTests package

# Etapa 2: ejecutar solo con el JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home andanza
COPY --from=build /app/target/*.jar app.jar
USER andanza

# Pensado para instancias de 512 MB y 0.1 CPU: heap acotado, un solo recolector y pilas pequeñas.
# TieredStopAtLevel=1 deja solo el compilador rápido de la JVM: con tan poca CPU, optimizar al máximo compite
# con el arranque. Con 0.1 CPU bajó el arranque de 198 s a 103 s y no se nota en las peticiones.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -Xss512k -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=64m -XX:TieredStopAtLevel=1"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
