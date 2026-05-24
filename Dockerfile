FROM node:18-alpine AS frontend-build
WORKDIR /app/web
COPY web/package.json web/package-lock.json* ./
RUN npm install
COPY web/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17-alpine AS backend-build
WORKDIR /app
COPY api/pom.xml api/
COPY api/src/ api/src/
COPY --from=frontend-build /app/web/dist /app/api/src/main/resources/static
WORKDIR /app/api
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
