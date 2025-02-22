import {Column} from "./index";

import structureDefinitionResourcesR4 from "./fhir-metadata/structuredefinitions/r4/profiles-resources.json"
import structureDefinitionResourcesSTU3 from "./fhir-metadata/structuredefinitions/stu3/profiles-resources.json"
import structureDefinitionsTypesR4 from "./fhir-metadata/structuredefinitions/r4/profiles-types.json"
import structureDefinitionsTypesSTU3 from "./fhir-metadata/structuredefinitions/stu3/profiles-types.json"

export class ResourceSuggestionService {

    constructor(public fhirVersion: string) {
    }

    structureDefinitionsResource: any = this.fhirVersion === "r4" ? structureDefinitionResourcesR4 : structureDefinitionResourcesSTU3;
    structureDefinitionsTypes: any = this.fhirVersion === "r4" ? structureDefinitionsTypesR4 : structureDefinitionsTypesSTU3;


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
        let result: Column[] = [];
        for (let element of resources[0].resource.snapshot.element) {
            let expression = element.path;
            let name = this.removeResourceName(expression);

            if (element.path === resourceName) {
                continue;
            }
            if (element.base.path === "Element.id") { //Patient.id => Resource.id, this applies only to BackboneElement
                continue;
            }
            if (element.type && element.type.length === 1) {
                let dataType = this.getDataType(element.type[0].code);
                if (element.base.path === "Resource.id") {//|| dataType.name === "Reference") {
                    expression = `getIdPart(${expression})`;
                    result.push({name: name, type: 'join(" ")', expression: expression});
                } else if (dataType) {
                    if (dataType.kind === "primitive-type") {
                        result.push({name: name, type: 'join(" ")', expression: expression});
                    } else if (dataType.snapshot) {
                        this.addTypeElements(dataType, expression, expression, result);
                    } else {
                        result.push({name: name, type: 'join(" ")', expression: expression});
                    }
                } else {
                    result.push({name: name, type: 'join(" ")', expression: expression}); //fallback
                }
            } else if (element.type && element.type.length > 1) {
                let startExpression = expression.substring(0, expression.lastIndexOf("[x]"));
                for (let type of element.type) {
                    let dataType = this.getDataType(type.code);
                    let expressionWithCasting = startExpression + ".ofType(" + dataType.name + ")";
                    let newName = startExpression + dataType.name[0].toUpperCase() + dataType.name.substring(1);
                    if (dataType.kind === "primitive-type") {
                        result.push({
                            name: this.removeResourceName(newName),
                            type: 'join(" ")',
                            expression: expressionWithCasting
                        });
                    } else {
                        this.addTypeElements(dataType, expressionWithCasting, newName, result);
                    }
                }
            }


        }
        return result.filter((it: Column) => !it.expression.includes("extension") && !it.expression.includes("modifierExtension"));
    }

    private addTypeElements(dataType: any, expression: string, name: string, result: Column[]) {
        if (dataType.snapshot) {
            for (let element2 of dataType.snapshot.element) {
                let subpath = this.removeResourceName(element2.path);

                if (element2.base.path !== "Element.id" //Hide the generic id for inter-element referencing
                    && element2.path !== dataType.id) { //Hide the base element
                    let expression3 = expression + "." + subpath;
                    let newName = name + "." + subpath;
                    result.push({
                        name: this.removeResourceName(newName),
                        type: 'join(" ")',
                        expression: expression3
                    })
                }
            }
        }
    }

    private removeResourceName(expression: string): string {
        let firstIndexOfDot = expression.indexOf(".");
        return firstIndexOfDot !== -1 ? expression.substring(firstIndexOfDot + 1) : expression;
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
