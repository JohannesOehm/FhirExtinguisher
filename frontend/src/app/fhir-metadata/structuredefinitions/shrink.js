/**
 *  The purpose of this script is to remove the unnecessary documentation and unimportant information from the FHIR StructureDefinition,
 *  to speed up performance in browser and decrease the FhirExtinguisher executable size.
 */

let fs = require('fs');
let path = require("path");

function removeUnncessaryFields(bundle) {
    for(let entry of bundle.entry) {
        delete entry.resource.mapping;
        delete entry.resource.differential;
        delete entry.resource.description;
        delete entry.resource.contact;
        delete entry.resource.text;
        delete entry.resource.meta;
        if(entry.resource.snapshot) {
            for (let element of entry.resource.snapshot.element) {
                delete element.definition;
                delete element.short;
                delete element.constraint;
                delete element.comment;
                delete element.mapping;
            }
        }
    }
}

function shrinkStructureDefs(fhirVersion) {
    for (let fileName of ["profiles-resources.json", "profiles-types.json"]) {
        let filename = path.join(__dirname, fhirVersion, fileName);
        let data = JSON.parse(fs.readFileSync(filename));
        removeUnncessaryFields(data);
        fs.writeFileSync(filename, JSON.stringify(data))
    }
}

function extractReferenceTypes(fhirVersion) {
    let data = JSON.parse(fs.readFileSync(path.join(__dirname, fhirVersion, "search-parameters.json")));
    let result = [];
    for (let entry of data.entry) {
        let resource = entry.resource;
        if (resource.type === "reference") {
            result.push({
                base: resource.base,
                parameter: resource.code,
                target: resource.target
            });
        }
    }
    fs.writeFileSync(`reference-types-${fhirVersion}.json`, JSON.stringify(result));
}

function extractElements(fhirVersion) {
    let bundle = JSON.parse(fs.readFileSync(path.join(__dirname, fhirVersion, "profiles-resources.json")));
    let result = {};
    for (let entry of bundle.entry) {
        if(entry.resource.resourceType !== "StructureDefinition") {
            continue;
        }

        let listOfElements = [];
        for(let element of entry.resource.snapshot.element) {
            if (element.path.indexOf(".") !== -1 && element.path.lastIndexOf(".") === element.path.indexOf(".")) { //countOf(".") === 1
                let [resourceName, elementName] = element.path.split(".");
                listOfElements.push(elementName.replace("[x]", ""));
            }
        }
        result[entry.resource.name] = listOfElements;
    }
    fs.writeFileSync(`type-elements-${fhirVersion}.json`, JSON.stringify(result));
}

for (let fhirVersion of ["stu3", "r4"]) {
    shrinkStructureDefs(fhirVersion);
    extractElements(fhirVersion);
    extractReferenceTypes(fhirVersion);
}



