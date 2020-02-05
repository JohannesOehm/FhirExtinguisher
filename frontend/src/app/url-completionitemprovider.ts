import {CancellationToken, editor, languages, Position as mPosition} from "monaco-editor";
import {R4} from "@ahryman40k/ts-fhir-types";
import CompletionItem = languages.CompletionItem;
import CompletionItemKind = languages.CompletionItemKind;

export class URLCompletionItemProvider implements languages.CompletionItemProvider {
    triggerCharacters = ["?", "&"];

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
            suggestions = this.searchParamSuggestions(model, position, textUntilPosition);
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
        let resourceName: string;
        if (textUntilPosition.indexOf("/") !== -1) {
            resourceName = textUntilPosition.substring(0, textUntilPosition.indexOf("/"));
        } else if (textUntilPosition.indexOf("?") !== -1) {
            resourceName = textUntilPosition.substring(0, textUntilPosition.indexOf("?"));
        } else {
            resourceName = textUntilPosition;
        }


        let definition = this.conformanceStatement.rest
            .filter(it => it.mode === "server")[0]
            .resource.filter(it => it.type === resourceName)[0];


        // definition.searchParam


        let word = model.getWordAtPosition(position);
        console.log("word = ", word);

        let range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word != null ? word.startColumn : 0,
            endColumn: position.column
        };


        return definition.searchParam.map(it => it.name).map(it => (
            {
                label: it,
                kind: CompletionItemKind.Function,
                range: range,
                insertText: it
            }
        ));
    }
}
