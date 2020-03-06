import {Column} from "./index";

export class ResourceSuggestionService {

    structureDefinitions: any;

    constructor(fhirVersion: string) {
        fetch(`/structuredefinitions/${fhirVersion}/profiles-resources.json`)
            .then(res => res.json())
            .then(it => this.structureDefinitions = it);
    }


    getResourceNames(): string[] {
        return this.structureDefinitions.entry
            .filter((it: any) => it.resource.resourceType === "StructureDefinition")
            // .filter((it:any) => it.resource.type )
            .map((it: any) => it.resource.name)
    }

    getResourceFields(resourceName: string): Column[] {
        let resources = this.structureDefinitions.entry
            .filter((it: any) => it.resource.resourceType === "StructureDefinition")
            .filter((it: any) => it.resource.name == resourceName);

        if (resources.length === 0) {
            console.log("Invalid resource name: " + resourceName);
            return [];
        }
        var result = [];
        for (let element of resources[0].resource.snapshot.element) {
            let firstIndexOfDot = element.path.indexOf(".");
            let name = firstIndexOfDot !== -1 ? element.path.substring(firstIndexOfDot + 1) : element.path;

            let expression = element.path;
            if (element.path === resourceName) {
                continue;
            }
            if (expression === resourceName + ".id" || (element.type && element.type.length === 1 && element.type[0].code === "Reference")) {
                expression = `getIdPart(${expression})`;
            }

            result.push({name: name, type: 'join(" ")', expression: expression});
        }
        return result;
    }
}
