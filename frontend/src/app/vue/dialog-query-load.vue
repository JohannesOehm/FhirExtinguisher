<template>
  <b-modal @ok="handleOk" @shown="handleShown" id="modal-query-load" title="Load query">
    <form>
      <div class="form-group">
        <div class="input-group">
          <div class="input-group-prepend">
            <label class="input-group-text" for="resourceName">Query:</label>
          </div>
          <select class="custom-select" id="resourceName" v-model="query">
            <option v-bind:value="query" v-for="it in queries">
              {{ it.name }}
            </option>
          </select>
        </div>
      </div>
    </form>
  </b-modal>
</template>

<script lang="ts">
import {ResourceSuggestionService} from '../ResourceSuggestionService'
import {Column} from "../index";
import * as CSV from "../csv";

type StoredQuery = { name: string, url: string }

export default {
  name: "DialogQueryLoad",
  data: function (): { queries: StoredQuery[], query: StoredQuery } {
    return {
      queries: [],
      query: null
    }
  },
  methods: {
    handleOk: function () {
      this.$emit("import-link", this.query.url);
    },
    handleChange: function (e: any) {
    },
    handleShown: async function () {
      let response = await fetch("/query")
      let csvString = await response.text();
      (<any>CSV).fetch({
        data: csvString
      }).done((it: any) => {
        let tmp = [];
        for (let record of it.records) {
          tmp.push({name: record[0], url: record[1]});
        }

        this.queries = tmp;
      }).catch((it: any) => {
            alert(it);
            this.tableData = null;
          }
      );
    }
  },
  watch: {
  },
  mounted: function () {
  }
}
</script>