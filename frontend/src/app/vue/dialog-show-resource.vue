<template>
  <b-modal id="modal-show-resource" title="Raw resource" size="xl" hide-footer="true">
    <a href="#" class="text-muted" v-b-modal.modal-test-fhirpath>Test FHIRPath...</a>
    <div id="resourceView"></div>
  </b-modal>
</template>

<script lang="ts">
import * as monaco from "monaco-editor";
import {CancellationToken, editor, IPosition, IRange, languages} from "monaco-editor";
import {getPositionOfKeys, getPositionOfReferences} from "../json-tokenize"
import {AST, findAtPosition, parseTokens, Token, tokenize, isKeyInParent, getKeyInParent} from "json-parse-ast"
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
        startLineNumber: token.startLineNumber,
        startColumn: token.startColumn,
        endLineNumber: token.endLineNumber,
        endColumn: token.endColumn - 2
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

function provideDecorationsOnKeys(tokens: Token[]): IModelDeltaDecoration[] {
  let result: IModelDeltaDecoration[] = [];
  for (let token of tokens) {
    let range = {
      startLineNumber: token.position.startLineNumber,
      startColumn: token.position.startColumn,
      endLineNumber: token.position.endLineNumber,
      endColumn: token.position.endColumn - 2
    }
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

function fhirpathEscape(raw: string): string {
  return <string>(<any>raw).replaceAll("\\", "\\\\").replaceAll("'", "\\'").replaceAll('"', '\\"').replaceAll("`", "\\\`").replaceAll("\r", "\\r")
      .replaceAll("\n", "\\n").replaceAll("\t", "\\t").replaceAll("\f", "\\f")
}

function findValue(ast: AST, key_: string): string {
  if (ast.type !== "Object") return;
  let values = <{ key: AST, value: AST }[]>ast.extractValues();
  console.log("values", values);
  for (let {key, value} of values) {
    if (key.value === key_) {
      return value.value;
    }
  }
  return null;
}


function removeDataType(pathelement: string): string {
  let nonUnionTypes = ["servicePeriod", "approvalDate", "lastReviewDate", "effectivePeriod", "recordedDate", "reasonCode",
    "reasonReference", "minutesDuration", "requestedPeriod", "productCode", "instantiatesCanonical", "instantiatesUri",
    "additionalIdentifier", "validityPeriod", "definitionUri", "definitionCanonical", "enteredDate", "derivedFromUri",
    "billablePeriod", "preAuthPeriod", "formCode", "prognosisCodeableConcept", "prognosisReference", "groupIdentifier",
    "dateTime", "subscriberId", "distinctIdentifier", "manufactureDate", "expirationDate", "udiDeviceIdentifier", "languageCode",
    "measurementPeriod", "conclusionCode", "masterIdentifier", "preAuthRefPeriod", "benefitPeriod", "estimatedAge", "statusDate",
    "outcomeCode", "outcomeReference", "requestIdentifier", "occurrenceDateTime", "serviceProvisionCode", "availableTime",
    "procedureReference", "procedureCode", "vaccineCode", "doseQuantity", "packageId", "crossReference", "restoreDate",
    "dataExclusivityPeriod", "internationalBirthDate", "batchIdentifier", "uniqueId", "referenceRange", "validCodedValueSet",
    "normalCodedValueSet", "abnormalCodedValueSet", "criticalCodedValueSet", "birthDate", "paymentDate", "paymentIdentifier",
    "usedReference", "usedCode", "locationCode", "locationReference", "accessionIdentifier", "receivedTime", "organismId",
    "parentSubstanceId", "executionPeriod", "lockedDate", "validateCode", "postalCode", "maxDosePerPeriod", "contentReference",
    "dateRange", "versionId", "linkId"];

  let datatypes = ["Base64Binary", "Boolean", "Canonical", "Code", "Date", "DateTime", "Decimal", "Id", "Instant",
    "Integer", "Markdown", "Oid", "PositiveInt", "String", "Time", "UnsignedInt", "Uri", "Url", "Uuid", "Data Types",
    "Address", "Age", "Annotation", "Attachment", "CodeableConcept", "Coding", "ContactPoint", "Count", "Distance",
    "Duration", "HumanName", "Identifier", "Money", "Period", "Quantity", "Range", "Ratio", "Reference", "SampledData",
    "Signature", "Timing"];

  if (nonUnionTypes.includes(pathelement)) {
    return pathelement;
  }

  for (let datatype of datatypes) {
    pathelement = pathelement.replace(new RegExp(datatype + "$"), "");
  }
  //TODO: This is not optimal, as for example Patient.birthDate becomes "birth"

  return pathelement;

}

// function findTokenAtPosition(tokens: Token[], position: IPosition): [number, any] {
//   for (let i in tokens) {
//     let token = tokens[i];
//     console.log("i", i, "token", token);
//     if ((token.position.start
//         && token.position.start.lineno === position.lineNumber
//         && token.position.start.column < position.column
//         && token.position.end.column >= position.column)
//         ||
//         (!token.position.start
//             && token.position.lineno === position.lineNumber
//             && token.position.column === position.column)) {
//       return [parseInt(i), token];
//     }
//   }
// }

let setWindowSize = function () {
  console.log("setWindowSize()")
  let elementById = document.getElementById("resourceView");
  elementById.style.height = (window.innerHeight - 200) + "px";
  (<any>window).resourceEditor.layout();
};

export default {
  name: "DialogShowResource",
  mounted() {
    this.$root.$on('bv::modal::shown', (bvEvent: any, modalId: any) => {
      if (modalId === "modal-show-resource") {
        monaco.languages.registerLinkProvider("json", new ReferenceLinkProvider(this.endpointUrl, JSON.parse(this.resource).resourceType));
        let resourceView = document.getElementById("resourceView");
        let editor = monaco.editor.create(resourceView, {
          value: "",
          language: "json",
          minimap: {enabled: false},
          // automaticLayout: true,
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
          let ast = parseTokens(tokenize(editor.getValue()));
          let targetNode = findAtPosition(ast, e.target.position);
          if (targetNode === null || !isKeyInParent(targetNode)) return;
          //TODO: Handle union types
          //TODO: Handle extensions (_birthdate)
          //TODO: Select discriminator for extension/modifierExtension / CodeableConcept / Questionnaire.item / identifier

          let current = targetNode;
          let result = "." + removeDataType(current.value);
          while (current) {
            let keyInParent = getKeyInParent(current);
            if (typeof keyInParent === "undefined") {

            } else if (typeof keyInParent === "number") {
              if (current.parent) {
                let upperKey = getKeyInParent(current.parent);
                if (upperKey === "extension" || upperKey === "modifierExtension") {
                  result = ".where(url='" + findValue(current, "url") + "')" + result;
                } else if (upperKey === "coding" || upperKey === "identifier") {
                  result = ".where(system='" + findValue(current, "system") + "')" + result;
                } else {
                  result = "[" + keyInParent + "]" + result;
                }
              } else {
                result = "[" + keyInParent + "]" + result;
              }
            } else {
              keyInParent = keyInParent[0] === "_" ? keyInParent.substring(1) : keyInParent; //remove '_' prefix for extensions
              keyInParent = removeDataType(keyInParent); //remove datatype for union types
              result = "." + keyInParent + result;
            }
            current = current.parent;
          }

          result = result.substring(1).replace(".extension.where(url='", ".extension('");

          this.$emit("test-fhirpath", this.resource, result);


          // let [i, token] = findTokenAtPosition(tokens, e.target.position);
          // let [path, isKey] = <[string[], boolean]>getPathInObject(tokens.slice(0, i + 1));
          // let isKey = true;
          // if (isKey) {
          //   // let pathString = path.map(it => removeDataType(it)).join(".");
          //   this.$emit("test-fhirpath", this.resource, "$this");
          // }
        });

        let positionsOfKeys = getPositionOfKeys(tokenize(editor.getValue()));
        let newDecorations = provideDecorationsOnKeys(positionsOfKeys);
        editor.deltaDecorations([], newDecorations);


        (<any>window).resourceEditor = editor;
        setWindowSize();
        window.addEventListener('resize', setWindowSize);
      }
    })

    this.$root.$on('bv::modal::shown', (bvEvent: any, modalId: any) => {
      if (modalId === "modal-show-resource") {
        window.removeEventListener('resize', setWindowSize);
      }
    });
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

