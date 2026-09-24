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

# Pensado para instancias de 512 MB: heap acotado, un solo recolector y pilas pequeñas.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -Xss512k -XX:MaxMetaspaceSize=128m -XX:ReservedCodeCacheSize=64m"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
