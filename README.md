# FHIR Extinguisher

## Goal 
This project's goal is to simplify data analysis on HL7 FHIR by easing conversion 
of the hierarchical structure into a flat list for analysing R DataFrames.

This project is a simple WebServer, that connects to a FHIR server and forwards the requests.
This way you can use all the FHIR filtering options provided by the server. For each Resource in the resulting bundle, 
a FHIRPath expression is evaluated.

All parameters are forwarded as they are, except: 
* `__columns` Must be `name:expression,name2:expression2`. Name might be followed by options like `@join(" ")` and `@explode`, 
which control how to handle multiple returned results by the FHIR path expression. `@join` concats the strings into a single cell with a delimiter of your choice,
while explode will create multiple rows for the element.
* `__limit` By default, the FhirExtinguisher automatically fetches the next bundle. This way you can limit the number of resources processed.
* `__csvFormat` TODO Not implemented yet

The returned bundles are taken and evaluated against multiple FHIR path expressions.

Please escape `@join(", ")` as `@join("%2C ")`and `@join(":")` as `@join("%3A")`!