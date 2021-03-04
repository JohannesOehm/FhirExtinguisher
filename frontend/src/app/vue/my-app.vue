<template>
  <div id="app" style="display: flex;flex-direction: column;height:100%; max-height: 100%; overflow: hidden;">
    <Searchbar :endpoint-url="endpointUrl" @startRequest="handleRequest" ref="searchbar" style="flex: 0 0 auto;"/>
    <div id="content" style="flex: 1 0 auto; overflow: hidden;max-height: calc(100% - 40px);">
      <div class="bg-light sidebar" id="sidebar" style="width: 500px;">
        <div class="sidebar-sticky" style="">
          <ColumnsView :columns="columns" @addColumn="handleAddColumn"
                       @editColumn="handleEditColumn"/>
        </div>
      </div>
      <div id="separator" draggable="true"></div>
      <div id="tableOrRaw" role="main" style="overflow: auto">
        <MyContentView :columns="columns" :fhir-query="fhirQuery" :rawData="rawData" :limit="limit"
                       @update-columns="updateColumns" @update-limit="updateLimit" @import-link="importLink"
                       @show-resource="handleShowResource"
                       ref="content"/>
      </div>
    </div>
    <DialogColumn :data="dialog.data" :title="dialog.title" @clicked-okay="handleDialogSubmit"/>
    <DialogQuestionnaireTs @start-request="handleRequest" @update-columns="updateColumns" @update-url="updateUrl"/>
    <DialogResource :fhirVersion="fhirVersion" @update-columns="updateColumns"/>
    <DialogCheatSheet/>
    <DialogShowResource :resource="resource" ref="modalResource" :endpointUrl="endpointUrl"
                        v-on:test-fhirpath="handleTestFhirpathResource"/>
    <DialogAbout/>
    <DialogQueryLoad @import-link="importLink" @start-request="handleRequest"/>
    <DialogQuerySave/>
    <DialogTestFhirpath :resource="resource" :fhirpath="fhirpathToTest" @add-column="handleAddColumn"/>
  </div>
</template>


<script lang="ts">
import Searchbar from './searchbar.vue';
import ColumnsView from './columns-view.vue';
import DialogColumn from './dialog-column.vue';
import MyContentView from './my-content-view.vue';
import DialogQuestionnaireTs from './dialog-questionnaire-ts.vue';
import DialogResource from './dialog-resource.vue';
import DialogQueryLoad from './dialog-query-load.vue';
import DialogQuerySave from './dialog-query-save.vue';
import DialogShowResource from './dialog-show-resource.vue';
import DialogTestFhirpath from './dialog-test-fhirpath.vue';
import DialogAbout from './dialog-about.vue';
import DialogCheatSheet from "./dialog-cheat-sheet.vue";
import {
  Column,
  ExplodeLong,
  ExplodeWide,
  Join,
  ListProcessingMode,
  parseColumns,
  stringifyColumns
} from "columns-parser";
// import * as $ from ''

// (<any>$("#sidebar")).resizable({handles: "e"});

// export function columnsToString(columns: Column[]) {
//   function escape(str: string) {
//     return str.replace(/,/g, "\\,").replace(/:/g, "\\:")
//   }
//
//   return columns.map((it: Column) => {
//     let name = it.name.replace(/:/g, "\\:").replace(/@/g, "\\@");
//     let type = it.type.replace(/:/g, "\\:");
//     if (it.subColumns) {
//       type += "(" + it.subColumns.map(it => escape(it.name) + ":" + escape(it.expression)).join(",").replace(/\)/g, "\\)") + ")";
//     }
//     let expression = it.expression.replace(",", "\\,");
//     return `${name}@${type}:${expression}`
//   }).join(",");
// }

export function columnsToString(columns: VmColumn[]): string {
  return stringifyColumns(columns.map((it: VmColumn) => convertFromVmColumn(it)));
}

export function columnsFromString(value: string): VmColumn[] {
  return parseColumns(value).map((it: Column) => convertToVmColumn(it));
}

function convertToVmColumn(column: Column): VmColumn {
  let subColumns: VmSubColumn[];
  let type = column.type.toString();
  if (column.type instanceof ExplodeLong || column.type instanceof ExplodeWide) {
    subColumns = column.type.subcolumns.map((it: Column) => ({name: it.name, expression: it.expression}));
    type = "explodeLong";
  }
  if (column.type instanceof ExplodeWide) {
    subColumns.unshift({name: "$disc", expression: column.type.discriminator});
    type = "explodeWide";
  }
  return {
    name: column.name,
    expression: column.expression,
    type: type,
    subColumns: subColumns
  }
}

function convertFromVmColumn(vmColumn: VmColumn): Column {
  let type: ListProcessingMode;
  if (vmColumn.type.toString().startsWith("join")) {
    let sep = /join\(\s*"([^"]*)"\s*\)/.exec(vmColumn.type)[1]
    type = new Join(sep);
  } else if (vmColumn.type.startsWith("explodeLong")) {
    type = new ExplodeLong(vmColumn.subColumns.map((it: VmSubColumn) => new Column(it.name, it.expression, null)))
  } else if (vmColumn.type.startsWith("explodeWide")) {
    let disc = vmColumn.subColumns.filter((it: VmSubColumn) => it.name === "$disc")[0].expression;
    type = new ExplodeWide(disc, vmColumn.subColumns.filter((it: VmSubColumn) => it.name !== "$disc").map(it => new Column(it.name, it.expression, null)));
  }
  return new Column(vmColumn.name, vmColumn.expression, type)
}

function getUrlParams(search: string): Map<string, string> {
  const hashes = search.slice(search.indexOf('?') + 1).split('&');
  const params: Map<string, string> = new Map();
  for (let hash of hashes) {
    let idx = hash.indexOf('=');
    const key = hash.substring(0, idx);
    const val = hash.substring(idx + 1);
    params.set(key, val);
  }
  return params;
}

type ParsedUrl = { limit: number, url: string, columns: Column[] };

export function parseLink(link: string): ParsedUrl {
  let urlToParse: string;
  if (link.indexOf("fhir/") != -1) {
    urlToParse = link.substring(link.indexOf("fhir/") + "fhir/".length);
  } else {
    urlToParse = link;
  }

  let urlParams = getUrlParams(urlToParse);

  let limit = parseInt(urlParams.get("__limit"));

  // let columns = new ColumnsParser().parseColumns(decodeURIComponent(urlParams.get("__columns")));
  let columns = parseColumns(decodeURIComponent(urlParams.get("__columns")));

  let url = urlToParse.split("?")[0];
  let query = [...urlParams.entries()] //TODO: Improve this somehow
      .filter(it => it[0] !== "__columns" && it[0] !== "__limit")
      .map(it => it[0] + "=" + it[1])
      .join("&");
  return {url: `${url}?${query}`, columns, limit}
}

(<any>window).parseLink = parseLink;

/**
 * Vm = View Model
 */
export type VmColumn = { name: string, type: string, expression: string, subColumns?: VmSubColumn[] }
export type VmSubColumn = { name: string, expression: string };

type DialogConfig = {
  title: string, mode: "add" | "edit", idx: number, data: VmColumn
};

export default {
  name: "MyApp",
  data: function (): { resource: string, fhirpathToTest: string, limit: number, columns: any[], rawData: string, endpointUrl: string, dialog: DialogConfig, fhirQuery: string, fhirVersion: "r4" | "stu3" } {
    return {
      columns: [{name: "id", type: 'join(" ")', expression: "getIdPart(id)"},
        {
          name: "identifier",
          type: 'join("\\n\\n")',
          expression: "identifier"
        }, {
          name: "lastUpdated",
          type: 'join(" ")',
          expression: "meta.lastUpdated"
        },
      ],
      rawData: null,
      endpointUrl: "http://url/to/fhir/endpoint",
      fhirVersion: "r4",
      fhirQuery: "",
      resource: "",
      fhirpathToTest: "$this",
      dialog: {
        title: "",
        mode: "add",
        idx: -1,
        data: {
          name: "",
          type: 'join(", ")',
          expression: ""
        }
      },
      limit: 50
    }
  },
  components: {
    DialogCheatSheet,
    DialogColumn,
    Searchbar,
    ColumnsView,
    MyContentView,
    DialogQuestionnaireTs,
    DialogResource,
    DialogAbout,
    DialogShowResource,
    DialogQueryLoad,
    DialogQuerySave,
    DialogTestFhirpath,
  },
  methods: {
    handleAddColumn: function (name: string = "", expression: string = "") {
      this.dialog.title = "Add Column...";
      this.dialog.mode = "add";
      this.dialog.data = {
        name: name,
        type: 'join(", ")',
        expression: expression
      }
      this.$bvModal.show("modal-column")
    },
    handleEditColumn: function (idx: number) {
      this.dialog.title = "Edit Column...";
      this.dialog.mode = "edit";
      this.dialog.idx = idx;
      this.dialog.data = {
        name: this.columns[idx].name,
        type: this.columns[idx].type,
        expression: this.columns[idx].expression,
        subColumns: this.columns[idx].subColumns
      }
      this.$bvModal.show("modal-column");
    },
    handleDialogSubmit: function (data: VmColumn) {
      if (this.dialog.mode === "add") {
        this.columns.push(data);
      } else {
        this.columns.splice(this.dialog.idx, 1, data);
      }
    },
    handleRequest: function () {
      this.$refs.content.loadBundle()
    },
    handleShowResource: function (value: string) {
      this.resource = value;
      this.$bvModal.show('modal-show-resource');
    },
    handleTestFhirpathResource: function (resource: string, fhirpath: string) {
      this.resource = resource;
      this.fhirpathToTest = fhirpath;
      this.$bvModal.show('modal-test-fhirpath');
    },
    updateColumns: function (columns: VmColumn[], replace: Boolean) {
      if (replace) {
        this.columns = [];
      }
      for (let column of columns) {
        this.columns.push(column);
      }
    },
    updateUrl: function (url: string) {
      this.$refs.searchbar.setQueryUrl(url);
    },
    updateLimit: function (newLimit: number) {
      this.limit = newLimit;
    },
    importLink: function (link: string) {
      try {
        (<any>window).searchEditor.setValue(parseLink(link).url);
      } catch (e) {
        console.log(e);
      }
      try {
        this.updateColumns(parseLink(link).columns.map((it: Column) => convertToVmColumn(it)), true);
      } catch (e) {
        console.log(e);
      }

      try {
        this.limit = parseLink(link).limit;
      } catch (e) {
        console.log(e);
      }

    },
    getDownloadUrl: function (addParams: boolean = true) {
      let params = addParams ? `__limit=${this.limit}&__columns=${encodeURIComponent(columnsToString(this.columns))}` : "";
      let fhirQuery = (<any>window).searchEditor?.getValue() ?? "";
      if (fhirQuery.endsWith("?")) {
        return "fhir/" + fhirQuery + params;
      } else if (fhirQuery.includes("?")) {
        return "fhir/" + fhirQuery + "&" + params;
      } else {
        return "fhir/" + fhirQuery + "?" + params;
      }

    }
  },
  computed: {},
  mounted: function () {
    (<any>window).parseColumns = parseColumns;

    fetch("info")
        .then(res => res.json())
        .then(res => {
          this.endpointUrl = res.server + "/";
          this.fhirVersion = res.version;
          document.title = res.server.replace("https://", "").replace("http://", "");
        });

    let separator = document.getElementById("separator");
    var dragging = false;
    separator.onmousedown = function (e: MouseEvent) {
      e.preventDefault();
      document.body.style.cursor = "col-resize";
      dragging = true;
    };
    window.addEventListener("mousemove", function (e: MouseEvent) {
      if (dragging) {
        // e.preventDefault();
        // console.log("dragging", e);
        document.getElementById("sidebar").style.width = e.pageX + "px";
      }
    });
    window.addEventListener("mouseup", function (e: MouseEvent) {
      if (dragging) {
        // e.preventDefault();
        // console.log("dragend", e);
        document.body.style.cursor = null;
        dragging = false;
      }
    });
  }
}
</script>