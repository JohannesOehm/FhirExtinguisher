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

for (let fhirVersion of ["stu3", "r4"]) {
    for (let fileName of ["profiles-resources.json", "profiles-types.json"]) {
        let filename = path.join(__dirname, fhirVersion, fileName);
        let data = JSON.parse(fs.readFileSync(filename));
        removeUnncessaryFields(data);
        fs.writeFileSync(filename, JSON.stringify(data))
    }

    let data = JSON.parse(fs.readFileSync(path.join(__dirname, fhirVersion, "search-parameters.json")));
    let result = [];
    for (let entry of data.entry){
        let resource = entry.resource;
        if(resource.type === "reference") {
            result.push({
                base: resource.base,
                parameter: resource.code,
                target: resource.target
            });
        }
    }
    fs.writeFileSync("reference-types.json", JSON.stringify(result));

}



