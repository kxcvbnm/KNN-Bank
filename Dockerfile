# build the application
FROM eclipse-temurin:21-jdk-jammy AS builder

# set working directory inside container
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -Dmaven.test.skip=true

# build a prodcution ready image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# copy final excutable jar file from builder
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8090

# run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]