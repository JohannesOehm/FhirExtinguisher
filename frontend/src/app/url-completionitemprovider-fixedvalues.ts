import {IRange, languages} from "monaco-editor";
import CompletionItemKind = languages.CompletionItemKind;

export class FixedValuesHelper {
    public static returnSummary(range: IRange) {
        return [{
            label: "true",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "true",
            documentation: "Return a limited subset of elements from the resource. This subset SHOULD consist " + "solely of all supported elements that are marked as \"summary\" in the base definition of the " + "resource(s) (see ElementDefinition.isSummary)"
        }, {
            label: "text",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "text",
            documentation: "Return only the \"text\" element, the 'id' element, the 'meta' element, and only" + " top-level mandatory elements"
        }, {
            label: "data",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "data",
            documentation: "Remove the text element"
        }, {
            label: "count",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "count",
            documentation: "Search only: just return a count of the matching resources, without returning the actual matches"
        }, {
            label: "false",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "false",
            documentation: "Return all parts of the resource(s)"
        }];
    }

    public static returnContained(range: IRange) {
        return [{
            label: "false",
            range: range,
            detail: "default",
            kind: CompletionItemKind.Value,
            insertText: "false",
            documentation: "do not return contained resources"
        }, {
            label: "true",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "contained",
            documentation: "return contained resource"
        }, {
            label: "both",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "contained",
            documentation: "return both contained and non-contained (normal) resources"
        }];
    }

    public static returnContainedType(range: IRange) {
        return [{
            label: "contained",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "contained",
            documentation: "return only the contained resource"
        }, {
            label: "container",
            range: range,
            detail: "default",
            kind: CompletionItemKind.Value,
            insertText: "contained",
            documentation: "return only the contained resource"
        }];
    }

    public static returnTrueFalse(range: IRange) {
        return ["true", "false"].map(it => ({
            label: it,
            range: range,
            kind: CompletionItemKind.Value,
            insertText: it
        }));
    }

    public static returnTotal(range: IRange) {
        return [{
            label: "none",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "none",
            documentation: "There is no need to populate the total count; the client will not use it"
        }, {
            label: "estimate",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "estimate",
            documentation: "A rough estimate of the number of matching resources is sufficient"
        }, {
            label: "accurate",
            range: range,
            kind: CompletionItemKind.Value,
            insertText: "accurate",
            documentation: "The client requests that the server provide an exact total of the number of matching resources"
        }];
    }

}