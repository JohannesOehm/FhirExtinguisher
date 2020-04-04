import {Column} from "./index";

export class ResourceSuggestionService {

    structureDefinitionsResource: any;
    structureDefinitionsTypes: any;

    constructor(fhirVersion: string) {
        fetch(`/structuredefinitions/${fhirVersion}/profiles-resources.json`)
            .then(res => res.json())
            .then(it => this.structureDefinitionsResource = it);
        fetch(`/structuredefinitions/${fhirVersion}/profiles-types.json`)
            .then(res => res.json())
            .then(it => this.structureDefinitionsTypes = it);
    }


    getResourceNames(): string[] {
        return this.structureDefinitionsResource.entry
            .filter((it: any) => it.resource.resourceType === "StructureDefinition")
            // .filter((it:any) => it.resource.type )
            .map((it: any) => it.resource.name)
    }

    getResourceFields(resourceName: string): Column[] {
        let resources = this.structureDefinitionsResource.entry
            .filter((it: any) => it.resource.resourceType === "StructureDefinition")
            .filter((it: any) => it.resource.name == resourceName);

        if (resources.length === 0) {
            console.log("Invalid resource name: " + resourceName);
            return [];
        }
        let result = [];
        for (let element of resources[0].resource.snapshot.element) {
            let expression = element.path;
            let name = this.removeResourceName(expression);

            if (element.path === resourceName) {
                continue;
            }
            let dataType = element.type.length === 1 ? this.getDataType(element.type[0].code) : null;
            if ((dataType === null || dataType.type === "primitive-type") && expression !== resourceName + ".id") {
                result.push({name: name, type: 'join(" ")', expression: expression});
            } else if (expression === resourceName + ".id" || (element.type && element.type.length === 1 && element.type[0].code === "Reference")) {
                expression = `getIdPart(${expression})`;
                result.push({name: name, type: 'join(" ")', expression: expression});
            } else if (dataType.snapshot) {
                for (let element2 of dataType.snapshot.element) {
                    let subpath = this.removeResourceName(element2.path);

                    if (element2.path !== dataType.id) {
                        let expression3 = expression + "." + subpath;
                        result.push({
                            name: this.removeResourceName(expression3),
                            type: 'join(" ")',
                            expression: expression3
                        })
                    }
                }
            }

        }
        return result.filter((it: Column) => !it.expression.includes("extension") && !it.expression.includes("modifierExtension"));
    }

    private removeResourceName(expression: string): string {
        let firstIndexOfDot = expression.indexOf(".");
        let name = firstIndexOfDot !== -1 ? expression.substring(firstIndexOfDot + 1) : expression;
        return name;
    }

    getDataType(datatypeName: string): any | null {
        let result = this.structureDefinitionsTypes.entry
            .filter((it: any) => it.resource.resourceType === "StructureDefinition")
            .filter((it: any) => it.resource.name === datatypeName);
        if (result.length === 1) {
            return result[0].resource;
        } else {
            return null;
        }
    }
}
