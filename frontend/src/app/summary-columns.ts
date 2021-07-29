import bundleR4 from './fhir-metadata/structuredefinitions/r4/profiles-resources.json';
import bundleSTU3 from './fhir-metadata/structuredefinitions/stu3/profiles-resources.json';

export function getSummaryColumns(fhirVersion: string, resourceTypes: string[]) {
    console.log("fhirVersion", fhirVersion)
    let bundle = fhirVersion === "r4" ? bundleR4 : bundleSTU3;

    let result = [];
    for (let entry of bundle.entry) {
        if (resourceTypes.includes(entry.resource.name) && entry.resource.snapshot) {
            for (let element of entry.resource.snapshot.element) {
                if (element.isSummary && !element.path.includes("modifierExtension")) {
                    let expression = element.path.replace(entry.resource.name + ".", "").replace("[x]", "");
                    if (expression === "id") {
                        expression = "getIdPart(id)";
                    }
                    if (expression === "meta") {
                        expression = "stringify(meta)";
                    }
                    if (!expression.includes(".")) {
                        result.push(expression);
                    }
                }
            }
        }
    }
    return result;
}

