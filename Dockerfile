FROM node:latest as builder

RUN apt-get -q update && apt-get -y -q install  openjdk-8-jdk

WORKDIR /fhirextinguisher

COPY . /fhirextinguisher

RUN  cd frontend && npm install &&  npm install --only=dev && npm run antlr4ts && cd .. && ./gradlew shadowJar

FROM openjdk:8-jre

WORKDIR /fhirextinguisher

COPY --from=builder /fhirextinguisher/build/libs/FhirExtinguisher-1.5.3-all.jar /fhirextinguisher/FhirExtinguisher-1.5.3-all.jar

ENTRYPOINT ["java", "-jar", "FhirExtinguisher-1.5.3-all.jar"]

#CMD ["java -jar build/libs/FhirExtinguisher-1.5.1-all.jar -f http://hapi.fhir.org/baseR4 -p 8080"]

