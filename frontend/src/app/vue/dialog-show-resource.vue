<template>
  <b-modal id="modal-show-resource" title="Raw resource" size="xl" hide-footer="true">
    <!--    <pre>{{ text }}</pre>-->
    <div id="resourceView"></div>
  </b-modal>
</template>

<script lang="ts">
import * as monaco from "monaco-editor";

export default {
  name: "DialogShowResource",
  mounted() {
    this.$root.$on('bv::modal::shown', (bvEvent: any, modalId: any) => {
      // if (!(<any>window).resourceEditor) {
      let elementById = document.getElementById("resourceView");
      elementById.style.height = (window.innerHeight - 200) + "px";
      (<any>window).resourceEditor = monaco.editor.create(elementById, {
        value: "",
        language: "json",
        minimap: {enabled: false},
        scrollbar: {
          vertical: "hidden",
          horizontal: "auto"
        },
        fontSize: 16,
        scrollBeyondLastLine: false,
        overviewRulerLanes: 0,
        overviewRulerBorder: false,
        hideCursorInOverviewRuler: true,
        readOnly: true
      });
      // }

      (<any>window).resourceEditor.setValue(this.text);


    })
  },
  props: ["resource"],
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

