import {CancellationToken, editor, languages, Position as mPosition} from "monaco-editor";
import {R4} from "@ahryman40k/ts-fhir-types";
import CompletionItem = languages.CompletionItem;
import CompletionItemKind = languages.CompletionItemKind;

export class URLCompletionItemProvider implements languages.CompletionItemProvider {
    triggerCharacters = ["?", "&", ":"];

    private conformanceStatement: R4.ICapabilityStatement = undefined;

    constructor() {
        fetch("/example-conformance.json")
            .then(res => res.json())
            .then(res => this.conformanceStatement = res)
    }

    provideCompletionItems(model: editor.ITextModel, position: mPosition, context: languages.CompletionContext, token: CancellationToken): languages.CompletionList {
        if (!this.conformanceStatement) {
            return {suggestions: []};
        }


        let textUntilPosition = model.getValueInRange({
            startLineNumber: 1, endLineNumber: 1,
            startColumn: 1, endColumn: position.column
        });
        let suggestions: CompletionItem[];
        if (textUntilPosition.indexOf("?") !== -1) {
            console.log("Suggestions for QueryParams");
            let lastParameterStart = Math.max(textUntilPosition.lastIndexOf("&"), textUntilPosition.lastIndexOf("?"));
            if (textUntilPosition.lastIndexOf("=") > lastParameterStart) {
                suggestions = this.paramValueSuggestions(model, position, textUntilPosition);
            } else if (textUntilPosition.lastIndexOf(":") > lastParameterStart) {
                suggestions = this.modifierSuggestions(model, position, textUntilPosition);
            } else {
                suggestions = this.searchParamSuggestions(model, position, textUntilPosition);
            }
        } else {
            console.log("Suggestions for ResourceNames");
            suggestions = this.resourceNameSuggestions(model, position);
        }

        return {
            suggestions: suggestions
        };
    }

    private resourceNameSuggestions(model: editor.ITextModel, position: mPosition): CompletionItem[] {
        let foo = this.conformanceStatement.rest.filter(it => it.mode === "server").map(it => it.resource);
        let types = foo[0].map(it => it.type);

        let word = model.getWordAtPosition(position);
        console.log("word = ", word);

        let range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word != null ? word.startColumn : position.column,
            endColumn: position.column
        };


        return types.map(it => (
            {
                label: it,
                kind: CompletionItemKind.Class,
                range: range,
                insertText: it + "?"
            }
        ));
    }

    private searchParamSuggestions(model: editor.ITextModel, position: mPosition, textUntilPosition: string): CompletionItem[] {
        let resourceName = this.getResourceName(textUntilPosition);
        let definition = this.getDefintionForResourceName(resourceName);


        let word = model.getWordAtPosition(position);
        console.log("word = ", word);

        let range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word != null ? word.startColumn : position.column,
            endColumn: position.column
        };

        let suggestions: CompletionItem[] = definition.searchParam.map(it => (
            {
                label: it.name,
                kind: CompletionItemKind.Function,
                range: range,
                detail: it.type,
                insertText: it.name,
                documentation: it.documentation
            }
        ));

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
                documentation: "TODO"
            },
            {
                label: "_count",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_count=",
                documentation: "TODO"
            },
            {
                label: "_include",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_include=",
                documentation: "TODO"
            },
            {
                label: "_revinclude",
                range: range,
                kind: CompletionItemKind.Operator,
                insertText: "_revinclude=",
                documentation: "TODO"
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
                documentation: "TODO"
            }
        ];

        return suggestions.concat(additionalParams);
    }

    private getDefintionForResourceName(resourceName: string) {
        return this.conformanceStatement.rest
            .filter(it => it.mode === "server")[0]
            .resource.filter(it => it.type === resourceName)[0];
    }

    private getResourceName(textUntilPosition: string) {
        let resourceName: string;
        if (textUntilPosition.indexOf("/") !== -1) {
            resourceName = textUntilPosition.substring(0, textUntilPosition.indexOf("/"));
        } else if (textUntilPosition.indexOf("?") !== -1) {
            resourceName = textUntilPosition.substring(0, textUntilPosition.indexOf("?"));
        } else {
            resourceName = textUntilPosition;
        }
        return resourceName;
    }

    private modifierSuggestions(model: editor.ITextModel, position: mPosition, textUntilPosition: string) {
        let resourceName = this.getResourceName(textUntilPosition);
        let definition = this.getDefintionForResourceName(resourceName);

        let paramName = textUntilPosition.substring(
            Math.max(textUntilPosition.lastIndexOf("?"), textUntilPosition.lastIndexOf("&")) + 1,
            textUntilPosition.lastIndexOf(":")
        );

        console.log("paramName = ", paramName);

        let word = model.getWordAtPosition(position);
        console.log("word = ", word);

        let range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word != null ? word.startColumn : position.column,
            endColumn: position.column
        };

        let paramDefinition = definition.searchParam.filter(it => it.name === paramName);
        let additionalParams: CompletionItem[] = [];
        if (paramDefinition.length === 0) {
            return [];
        } else if (paramDefinition[0].type === "token") {
            additionalParams = [
                {
                    label: "text",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "text=",
                    documentation: "The search parameter is processed as a string that searches text associated with the" +
                        " code/value - either CodeableConcept.text, Coding.display, or Identifier.type.text. In this case, " +
                        "the search functions as a normal string search"
                },
                {
                    label: "not",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "not=",
                    documentation: "Reverse the code matching described in the paragraph above: return all resources " +
                        "that do not have a matching item. Note that this includes resources that have no value for the " +
                        "parameter - e.g. ?gender:not=male includes all patients that do not have gender = male, including" +
                        " patients that do not have a gender at all"
                },
                {
                    label: "above",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "above=",
                    documentation: "The search parameter is a concept with the form [system]|[code], and the search parameter " +
                        "tests whether the coding in a resource subsumes the specified search code. For example, the search" +
                        " concept has an is-a relationship with the coding in the resource, and this includes the coding itself."
                },
                {
                    label: "below",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "below=",
                    documentation: "The search parameter is a concept with the form [system]|[code], and the search parameter " +
                        "tests whether the coding in a resource is subsumed by the specified search code. For example, the " +
                        "coding in the resource has an is-a relationship with the search concept, and this includes the coding itself."
                },
                {
                    label: "in",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "in=",
                    documentation: "The search parameter is a URI (relative or absolute) that identifies a value set, " +
                        "and the search parameter tests whether the coding is in the specified value set. The reference" +
                        " may be literal (to an address where the value set can be found) or logical (a reference to ValueSet.url)." +
                        " If the server can treat the reference as a literal URL, it does, else it tries to match known logical " +
                        "ValueSet.url values."
                },
                {
                    label: "not-in",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "not-in=",
                    documentation: "The search parameter is a URI (relative or absolute) that identifies a value set, and " +
                        "the search parameter tests whether the coding is not in the specified value set."
                },
                {
                    label: "of-type",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "not-in=",
                    documentation: "The search parameter has the format system|code|value, where the system and code refer" +
                        " to a Identifier.type.coding.system and .code, and match if any of the type codes match. All " +
                        "3 parts must be present."
                }
            ]
        } else if (paramDefinition[0].type === "string") {
            additionalParams = [
                {
                    label: "exact",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "exact=",
                    documentation: "The :exact modifier returns results that match the entire supplied parameter, including casing and accents. "
                },
                {
                    label: "contains",
                    range: range,
                    kind: CompletionItemKind.Operator,
                    insertText: "contains=",
                    documentation: "The :contains modifier returns results that include the supplied parameter value anywhere within the field being searched."
                }
            ]
        } else if (paramDefinition[0].type === "reference") {

        } else if (paramDefinition[0].type === "date") {

        } else if (paramDefinition[0].type === "composite") {

        } else if (paramDefinition[0].type === "quantity") {

        } else if (paramDefinition[0].type === "uri") {
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
        } else if (paramDefinition[0].type === "special") {

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

    private paramValueSuggestions(model: editor.ITextModel, position: mPosition, textUntilPosition: string): CompletionItem[] {
        let paramKey = textUntilPosition.substring(
            Math.max(textUntilPosition.lastIndexOf("?"), textUntilPosition.lastIndexOf("&")) + 1,
            textUntilPosition.lastIndexOf("=")
        );
        let [paramName, paramModifier] = paramKey.split(":", 2);
        console.log(`paramName = `, paramName, " paramModifier = ", paramModifier);

        let word = model.getWordAtPosition(position);

        let range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word != null ? word.startColumn : position.column,
            endColumn: position.column
        };

        if (paramName === "_summary") {
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
                },
            ]
        }

        if (paramModifier === "missing") {
            return [
                {
                    label: "true",
                    range: range,
                    kind: CompletionItemKind.Value,
                    insertText: "true"
                },
                {
                    label: "false",
                    range: range,
                    kind: CompletionItemKind.Value,
                    insertText: "false"
                },
            ]
        }
        return [];
    }
}
