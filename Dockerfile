FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY LidiaMartWeb.java .
COPY lib/mysql-connector-j-26.7.0.jar ./mysql-connector.jar
COPY images ./images

RUN javac -cp mysql-connector.jar LidiaMartWeb.java

EXPOSE 8080

CMD ["java", "-cp", ".:mysql-connector.jar", "LidiaMartWeb"]