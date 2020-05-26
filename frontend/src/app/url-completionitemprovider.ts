import {CancellationToken, editor, IRange, languages, Position as mPosition} from "monaco-editor";
import {R4} from "@ahryman40k/ts-fhir-types";
import CompletionItem = languages.CompletionItem;
import CompletionItemKind = languages.CompletionItemKind;

export class URLCompletionItemProvider implements languages.CompletionItemProvider {
    triggerCharacters = ["?", "&", ":", "=", "."];

    private conformanceStatement: R4.ICapabilityStatement = undefined;
    private referenceTypes: { base: string[], parameter: string, target: string[] }[] = undefined;

    constructor() {
        fetch("/example-conformance.json")
            .then(res => res.json())
            .then(res => this.conformanceStatement = res)

        fetch("/reference-types.json")
            .then(res => res.json())
            .then(res => this.referenceTypes = res);
    }

    provideCompletionItems(model: editor.ITextModel, position: mPosition, context: languages.CompletionContext, token: CancellationToken): languages.CompletionList {
        if (!this.conformanceStatement) {
            return {suggestions: []};
        }

        let textUntilPosition = model.getValueInRange({
            startLineNumber: 1, endLineNumber: 1,
            startColumn: 1, endColumn: position.column
        });

        let fullText = model.getValue();

        let word = model.getWordAtPosition(position);
        console.log("word = ", word);

        let range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word != null ? word.startColumn : position.column,
            endColumn: position.column
        };

        let suggestions: CompletionItem[];
        if (textUntilPosition.indexOf("?") !== -1) {
            let lastParameterStart = Math.max(textUntilPosition.lastIndexOf("&"), textUntilPosition.lastIndexOf("?"));
            if (textUntilPosition.lastIndexOf("=") > lastParameterStart) {
                suggestions = this.getParamValueSuggestions(fullText, range, textUntilPosition);
            } else if (textUntilPosition.lastIndexOf(".") > lastParameterStart) {
                suggestions = this.getChainedSuggestions(fullText, range, textUntilPosition);
            } else if (textUntilPosition.lastIndexOf(":") > lastParameterStart) {
                suggestions = this.getModifierSuggestions(fullText, range, textUntilPosition);
            } else {
                suggestions = this.getSearchParamSuggestions(fullText, range, textUntilPosition);
            }
        } else {
            suggestions = this.getResourceNameSuggestions(range);
        }

        return {
            suggestions: suggestions
        };
    }

    private getResourceNameSuggestions(range: IRange, suffix: String = "?"): CompletionItem[] {
        let types = this.getAllResourceNamesSupportedByServer();
        return types.map(it => (
            {
                label: it,
                kind: CompletionItemKind.Class,
                range: range,
                insertText: it + suffix
            }
        ));
    }

    private getAllResourceNamesSupportedByServer() {
        let foo = this.conformanceStatement.rest.filter(it => it.mode === "server").map(it => it.resource);
        return foo[0].map(it => it.type);
    }

    private getSearchParamSuggestions(fullText: string, range: IRange, textUntilPosition: string): CompletionItem[] {
        let resourceNames = this.getResourceName(textUntilPosition);

        console.log("resourceNames = ", resourceNames);

        let suggestions: CompletionItem[] = [];


        if (resourceNames === null || resourceNames.length === 0) {
            suggestions.push(
                {
                    label: "_type",
                    kind: CompletionItemKind.TypeParameter,
                    range: range,
                    // detail: it.type,
                    insertText: "_type=",
                    documentation: "TODO",
                    preselect: true
                }
            );
        }
        if (resourceNames === null) {
            resourceNames = this.getAllResourceNamesSupportedByServer();
        }

        for (let resourceName of resourceNames) {
            let definition = this.getDefinitionForResourceName(resourceName);
            if (!definition) {
                console.log("Cannot find definition for " + resourceName + "!")
                continue;
            }

            suggestions.push(...definition.searchParam.map(it => (
                {
                    label: it.name,
                    kind: CompletionItemKind.Function,
                    range: range,
                    detail: it.type,
                    insertText: it.name,
                    documentation: it.documentation
                }
            )));

        }

        let additionalParams: CompletionItem[] = [
            {
                label: "_sort",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_sort=",
                documentation: "The client can indicate which order to return the results by using the parameter _sort, " +
                    "which can contain a comma-separated list of sort rules in priority order. A '-' prefix inverts order"
            },
            {
                label: "_has",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_has:",
                // documentation: "TODO"
            },
            {
                label: "_count",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_count=",
                documentation: "How many resources should be returned in one page?"
            },
            {
                label: "_include",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_include",
                // documentation: "TODO"
            },
            {
                label: "_revinclude",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_revinclude",
                // documentation: "TODO"
            },
            {
                label: "_summary",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_summary=",
                documentation: "true | text | data | count | false"
            },
            {
                label: "_total",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_total=",
                documentation: "none | estimate | accurate"
            },
            {
                label: "_elements",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_elements=",
                // documentation: "TODO"
            },
            {
                label: "_format",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_format=",
                // documentation: "TODO"
            },
            {
                label: "_pretty",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_pretty=",
                // documentation: "TODO"
            },
            {
                label: "_query",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_query=",
                // documentation: "TODO"
            },
            {
                label: "_filter",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_filter=",
                // documentation: "TODO"
            },
            {
                label: "_contained",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_contained=",
                documentation: "Whether to return resources contained in other resources in the search matches"
            },
            {
                label: "_containedType",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_containedType=",
                documentation: "If returning contained resources, whether to return the contained or container resources"
            },
            {
                label: "_text",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_text",
                documentation: "Text search against the narrative"
            },
            {
                label: "_content",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_content",
                documentation: "Text search against the entire resource"
            },
            {
                label: "_security",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_security=",
                documentation: "Search by a security label"
            },
            {
                label: "_profile",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_profile=",
                documentation: "Search for all resources tagged with a profile"
            },
            {
                label: "_tag",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_tag=",
                documentation: "Search by a resource tag"
            },
            {
                label: "_lastUpdated",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_lastUpdated=",
                documentation: "Date last updated. Server has discretion on the boundary precision"
            }
        ];

        return suggestions.concat(additionalParams);
    }

    /**
     * Suggestions for
     * Observation?subject.<click>
     * Observation?subject:Patient.<click>
     */
    private getChainedSuggestions(fullText: string, range: IRange, textUntilPosition: string): CompletionItem[] {
        let resourceName = this.getResourceName(textUntilPosition)[0];
        let paramName = textUntilPosition.substring(
            Math.max(textUntilPosition.lastIndexOf("?"), textUntilPosition.lastIndexOf("&")) + 1,
            textUntilPosition.lastIndexOf(".")
        );
        let strings: Set<string> = new Set();
        if (paramName.includes(":")) { //?subject:Patient.
            let referencedResourceName = paramName.substring(paramName.indexOf(":") + 1);
            this.getDefinitionForResourceName(referencedResourceName).searchParam.forEach(it => strings.add(it.name));
        } else { //subject.<click>
            let referenceTypes = this.getReferenceType(resourceName, paramName);
            for (let referenceType of referenceTypes) {
                let definition = this.getDefinitionForResourceName(referenceType);
                definition.searchParam.forEach(it => strings.add(it.name));
            }
        }

        return Array.from(strings).map(it => (
            {
                label: it,
                kind: CompletionItemKind.Function,
                range: range,
                detail: "",
                insertText: it
            }
        ));

        //TODO: Insert type (e.g. subject.name is string,...)

    }

    private getDefinitionForResourceName(resourceName: string) {
        return this.conformanceStatement.rest
            .filter(it => it.mode === "server")[0]
            .resource.filter(it => it.type === resourceName)[0];
    }

    /**
     * null is wildcard
     */
    private getResourceName(textUntilPosition: string): string[] | null {
        let resourceName: string;
        if (textUntilPosition.indexOf("/") !== -1) {
            resourceName = textUntilPosition.substring(0, textUntilPosition.indexOf("/"));
        } else if (textUntilPosition.indexOf("?") !== -1) {
            resourceName = textUntilPosition.substring(0, textUntilPosition.indexOf("?"));
        }

        if (resourceName === "") {
            let idxOfQ = textUntilPosition.indexOf("?");
            if (idxOfQ !== -1) {
                let urlSearchParams = new URLSearchParams(textUntilPosition.substring(idxOfQ + 1));
                if (urlSearchParams.has("_type")) {
                    return urlSearchParams.get("_type").split(",");
                }
            }
            return null;
        } else {
            return [resourceName];
        }

    }

    private getModifierSuggestions(fullText: string, range: IRange, textUntilPosition: string) {

        let paramName = textUntilPosition.substring(
            Math.max(textUntilPosition.lastIndexOf("?"), textUntilPosition.lastIndexOf("&")) + 1,
            textUntilPosition.lastIndexOf(":")
        );

        console.log("paramName = ", paramName);


        if (paramName === "_include" || paramName === "_revinclude") {
            return [
                {
                    label: "iterate",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "iterate=",
                    // documentation: "TODO"
                }
            ]
        }
        if (paramName.startsWith("_has")) {
            let [, resourceNameTgt, joinField, attribute] = paramName.split(":");
            if (!resourceNameTgt) {
                return this.getAllResourceTypes().map(it => (
                    {
                        label: it,
                        kind: CompletionItemKind.Class,
                        range: range,
                        insertText: it + ":"
                    }
                ));
            } else if (resourceNameTgt && !joinField) {
                let definition = this.getDefinitionForResourceName(resourceNameTgt);
                return definition.searchParam
                    .filter(it => it.type === "reference") //TODO This makes only sense on references, doesn't it?
                    .map(it => (
                        {
                            label: it.name,
                            kind: CompletionItemKind.Function,
                            range: range,
                            detail: it.type,
                            insertText: it.name + ":",
                            documentation: it.documentation
                        }
                    ));
            } else if (resourceNameTgt && joinField) {
                let definition = this.getDefinitionForResourceName(resourceNameTgt);
                return definition.searchParam
                    .map(it => (
                        {
                            label: it.name,
                            kind: CompletionItemKind.Function,
                            range: range,
                            detail: it.type,
                            insertText: it.name + "=",
                            documentation: it.documentation
                        }
                    ));
            }
        }

        let resourceNames = this.getResourceName(textUntilPosition);
        let paramDefinition = this.getParamDefinitionFirstMatch(resourceNames, paramName);

        let additionalParams: CompletionItem[] = [];

        if (!paramDefinition) {
            return [];
        } else if (paramDefinition.type === "token") {
            additionalParams = [{
                label: "text",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "text=",
                documentation: "The search parameter is processed as a string that searches text associated with the" +
                    " code/value - either CodeableConcept.text, Coding.display, or Identifier.type.text. In this case, " +
                    "the search functions as a normal string search"
            }, {
                label: "not",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "not=",
                documentation: "Reverse the code matching described in the paragraph above: return all resources " +
                    "that do not have a matching item. Note that this includes resources that have no value for the " +
                    "parameter - e.g. ?gender:not=male includes all patients that do not have gender = male, including" +
                    " patients that do not have a gender at all"
            }, {
                label: "above",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "above=",
                documentation: "The search parameter is a concept with the form [system]|[code], and the search parameter " +
                    "tests whether the coding in a resource subsumes the specified search code. For example, the search" +
                    " concept has an is-a relationship with the coding in the resource, and this includes the coding itself."
            }, {
                label: "below",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "below=",
                documentation: "The search parameter is a concept with the form [system]|[code], and the search parameter " +
                    "tests whether the coding in a resource is subsumed by the specified search code. For example, the " +
                    "coding in the resource has an is-a relationship with the search concept, and this includes the coding itself."
            }, {
                label: "in",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "in=",
                documentation: "The search parameter is a URI (relative or absolute) that identifies a value set, " +
                    "and the search parameter tests whether the coding is in the specified value set. The reference" +
                    " may be literal (to an address where the value set can be found) or logical (a reference to ValueSet.url)." +
                    " If the server can treat the reference as a literal URL, it does, else it tries to match known logical " +
                    "ValueSet.url values."
            }, {
                label: "not-in",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "not-in=",
                documentation: "The search parameter is a URI (relative or absolute) that identifies a value set, and " +
                    "the search parameter tests whether the coding is not in the specified value set."
            }, {
                label: "of-type",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "not-in=",
                documentation: "The search parameter has the format system|code|value, where the system and code refer" +
                    " to a Identifier.type.coding.system and .code, and match if any of the type codes match. All " +
                    "3 parts must be present."
            }]
        } else if (paramDefinition.type === "string") {
            additionalParams = [{
                label: "exact",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "exact=",
                documentation: "The :exact modifier returns results that match the entire supplied parameter, including casing and accents. "
            }, {
                label: "contains",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "contains=",
                documentation: "The :contains modifier returns results that include the supplied parameter value anywhere within the field being searched."
            }]
        } else if (paramDefinition.type === "reference") {
            additionalParams = [
                {
                    label: "identifier",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "identifier=",
                    documentation: "Search by identifier rather than literal reference (e.g. Observation?subject:identifier=http://acme.org/fhir/identifier/mrn|123456)"
                }, {
                    label: "above",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "above=",
                    documentation: "Some references are circular - that is, the reference points to another resource of the same type. When the reference establishes a strict hierarchy, the modifiers :above and :below may be used to search transitively through the hierarchy: "
                }, {
                    label: "below",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "below=",
                    documentation: "The modifier :below is used with canonical references, to control whether the version is considered in the search. \n\nSome references are circular - that is, the reference points to another resource of the same type. When the reference establishes a strict hierarchy, the modifiers :above and :below may be used to search transitively through the hierarchy: "
                },
                // ...this.getAllResourceTypes().map(it => ({
                ...resourceNames.map(resourceName => {
                    return this.getReferenceType(resourceName, paramName).map(it => ({
                        label: it,
                        range: range,
                        kind: CompletionItemKind.Class,
                        insertText: it
                    }))
                }).reduce((acc, val) => acc.concat(val), []) //TODO: Use .flatMap() instead
            ];
        } else if (paramDefinition.type === "date") {

        } else if (paramDefinition.type === "composite") {

        } else if (paramDefinition.type === "quantity") {

        } else if (paramDefinition.type === "uri") {
            additionalParams = [
                {
                    label: "above",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "above=",
                    documentation: "TODO"
                },
                {
                    label: "below",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "below=",
                    documentation: "TODO"
                }
            ]
        } else if (paramDefinition.type === "special") {

        } else {
            // return [];
        }


        let missing: CompletionItem = {
            label: "missing",
            range: range,
            kind: CompletionItemKind.Operator,
            insertText: "missing=",
            detail: "true | false",
            documentation: `Searching for ${paramName}:missing=true will return all the resources that don't have a value for ` +
                `the ${paramName} parameter (which usually equates to not having the relevant element in the resource). Searching ` +
                `for ${paramName}:missing=false will return all the resources that have a value for the gender parameter. \n\nFor simple ` +
                `data type elements, :missing=true will match on all elements where either the underlying element is omitted or` +
                ` where the element is present with extensions but no @value is specified.`
        };
        return additionalParams.concat([missing]);

    }

    private getParamDefinitionFirstMatch(resourceNames: string[], paramName: string) {
        for (let resourceName of resourceNames) {
            let definition = this.getDefinitionForResourceName(resourceNames[0]);

            let paramDefinition = definition.searchParam.filter(it => it.name === paramName);
            if (paramDefinition.length !== 0) {
                return paramDefinition[0];
            }

        }
        return undefined;
    }

    private getParamValueSuggestions(fullText: string, range: IRange, textUntilPosition: string): CompletionItem[] {
        let paramKey = textUntilPosition.substring(
            Math.max(textUntilPosition.lastIndexOf("?"), textUntilPosition.lastIndexOf("&")) + 1,
            textUntilPosition.lastIndexOf("=")
        );
        let paramValue = textUntilPosition.substring(textUntilPosition.lastIndexOf("=") + 1);
        let [paramName, paramModifier] = paramKey.split(":", 2);
        console.log(`paramName = `, paramName, " paramModifier = ", paramModifier, " paramValue = " + paramValue);


        if (paramName === "_summary") {
            return this.returnSummary(range)
        }

        if (paramName === "_pretty" || paramModifier === "missing") {
            return ["true", "false"].map(it => ({
                label: it,
                range: range,
                kind: CompletionItemKind.Value,
                insertText: it
            }));
        }
        if (paramName === "_format") {
            return this.returnFormats(range);
        }


        let resourceNames = this.getResourceName(fullText); //TODO
        if (paramName === "_include") {
            if (paramValue === "") {
                return resourceNames.map(resourceName => ({
                    label: resourceName,
                    range: range,
                    kind: CompletionItemKind.Class,
                    insertText: resourceName + ":"
                }));
            }
        } else if (paramName === "_revinclude") {
            if (paramValue === "") {
                let types = this.getAllResourceTypes();
                return types.map(it => (
                    {
                        label: it,
                        kind: CompletionItemKind.Class,
                        range: range,
                        insertText: it + ":"
                    }
                ));
            }
        }
        if (paramName === "_include" || paramName === "_revinclude" && paramValue.includes(":")) {
            let paramValueResourceName = paramValue.substring(0, paramValue.indexOf(":"));
            let definition = this.getDefinitionForResourceName(paramValueResourceName);
            if (definition) {
                return definition.searchParam
                    .filter(it => it.type === "reference") //TODO This makes only sense on references, doesn't it?
                    .map(it => ({
                            label: it.name,
                            kind: CompletionItemKind.Function,
                            range: range,
                            detail: it.type,
                            insertText: it.name,
                            documentation: it.documentation
                        }));
            }
        }

        if (paramName === "_sort") {
            return resourceNames
                .map(it => this.getDefinitionForResourceName(it))
                .map(it => it.searchParam.map(it => ({
                        label: it.name,
                        kind: CompletionItemKind.Function,
                        range: range,
                        detail: it.type,
                        insertText: it.name,
                        documentation: it.documentation
                    })
                )).reduce((acc, val) => acc.concat(val), []) //TODO: Use .flatMap() instead;
        }

        if (paramName === "_type") {
            return this.getResourceNameSuggestions(range, "");
        }


        return [];
    }

    private returnSummary(range: { endColumn: number; startColumn: number; endLineNumber: number; startLineNumber: number }) {
        return [
            {
                label: "true",
                range: range,
                kind: CompletionItemKind.Value,
                insertText: "true",
                documentation: "Return a limited subset of elements from the resource. This subset SHOULD consist " +
                    "solely of all supported elements that are marked as \"summary\" in the base definition of the " +
                    "resource(s) (see ElementDefinition.isSummary)"
            },
            {
                label: "text",
                range: range,
                kind: CompletionItemKind.Value,
                insertText: "text",
                documentation: "Return only the \"text\" element, the 'id' element, the 'meta' element, and only" +
                    " top-level mandatory elements"
            },
            {
                label: "data",
                range: range,
                kind: CompletionItemKind.Value,
                insertText: "data",
                documentation: "Remove the text element"
            },
            {
                label: "count",
                range: range,
                kind: CompletionItemKind.Value,
                insertText: "count",
                documentation: "Search only: just return a count of the matching resources, without returning the actual matches"
            },
            {
                label: "false",
                range: range,
                kind: CompletionItemKind.Value,
                insertText: "false",
                documentation: "Return all parts of the resource(s)"
            }
        ];
    }

    private returnFormats(range: { endColumn: number; startColumn: number; endLineNumber: number; startLineNumber: number }) {
        let xml = ["xml", "text/xml", "application/xml", "application/fhir+xml"];
        let json = ["json", "application/json", "application/fhir+json"];
        let turtle = ["ttl", "application/fhir+turtle", "text/turtle"];

        let xmlS = this.conformanceStatement.format.filter(it => xml.includes(it)) ? xml : [];
        let jsonS = this.conformanceStatement.format.filter(it => json.includes(it)) ? json : [];
        let turtleS = this.conformanceStatement.format.filter(it => turtle.includes(it)) ? turtle : [];
        return [...xmlS, ...jsonS, ...turtleS, "html", "text/html"].map(it => ({
            label: it,
            range: range,
            kind: CompletionItemKind.Value,
            insertText: it
        }));
    }

    private getAllResourceTypes() {
        return this.conformanceStatement.rest.filter(it => it.mode === "server").map(it => it.resource)[0].map(it => it.type);
    }


    private getReferenceType(base: string, parameter: string): string[] {
        for (let o of this.referenceTypes) {
            if (o.base.includes(base) && o.parameter === parameter) {
                return o.target;
            }
        }
        return [];
    }
}
