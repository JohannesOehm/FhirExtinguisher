<template>
  <b-modal id="modal-show-resource" title="Raw resource" size="xl" hide-footer="true">
    <a href="#" class="text-muted" v-b-modal.modal-test-fhirpath>Test FHIRPath...</a>
    <div id="resourceView"></div>
  </b-modal>
</template>

<script lang="ts">
import * as monaco from "monaco-editor";
import {CancellationToken, editor, IPosition, IRange, languages} from "monaco-editor";
import {getPositionOfReferences, getPositionOfKeys, getPathInObject, tokenize} from "../json-tokenize"
import ILink = languages.ILink;
import IEditorMouseEvent = editor.IEditorMouseEvent;
import IModelDeltaDecoration = editor.IModelDeltaDecoration;

class ReferenceLinkProvider implements languages.LinkProvider {
  // resolveLink(link: languages.ILink, token: CancellationToken): languages.ProviderResult<languages.ILink> {
  //   return undefined;
  // }

  constructor(public fhirServerUrl: string, public resourceType: string) {

  }


  resTypesRefs: any = {
    "Encounter": ["diagnosis.condition.reference", "subject.reference", "episodeOfCare.reference", "basedOn.reference",
      "appointment.reference", "reasonReference.reference", "account.reference", "hospitalization.reference", "hospitalization.destination",
      "location.location.reference", "serviceProvider.reference", "partOf.reference"],
    "Patient": ["managingOrganization.reference", "generalPractitioner.reference", "link.other.reference", "contact.organization.reference"],
    "Condition": ["recorder.reference", "asserter.reference", "stage.assessment.reference", "evidence.detail.reference",
      "subject.reference", "encounter.reference"],
    "Observation": ["basedOn.reference", "partOf.reference", "subject.reference", "focus.reference", "encounter.reference",
      "performer.reference", "specimen.reference", "device.reference", "hasMember.reference", "derivedFrom.reference"]
  }

  provideLinks(model: editor.ITextModel, cancellationToken: CancellationToken): languages.ProviderResult<languages.ILinksList> {
    let tokens = getPositionOfReferences(tokenize(model.getValue()), this.resTypesRefs[this.resourceType]);
    let links: ILink[] = [];
    for (let token of tokens) {
      let range: IRange = {
        startLineNumber: token.start.lineno,
        startColumn: token.start.column + 1,
        endLineNumber: token.end.lineno,
        endColumn: token.end.column
      };
      links.push({
        range: range,
        tooltip: "Open Reference",
        url: this.fhirServerUrl + (this.fhirServerUrl.endsWith("/") ? "" : "/") + model.getValueInRange(range)
      })
    }
    return {
      links: links,
      dispose: () => {
      }
    }
  }
}

function provideDecorationsOnKeys(tokens: any): IModelDeltaDecoration[] {
  let result: IModelDeltaDecoration[] = [];
  for (let token of tokens) {
    let range: IRange = {
      startLineNumber: token.start.lineno,
      startColumn: token.start.column + 1,
      endLineNumber: token.end.lineno,
      endColumn: token.end.column
    };
    result.push({
      range: range,
      options: {
        isWholeLine: false,
        inlineClassName: 'show-resource-json-key',
        // glyphMarginClassName: 'myGlyphMarginClass'
      }
    });
  }
  return result;
}

function removeDataType(pathelement: string): string {
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

  for (let datatype of datatypes) {
    pathelement = pathelement.replace(new RegExp(datatype + "$"), "")
  }
  //TODO: This is not optimal, as for example Patient.birthDate becomes "birth"

  return pathelement;

}

function findTokenAtPosition(tokens: any[], position: IPosition): [number, any] {
  for (let i in tokens) {
    let token = tokens[i];
    console.log("i", i, "token", token);
    if ((token.position.start
        && token.position.start.lineno === position.lineNumber
        && token.position.start.column < position.column
        && token.position.end.column >= position.column)
        ||
        (!token.position.start
            && token.position.lineno === position.lineNumber
            && token.position.column === position.column)) {
      return [parseInt(i), token];
    }
  }
}


export default {
  name: "DialogShowResource",
  mounted() {
    this.$root.$on('bv::modal::shown', (bvEvent: any, modalId: any) => {
      if (modalId === "modal-show-resource") {
        let elementById = document.getElementById("resourceView");
        elementById.style.height = (window.innerHeight - 200) + "px";

        monaco.languages.registerLinkProvider("json", new ReferenceLinkProvider(this.endpointUrl, JSON.parse(this.resource).resourceType));

        let editor = monaco.editor.create(elementById, {
          value: "",
          language: "json",
          minimap: {enabled: false},
          // scrollbar: {
          //   vertical: "hidden",
          //   horizontal: "auto"
          // },
          fontSize: 16,
          scrollBeyondLastLine: false,
          // overviewRulerLanes: 0,
          // overviewRulerBorder: false,
          // hideCursorInOverviewRuler: true,
          readOnly: true
        });
        editor.setValue(this.text);

        editor.onMouseDown((e: IEditorMouseEvent) => {
          let tokens = tokenize(editor.getValue());
          let [i, token] = findTokenAtPosition(tokens, e.target.position);
          let [path, isKey] = <[string[], boolean]>getPathInObject(tokens.slice(0, i + 1));
          if (isKey) {
            let pathString = path.map(it => removeDataType(it)).join(".");
            this.$emit("test-fhirpath", this.resource, pathString);
          }
        });

        let positionsOfKeys = getPositionOfKeys(tokenize(editor.getValue()));
        let newDecorations = provideDecorationsOnKeys(positionsOfKeys);
        editor.deltaDecorations([], newDecorations);


        (<any>window).resourceEditor = editor;
      }
    })
  },
  props: ["resource", "endpointUrl"],
  computed: {
    text: function () {
      try {
        return JSON.stringify(JSON.parse(this.resource), null, 2);
      } catch (e) {
        return "";
      }
    },
  }
}
</script>

