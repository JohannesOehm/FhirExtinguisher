# FHIR Extinguisher

## Goal 
This project's goal is to simplify data analysis on HL7 FHIR by easing conversion 
of the hierarchical structure into a flat list for analysing R DataFrames.

There are alternative ways to do this:
* Using [Cerner Bunsen](https://github.com/cerner/bunsen), you can load FHIR resources as DataFrames in Spark and query them using Spark-SQL.
 The main advantage is that you can use the full set of SQL operations like JOIN, etc., but You have to export 
 your entire dataset first.
* Using the [FHIR GraphQL API](http://hl7.org/fhir/graphql.html), you can use operators like "@flatten" to transform it 
into a flatter format. But the result is still a JSON, and the GraphQL API must be supported by the server.  

## How does this work?
This project is a simple WebServer, that connects to a FHIR server and forwards the requests.
This way you can use all the FHIR filtering options provided by the server. For each resource in the resulting bundle, 
a FHIRPath expression is evaluated.

All parameters are forwarded as they are, except: 
* `__columns` Must be `name:expression,name2:expression2`. Name might be followed by options like `@join(" ")` and `@explode`, 
which control how to handle multiple returned results by the FHIR path expression. `@join` concats the strings into a single cell with a delimiter of your choice,
while explode will create multiple rows for the element.
* `__limit` By default, the FhirExtinguisher automatically fetches the next bundle. This way you can limit the number of resources processed.
* `__csvFormat` Is not implemented yet. Change the output format.

The returned bundles are taken and evaluated against multiple FHIR path expressions using the HAPI FHIRPath engine.

Please escape `@join(", ")` as `@join("%2C ")`and `@join(":")` as `@join("%3A")`!

## Building
use `gradlew shadowJar` to compile the project. The resulting .jar file will be in `/build/libs/`.

## Running 
Use `java -jar FhirExtinguisher-<version>-all.jar -f http://hapi.fhir.org/baseR4 -p 8080` to start the server 
on your local machine and connects to the public FHIR R4 server.

