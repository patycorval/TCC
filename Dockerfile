# Docker é uma vm

FROM eclipse-temurin:17-jdk-alpine
# quer apline só com java

# cria uma pasta app dentro da maquina virtual
WORKDIR /app 

# o pom que faz o java compilar
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY src ./src

RUN chmod 777 mvnw

# vai gerar o war
RUN ./mvnw package

RUN ls -l ./target

CMD ["java", "-jar", "target/sitebd-0.0.1-SNAPSHOT.war"]
