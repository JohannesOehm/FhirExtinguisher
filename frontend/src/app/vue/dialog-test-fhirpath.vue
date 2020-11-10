<template>
  <b-modal id="modal-test-fhirpath" title="Test FHIRPath" size="xl" hide-footer="true">
    <div class="form-group">
      <div class="input-group">
        <div class="input-group-prepend">
          <label class="input-group-text" for="fhirpathString">FHIRPath:</label>
        </div>
        <input type="text" class="form-control" id="fhirpathString" v-model="fhirpath"
               @keyup="evaluateExpression(fhirpath, resource)">
      </div>
    </div>
    <div id="resultView">
      <ul>
        <li v-for="t in text">
          <pre>{{ t }}</pre>
        </li>
      </ul>
      <span style="color: darkred" v-if="error">{{ error }}</span>
    </div>
  </b-modal>
</template>

<script lang="ts">
import * as _ from "lodash";

// const debounce = (func: any, wait: number) => {
//   let timeout: any;
//
//   return function executedFunction(...args: any) {
//     const later = () => {
//       clearTimeout(timeout);
//       func(...args);
//     };
//
//     clearTimeout(timeout);
//     timeout = setTimeout(later, wait);
//   };
// };

export default {
  name: "DialogTestFhirpath",
  data: function (): { fhirpath: string, text: string[], error: string | null } {
    return {fhirpath: "stringify($this)", text: [], error: null}
  },
  mounted() {
    this.$root.$on('bv::modal::shown', (bvEvent: any, modalId: any) => {
      if (modalId === "modal-test-fhirpath") {
        this.evaluateExpression(this.fhirpath, this.resource)
      }
    })
  },
  props: ["resource"],
  methods: {
    evaluateExpression: _.debounce(async function (fhirpath: string, resource: string): Promise<void> {
      //TODO: Use debounce!!
      try {
        let response = await fetch("/fhirPath?expr=" + encodeURIComponent(fhirpath), {
          method: "POST",
          body: resource,
          headers: {
            "Content-Type": "application/json"
          }
        })
        if (response.status == 200) {
          this.error = null;
          this.text = await response.json();
        } else {
          this.text = [];
          this.error = await response.text();
        }
      } catch (e) {
        this.text = [];
        this.error = e.toString();
      }

    }, 500)
  }
}
</script>

