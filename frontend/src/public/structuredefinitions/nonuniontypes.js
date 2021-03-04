let fs = require('fs');
let path = require("path");
let datatypes = [
    "Base64Binary",
    "Boolean",
    "Canonical",
    "Code",
    "Date",
    "DateTime",
    "Decimal",
    "Id",
    "Instant",
    "Integer",
    "Markdown",
    "Oid",
    "PositiveInt",
    "String",
    "Time",
    "UnsignedInt",
    "Uri",
    "Url",
    "Uuid",
    "Data Types",
    "Address",
    "Age",
    "Annotation",
    "Attachment",
    "CodeableConcept",
    "Coding",
    "ContactPoint",
    "Count",
    "Distance",
    "Duration",
    "HumanName",
    "Identifier",
    "Money",
    "Period",
    "Quantity",
    "Range",
    "Ratio",
    "Reference",
    "SampledData",
    "Signature",
    "Timing"
];

function union(setA, setB) {
    var _union = new Set(setA);
    for (var elem of setB) {
        _union.add(elem);
    }
    return _union;
}


function boilerplate(fhirVersion) {
    let result = new Set();
    for (let fileName of ["profiles-resources.json", "profiles-types.json"]) {
        let filename = path.join(__dirname, fhirVersion, fileName);
        let data = JSON.parse(fs.readFileSync(filename));
        result = union(result, getNonUnionTypes(data));
        console.log(result);
    }
    fs.writeFileSync("non-union-types.json", JSON.stringify(Array.from(result)))
}

function getNonUnionTypes(bundle) {
    let results = new Set();
    for (let entry of bundle.entry) {
        if (entry.resource.snapshot) {
            for (let element of entry.resource.snapshot.element) {
                let [resourceName, elementName] = element.path.split(".");
                for (let datatype of datatypes) {
                    if (elementName && elementName.includes(datatype)) {
                        console.log(elementName, datatype);
                        results.add(elementName);
                    }
                }
                // if (elementName && elementName.includes("[x]")) {
                //     console.log(resourceName, elementName);
                // }
                // listOfElements.push(elementName.replace("[x]", ""));
            }
        }
    }
    return results;
}

for (let fhirVersion of ["stu3", "r4"]) {
    boilerplate(fhirVersion);
}
