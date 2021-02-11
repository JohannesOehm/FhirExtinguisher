<template>
  <div>
    <div style="display: flex; justify-content: space-between; align-items: center" class=" border-bottom">
      <h1 class="h2">Results</h1>
      <div class="btn-toolbar">
        <div class="btn-group mr-2">
          <button class="btn btn-sm btn-outline-secondary" v-bind:class="{ active: showRaw }"
                  v-on:click="toggleRaw">Raw View
          </button>
        </div>
        <div class="btn-group mr-2">
          <button @click="copyToClipboard()" class="btn btn-sm btn-outline-secondary">
            Copy Link
          </button>
          <button @click="copyRCode()" class="btn btn-sm btn-outline-secondary">
            Copy R code
          </button>
          <button @click="importLink()" class="btn btn-sm btn-outline-secondary">
            Import Link
          </button>
        </div>
        <div class="btn-group mr-2">
          <button @click="makeDownload()" class="btn btn-sm btn-outline-secondary" type="submit">
            Download
          </button>
          <button class="btn btn-sm btn-outline-secondary" v-on:click="editLimit">Limit:
            {{ limit }}
          </button>
        </div>

      </div>
    </div>
    <div class="table-responsive" v-if="!showRaw">
      <table class="table table-striped table-sm" style="white-space: pre-wrap;" v-if="tableData != null">
        <thead>
        <tr>
          <th v-for="(fieldName, idx) in tableData.fields"><span v-if="idx !== 0">{{ fieldName }}</span></th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="row in tableData.records" v-on:dblclick="openRawDialog(row[0])">
          <td v-for="(rowElem, idx) in row"><span v-if="idx !== 0"> {{ rowElem }}</span></td>
        </tr>
        </tbody>
        <caption>
          This is just a preview, where only the resources in the first Bundle returned by the server were processed.
          To fetch as many bundle pages from the FHIR server as needed to fulfill the limit, use the download link
          above.
        </caption>
      </table>
      <div v-else>
        Table data is not available.<br><br>
        <span v-if="!tableLoading && !dataLoading && tableError == null && rawError == null">
          Please enter a valid FHIR Search query in the text field above and press GET!</span>
        <span v-if="columns.length === 0">
          There are no columns speicified</span>
        <span v-if="dataLoading">Raw data is loading...</span>
        <span v-if="tableLoading">Table data is loading...</span>
        <div v-if="tableError != null"><br>
          <pre>{{ tableError }}</pre>
        </div>
      </div>
    </div>
    <div id="rawView" v-else>
      <div v-html="rawDataWithHighlighting" v-if="rawData != null">
      </div>
      <div v-else>
        <div v-if="rawError != null">
          Error: <br>
          <pre>{{ rawError }}</pre>
        </div>
        <div v-else-if="!dataLoading">
          Please enter a valid FHIR Query URL and press GET!
        </div>
        <div v-else>
          Loading...
        </div>
      </div>
    </div>

  </div>
</template>

<script lang="ts">
import * as CSV from '../csv.js';
import * as monaco from "monaco-editor";
import {columnsToString, VmColumn} from "./my-app";

type TableData = { records: string[][], fields: string[], metadata: any };

function stringifyHeaders(headers: Headers) {
  let s = "";
  headers.forEach((value, key) => {
    s += key + ": " + value + "\n";
  });
  return s;
}

function copyStringToClipboard(stringToCopy: string) {
  const el = document.createElement('textarea');
  el.value = stringToCopy;
  el.setAttribute('readonly', '');
  el.style.position = 'absolute';
  el.style.left = '-9999px';
  document.body.appendChild(el);
  el.select();
  document.execCommand('copy');
  document.body.removeChild(el);
}

let rTypes: { [name: string]: string } | null = null;

export default {
  name: "ContentView",
  data: function (): {
    showRaw: boolean, tableLoading: boolean, tableData: TableData, tableError: string,
    dataLoading: false, rawDataWithHighlighting: string, rawData: string, rawError: string,
    rawDataFormat: "xml" | "json"
  } {
    return {
      // table: {data:null, loading: false, error: null},
      // raw: {data:null, loading: false, error: null},
      showRaw: false,
      tableData: null,
      tableError: null,
      tableLoading: false,
      dataLoading: false,
      rawDataWithHighlighting: null,
      rawData: null,
      rawError: null,
      rawDataFormat: "json"
    }
  },
  props: ['columns', 'limit'],
  methods: {
    getDownloadUrl(): string {
      return this.$parent.getDownloadUrl();
    },
    toggleRaw: function () {
      this.showRaw = !this.showRaw;
    },
    editLimit: function () {
      let newLimit = window.prompt("Please enter the maximum number of resources to process:", this.limit);
      let newValue = parseInt(newLimit);
      if (!isNaN(newValue)) {
        this.$emit('update-limit', newValue);
      }
    },
    loadBundle: async function () {
      this.rawData = null;
      this.dataLoading = true;
      this.rawError = null;

      let url = (<any>window).searchEditor.getValue();
      if (url.includes("_summary=count")) {
        this.showRaw = true;
      }
      let response = await fetch("redirect/" + url, {
        headers: {"Accept": "application/json"}
      });
      if (response.ok) {
        this.rawDataFormat = response.headers.get("Content-Type").includes("json") ? "json" : "xml";
        this.rawData = await response.text();
      } else {
        this.rawError = (response.status + " " + response.statusText) + "\n" + stringifyHeaders(response.headers);
        this.rawError += "\n\n" + await response.text();
        this.showRaw = true;
        this.tableData = null;
      }
      this.dataLoading = false;
    },
    reEvaluateHighlighting: function () {
      if (this.rawData != null) {
        monaco.editor.colorize(this.rawData, this.rawDataFormat, {})
            .then((it: string) => this.rawDataWithHighlighting = it);
      } else {
        this.rawDataWithHighlighting = null;
      }
    },
    loadTableData: async function () {
      if (!this.rawData) {
        return
      }
      this.tableLoading = true;

      let params = `__limit=${this.limit}&__columns=${encodeURIComponent(columnsToString(this.columns))}`;
      let resourceFormat = this.rawDataFormat == "json" ? "application/json" : "application/xml";
      let response: Response;
      if (params.length < 1000) {
        response = await fetch("processBundle?" + params, {
          method: 'POST',
          body: this.rawData,
          headers: {
            "Content-Type": resourceFormat + "; charset=utf-8",
          }
        });
      } else {
        let formData = new FormData();
        formData.append("bundle", this.rawData);
        formData.append("bundleFormat", resourceFormat);
        formData.append("__limit", this.limit);
        formData.append("__columns", columnsToString(this.columns));

        response = await fetch("processBundle", {
          method: 'POST',
          body: formData,
          headers: {
            // "Content-Type": this.rawDataFormat == "json" ? "application/json" : "application/xml",
          }
        });
      }
      if (response.ok) {
        let csvString = await response.text();
        (<any>CSV).fetch({
          data: csvString
        }).done((it: TableData) => {
          this.tableData = it;
          this.tableError = null;
          this.tableLoading = false;
          rTypes = JSON.parse(response.headers.get("R-DataTypes"));
        }).catch((it: any) => {
          this.tableData = null;
          this.tableError = it;
          this.tableLoading = false;
          rTypes = null;
        });
      } else {
        this.tableData = null;
        this.tableLoading = false;
        this.tableError = (response.status + " " + response.statusText) + "\n" + stringifyHeaders(response.headers);
        this.tableError += "\n\n" + await response.text();
        rTypes = null;
      }
    },
    importLink: function () {
      let link = window.prompt("Please insert link to import!");
      if (link) {
        this.$emit("import-link", link);
      }
    },
    getDownloadLink: function () {
      return window.location.href.split("#")[0] + this.getDownloadUrl();
    },
    copyToClipboard: function () {
      let stringToCopy = this.getDownloadLink();
      copyStringToClipboard(stringToCopy)
    },
    openUrl: function (url: string) {
      window.open(url);
    },
    openRawDialog: function (value: string) {
      this.$emit("show-resource", value);
    },
    copyRCode: function () {
      function escapeRString(value: string): string {
        return value.replace("\\", "\\\\").replace('"', '\\"');
      }

      if (!rTypes) {
        alert("Type info is not available. \n You must click on GET to download the table preview data first!");
        return;
      }

      let tmp: string = "";

      if (Object.values(rTypes).indexOf("DATETIME") !== -1 || Object.values(rTypes).indexOf("TIME") !== -1) {
        tmp += "library(readr)\n"
      }
      if (Object.values(rTypes).indexOf("DATETIME") !== -1) {
        tmp += "setClass(\"fhirDateTime\")\n" +
            "setAs(\"character\", \"fhirDateTime\", function(from) parse_datetime(from))\n"
      }
      if (Object.values(rTypes).indexOf("TIME") !== -1) {
        tmp += "setClass(\"fhirTime\")\n" +
            "setAs(\"character\", \"fhirTime\", function(from) parse_time(from))\n"
      }


      copyStringToClipboard(tmp +
          `data <- read.csv("${escapeRString(this.getDownloadLink())}",
  header=TRUE, encoding="UTF-8",
  colClasses = c(${Object.entries(rTypes).filter(([name, _]) => name != "$raw")
              .map(([name, type]) => `"${escapeRString(name)}"="${type !== "DATE" ? type.toLowerCase() : "Date"}"`).join(",")})
  );
  # Please note that downloading huge amounts of data might take some time and R might give you a timeout.
  # In this case, please use either your browser to download a file or a library like RCurl where timeout is configurable.
`);
    },
    makeDownload: function () {
      let downloadUrl = this.getDownloadUrl();
      //For short URLs we can use GET, otherwise we have to use POST and put parameters into body
      if (downloadUrl.length < 1000) {
        this.openUrl(downloadUrl);
      } else {
        let target = this.$parent.getDownloadUrl(false);
        let html = `
          <form id="post-download-dummy" method="post" action="${target}">
            <input type="hidden" name="__columns" id="post-download-dummy-columns"/>
            <input type="hidden" name="__limit" id="post-download-dummy-limit"/>
          </form>
        `;
        document.body.insertAdjacentHTML("beforeend", html);
        (<HTMLInputElement>document.getElementById("post-download-dummy-columns")).value = columnsToString(this.columns);
        (<HTMLInputElement>document.getElementById("post-download-dummy-limit")).value = this.limit;
        (<HTMLFormElement>document.getElementById("post-download-dummy")).submit();
      }
    }
  },

  watch: {
    rawData: function (newData: string, oldData: string) {
      this.rawDataWithHighlighting = "Loading Highlighter...";
      this.reEvaluateHighlighting();
      this.tableData = null;
      if (newData != null) {
        this.loadTableData();
      }
    },
    columns: function (newData: VmColumn[], oldData: VmColumn[]) {
      this.loadTableData();
    }
  }
}
</script>