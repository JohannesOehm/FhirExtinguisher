FROM node:stretch as builder

RUN apt-get -q update && apt-get -y -q install  openjdk-8-jdk

WORKDIR /fhirextinguisher

COPY . /fhirextinguisher

RUN  cd frontend && npm install &&  npm install --only=dev && npm run antlr4ts && cd .. && ./gradlew shadowJar

FROM openjdk:8-jre

WORKDIR /fhirextinguisher

COPY --from=builder /fhirextinguisher/build/libs/FhirExtinguisher-* /fhirextinguisher/FhirExtinguisher-all.jar

ENTRYPOINT ["java", "-jar", "FhirExtinguisher-all.jar"]

