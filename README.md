# FhirExtinguisher
This project's goal is to simplify data analysis on HL7 FHIR by easing conversion 
of the hierarchical structure into a flat list for analysing R DataFrames.

## Video manual
If you're a lazy reader, you can also watch a [short 5 minute presentation](https://vimeo.com/556233461)
or a longer [German presentation](https://vimeo.com/479525872) on Vimeo!

## Alternatives
There are alternative ways to do this:
* Using [Cerner Bunsen](https://github.com/cerner/bunsen), you can load FHIR resources as DataFrames in Spark and query them using Spark-SQL.
 The main advantage is that you can use the full set of SQL operations like JOIN, etc., but You have to export 
 your entire dataset first.
* Using the [FHIR GraphQL API](http://hl7.org/fhir/graphql.html), you can use operators like "@flatten" to transform it 
into a flatter format. However, the result is still a JSON, and the GraphQL API must be supported by the server.
* [fhircrackr](https://cran.r-project.org/web/packages/fhircrackr/index.html) and FHIRton, which are XML-based implementations 
for usage with R or Python. 

## How does this work?
This project is a simple WebServer, that connects to a FHIR server and forwards the requests. This way you can use all 
the FHIR REST API filtering options provided by the server. For each resource in the resulting bundle, a
 [FHIRPath](http://hl7.org/fhirpath/) expression will be evaluated.

![image](img/Concept.png)

All parameters are forwarded as they are, except: 
* `__columns` Must be `name:expression,name2:expression2`. You may add a list processing mode after the expression:
    * `@join(" ")` concatenates the strings into a single cell with a delimiter of your choice
    * `@explodeWide($disc:expression,subcolumnname:subexpression,...)` will create new **columns** for each
      element. <br>
      Inside the subexpression, use `$this` to refer to the current element.
      `$disc` is the expression that will be evaluated for the column name using the following
      schema: `columnname.$disc.subcolumnname`. Use `%index` for the current index of the element returned by the main
      expression. <br>
      You may leave the subcolumnname empty for one expression. <br>
    * `@explodeLong(subcolumnname:subexpression,...)` will create a new **row** for each element returned.<br>
      Inside the subexpression, use `$this` to refer to the current element.<br>
      The resulting columns will be named `name.subcolumnname`. <br>
      If you omit the subcolumns, the column itself will be exploded.
      ![image](img/join-long-wide.png)
      Please escape `:` in the column name with `\:`. In the FHIRPath expression, escape `,` with `\,` and `@`
      with `\@`!
* `__limit` FhirExtinguisher **automatically fetches Bundle pages until limit is met**. This way you can limit the 
number of resources processed.
* `__csvFormat` Change the output CSV style. Supported are: `Default`, `Excel`, `InformixUnload`, `InformixUnloadCsv`, 
`MySQL`, `PostgreSQLCsv`, `PostgreSQLText`, `RFC4180`, `TDF` as provided by the Apache Commons CSV library, default is `Excel`.

The returned bundle(s) are evaluated against the FHIRPath expressions using the HAPI FHIRPath engine, and returned to the client.

## Download
You can download pre-compiled binaries in the Release-section of the [Gitlab](https://imigitlab.uni-muenster.de/published/fhirextinguisher/-/releases)!

## Running

Requirements: **Java 8**, GUI tested with **Firefox** and **Chrome**

Use `java -jar FhirExtinguisher-<version>-all.jar -f http://hapi.fhir.org/baseR4 -p 8080` to start the server on port
8080 on your local machine and connect to the public FHIR R4 server.

By default, FhirExtinguisher assumes, the server is FHIR R4. If you want to connect with a (D)STU3 server, please add 
`-v stu3` to the command line arguments.

Available command line options:

* `-f [url]` FHIR server URL
* `-v ["r4"|"stu3"]` FHIR version of the server
* `-a [username]:[password]` Basic authentication credentials, if required by FHIR server
* `-at [token]` Bearer token, if required by the FHIR server
* `-h "[header-name]: [value]"` Add a custom header
* `-p [portnumber]` Port number on your local machine to open, e.g. with `-p 8080`, the GUI will be available
  at `http://localhost:8080/`
* `-t 30` Set timeout for FHIR server in seconds (60 is default)
* `-ext` Allow connections of non-localhost machines
* `--no-conformance-check` Do not validate FHIR server URL at startup

To stop the FhirExtinguisher, press <kbd>Ctrl</kbd>+<kbd>C</kbd> in the command line window!

## Running with Docker

If you don't want to install Java on your local machine, you can use `docker pull wwuimi/fhirextinguisher` to pull a
docker image.


If you pulled the docker image from the docker hub use `docker run --rm -p 127.0.0.1:8080:8080 --name fhirextinguisher wwuimi/fhirextinguisher -f <fhir-server-url> -p 8080 -ext` to start the container.

If you build the image from the Dockerfile like its described in the Building section use 
`docker run --rm -p 127.0.0.1:8080:8080 --name fhirextinguisher fhirextinguisher -f <fhir-server-url> -p 8080 -ext`  to start the container.

Use `docker stop fhirextinguisher` to stop the container. You can use `docker ps` to check if container is
running.

## Usage

### By URL

You can create your own links using the specifications above. In R, you can use
`data <- read.csv('http://localhost:8080/fhir/Patient?__limit=50&__columns=id:Patient.id@join(" ")')` to always start
with a fresh version from the server. However, the FhirExtinguisher must be running during execution of the script.

You can create a link using the GUI and copy it into your R script.

### Graphical User Interface

You can use the GUI, which will be available under `http://localhost:[portnumber]/`, to download/export a CSV.
There, you can enter your query in the editor on the top using the [FHIR Search API](https://www.hl7.org/fhir/search.html) 
and add the columns in the sidebar on the left. 

![Screenshot](img/Launch-And-Usage-Short.gif)

Note that the table shown in the GUI **is only a preview based on the first Bundle returned by the server**. Note also, that the *Import Resource...* 
function is providing suggestions based on the Resource's StructureDefinintion, there is **no guarantee of completeness**.
Array fields are inserted like single elements, so there might be unintended behaviour.

![Screencast](img/FHIRSearch-Autocomplete.gif)

In the searchbar, you can use <kbd>Ctrl</kbd>+<kbd>Space</kbd> for autocompletion. This is based on the server conformance 
statement on HAPI FHIR using FHIR R4. Note that actual query options on your server might differ.

## Useful FHIRPath expressions

* `.type().name` Get the name of the type, e.g. `Patient.deceased.type().name` is either `dateTime` or `boolean`
* `.resolve()` resolves a reference to another resource.
* `iif(gender = "female", "w", "m")` if-else-expression
* `.substring(0, 1)` Return only the first letter of string

Custom functions introduced by FhirExtinguisher:

* `getIdPart()` circumvents some inconveniences with HAPI FHIR's `IdType` class
* `getChildFields()` returns a list of possible field names, but the expression until this point must return at least
  one element
* `stringify()` returns a recursive serialization of all the element's fields

## Building

Requirements: **Java 11, npm 6.13.x**

Use `./gradlew shadowJar` to compile the project. The resulting .jar file will be in `/build/libs/`.

At the first time, to compile the frontend, you have to run `npm install` (and eventually `npm install --only=dev`) in
the
`/frontend` folder, as well as `npm run antlr4ts`, since the gradle build script will only invoke webpack and copy the
files into the .jar file.

If you get `Process 'command 'cmd'' finished with non-zero exit value 2`, please execute `"node_modules/.bin/webpack"`
for the webpack error message.

### Building with Docker

Check out the repository, run  `docker build --tag fhirextinguisher .`

### Frontend Development

You can execute `node_modules\.bin\webpack-cli serve` to start an automatically updating version of the frontend. If you
start the FhirExtinguisher using port 8081, even the backend will work.

## WAR deployment

Besides running the FhirExtinguisher on your local machine using the built-in Jetty engine, it is also possible to
deploy the FhirExtinguisher via Tomcat. Therefore, you can download the pre-compiled war file, open it as zip file and
edit the parameters in `WEB-INF\classes\application.conf`. Currently, there seems to be a bug in ktor, which prevents
the UI from loading if the FhirExtinguisher is not deployed on the root path. Also, having an Apache Web Server ahead of
Tomcat can cause some issues when using AJP, so I recommend using plain HTTP instead. I will update this text as well as
the Ktor version as soon as these issues are resolved.

## Authors

* **Johannes Oehm** | (+49) 251 / 83-5 82 47 | johannes.oehm@uni-muenster.de

## Citation

* Oehm J, Storck M, Fechner M, Brix TJ, Yildirim K, Dugas M. FhirExtinguisher: A FHIR Resource Flattening Tool Using
  FHIRPath. Stud Health Technol Inform. 2021 May 27;281:1112-1113. doi: 10.3233/SHTI210369. PMID: 34042862.

## License

Apache Licence 2.0

## Acknowledgement

Supported by BMBF grant No. 01ZZ1802V (HiGHmed/Münster) 
