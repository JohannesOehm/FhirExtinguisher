<template>
  <b-modal id="modal-test-fhirpath" title="Test FHIRPath" size="xl" hide-footer="true">
    <div class="container">
      <div class="row align-items-center justify-content-center">
        <div class="col-9">
          <div class="input-group">
            <div class="input-group-prepend">
              <label class="input-group-text" for="fhirpathString">FHIRPath:</label>
            </div>
            <input type="text" class="form-control" id="fhirpathString" v-model="fhirpath"
                   @keyup="evaluateExpression(fhirpath, resource, stringify)">
          </div>
        </div>
        <div class="col-auto">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" id="stringify" v-model="stringify"
                   @change="evaluateExpression(fhirpath, resource, stringify)">
            <label class="form-check-label" for="stringify" v-b-tooltip.hover
                   title="Wrap expression with stringify() for preview">
              stringify()
            </label>
          </div>
        </div>

        <div class="col-auto">
          <button type="submit" class="btn btn-primary" @click="handleAdd">Add...</button>
        </div>
      </div>
      <div class="row" style="padding-top: 10px;">
        <ul>
          <li v-for="t in text">
            <pre>{{ t }}</pre>
          </li>
        </ul>
        <span style="color: darkred" v-if="error">{{ error }}</span>
      </div>
    </div>
  </b-modal>
</template>

<script lang="ts">
import * as _ from "lodash";

export default {
  name: "DialogTestFhirpath",
  data: function (): { text: string[], error: string | null, stringify: boolean } {
    return {text: [], error: null, stringify: true}
  },
  mounted() {
    this.$root.$on('bv::modal::shown', (bvEvent: any, modalId: any) => {
      if (modalId === "modal-test-fhirpath") {
        this.evaluateExpression(this.fhirpath, this.resource, this.stringify)
      }
    })
  },
  props: ["resource", "fhirpath"],
  methods: {
    handleAdd: function () {
      this.$bvModal.hide("modal-test-fhirpath")
      this.$emit("add-column", this.fhirpath, this.fhirpath);
    },
    evaluateExpression: _.debounce(async function (fhirpath: string, resource: string, stringify: boolean): Promise<void> {
      try {
        let expr = stringify ? `stringify(${fhirpath})` : fhirpath;
        let response = await fetch("fhirPath?expr=" + encodeURIComponent(expr), {
          method: "POST",
          body: resource,
          headers: {
            "Content-Type": "application/json"
          }
        })
        if (response.status == 200) {
          this.error = null;
          this.text = await response.json();
        } else if (response.status == 400) {
          this.text = [];
          this.error = await response.text();
        } else {
          this.text = [];
          this.error = response.status + " " + response.statusText;
          this.error += await response.text();
        }
      } catch (e) {
        this.text = [];
        this.error = e.toString();
      }

    }, 300)
  }
}
</script>

