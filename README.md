# FHIR Extinguisher

![Screenshot](img/Screenshot.png)

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
This way you can use all the FHIR REST API filtering options provided by the server. For each resource in the resulting bundle, 
a [FHIRPath](http://hl7.org/fhirpath/) expression is evaluated.

![image](img/Concept.png)

All parameters are forwarded as they are, except: 
* `__columns` Must be `name:expression,name2:expression2`. Name might be followed by options like `@join(" ")` and `@explode`, 
which control how to handle multiple returned results by the FHIRPath expression. `@join` concats the strings into a single cell with a delimiter of your choice,
while explode will create multiple rows for the element.
* `__limit` By default, the FhirExtinguisher automatically fetches the next bundle. This way you can limit the number of resources processed.
* `__csvFormat` Is not implemented yet. Change the output format.

The returned bundles are taken and evaluated against multiple FHIR path expressions using the HAPI FHIRPath engine.

Please escape `@join(", ")` as `@join("%2C ")`and `@join(":")` as `@join("%3A")`!

## Building
Requirements: Java 8, npm 6.13.x

Use `./gradlew shadowJar` to compile the project. The resulting .jar file will be in `/build/libs/`.

At the first time, to compile the frontend, please run `npm install` (and eventually `npm install --only=dev`) in the 
`/frontend` folder, since the gradle build script will only invoke webpack and copy the files into the .jar file. 

If you get `Process 'command 'cmd'' finished with non-zero exit value 2`, please execute `"node_modules/.bin/webpack"` for the 
webpack error message.

### Frontend Development
You can execute `"node_modules/.bin/webpack-dev-server"` to start an automatically updating version of the frontend. Note 
that the backend will not be executed, so there will not be actual function, but for CSS/Vue.js development this is quite nice.

## Running 
Use `java -jar FhirExtinguisher-<version>-all.jar -f http://hapi.fhir.org/baseR4 -p 8080` to start the server 
on your local machine and connects to the public FHIR R4 server. Use `-a user:passwd` if the server requires Basic Auth.

By default, FhirExtinguisher assumes, the server is R4. If you want to connect with a (D)STU3 server, please add `-v stu3` to the command line arguments.

List of all command line options:
* `-f [url]` FHIR server URL
* `-a [user]:[password]` Basic authentication credentials, if required by FHIR server
* `-p [portnumber]` Port number on local machine to open, e.g. with `-p 8080`, the GUI will be available under `http://localhost:8080/`
* `-ext` Allow connections of non-localhost machines

## Acknowledgement
Supported by BMBF grant No. 01ZZ1802V (HiGHmed/Münster) 