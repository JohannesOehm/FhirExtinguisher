<template>
  <b-modal id="modal-show-resource" title="Raw resource" size="xl" hide-footer="true">
    <a href="#" class="text-muted" v-b-modal.modal-test-fhirpath>Test FHIRPath...</a>
    <div id="resourceView"></div>
  </b-modal>
</template>

<script lang="ts">
import * as monaco from "monaco-editor";
import {CancellationToken, editor, IRange, languages} from "monaco-editor";
import {getPositionOfReferences, tokenize} from "../json-tokenize"
import ILink = languages.ILink;

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
        url: this.fhirServerUrl + "/" + model.getValueInRange(range)
      })
    }
    return {
      links: links,
      dispose: () => {
      }
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

        (<any>window).resourceEditor = monaco.editor.create(elementById, {
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


        (<any>window).resourceEditor.setValue(this.text);
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

