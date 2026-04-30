FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
    curl \
    gdal-bin \
    libgdal-dev \
 && rm -rf /var/lib/apt/lists/*

ENV LD_LIBRARY_PATH=/usr/lib
ENV GDAL_DATA=/usr/share/gdal

COPY --from=build /app/target/backend-1.0.0.jar app.jar

EXPOSE 8181

ENTRYPOINT ["java", "-Djava.library.path=/usr/lib", "-jar", "/app/app.jar"]