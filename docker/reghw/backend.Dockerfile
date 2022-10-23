#Stage-1
FROM eclipse-temurin:18 as build
# kijelőli a bázis könvtárat (a path-ok inen realatívak legyenek.
WORKDIR /app
# Maven könyvtár másolása
COPY .mvn/ ./.mvn
# A warapper (linuxos!) és a pom másolása
COPY mvnw pom.xml ./
# az mvnw script sorvégeit unix formátumra kell alakítani
RUN sed -i 's/\r$//' ./mvnw
# Naven függőségek beszerzése
RUN ./mvnw dependency:go-offline
# források másolása
COPY src ./src
# az sql ip módosítása
RUN sed -i 's/localhost:3306/172.99.0.99:3306/' ./src/main/resources/application.properties
RUN cat ./src/main/resources/application.properties | grep jdbc.mysql
# jar előállítása
RUN ./mvnw package -DskipTests

#Stzage-2
FROM eclipse-temurin:18-alpine
# A jar végleges helyére másolása
WORKDIR /app
COPY --from=build /app/target/TimeSeries-0.0.1-SNAPSHOT.jar ./TimeSeries-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "./TimeSeries-0.0.1-SNAPSHOT.jar"]
