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
                    <button @click="copyToClipboard(downloadUrl)" class="btn btn-sm btn-outline-secondary">
                        Copy Link
                    </button>
                    <button @click="importLink()" class="btn btn-sm btn-outline-secondary">
                        Import Link
                    </button>
                </div>
                <div class="btn-group mr-2">
                    <button @click="openUrl(downloadUrl)" class="btn btn-sm btn-outline-secondary" type="submit">
                        Download
                    </button>
                    <button class="btn btn-sm btn-outline-secondary" v-on:click="editLimit">Limit:
                        {{limit}}
                    </button>
                </div>

            </div>
        </div>
        <div class="table-responsive" v-if="!showRaw">
            <table class="table table-striped table-sm" style="white-space: pre-wrap;" v-if="tableData != null">
                <thead>
                <tr>
                    <th v-for="fieldName in tableData.fields">{{fieldName}}</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="row in tableData.records">
                    <td v-for="rowElem in row">{{rowElem}}</td>
                </tr>
                </tbody>
                <caption>
                    This is only a preview, where only the resources in the first Bundle returned by the server was processed.
                    Click download to process as many Bundles as needed to fulfill the limit.
                </caption>
            </table>
            <div v-else>
                Table data is not available.<br><br>
                <div v-if="tableError != null"><br>
                    <pre>{{tableError}}</pre>
                </div>
            </div>
        </div>
        <div id="rawView" v-else>
            <div v-html="rawDataWithHighlighting" v-if="rawData != null">
            </div>
            <div v-else>
                <div v-if="rawError != null">
                    Error: <br>
                    <pre>{{rawError}}</pre>
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
    import {ColumnsParser} from "../column-parser-antlr";


    type TableData = { records: string[][], fields: string[], metadata: any };
    type Column = { name: string, type: string, expression: string };


    function columnsToString(columns: Column[]) {
        return columns.map((it: Column) => {
            let name = it.name.replace(":", "\\:").replace("@", "\\@");
            let type = it.type.replace(":", "\\:");
            let expression = it.expression.replace(",", "\\,");
            return `${name}@${type}:${expression}`
        }).join(",");
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


    function stringifyHeaders(headers: Headers) {
        let s = "";
        headers.forEach((value, key) => {
            s += key + ": " + value + "\n";
        });
        return s;
    }

    export default {
        name: "ContentView",
        data: function (): {
            showRaw: boolean, tableLoading: boolean, tableData: TableData, tableError: string,
            dataLoading: false, rawDataWithHighlighting: string, limit: number, rawData: string, rawError: string
        } {
            return {
                showRaw: true,
                tableData: null,
                tableError: null,
                tableLoading: false,
                dataLoading: false,
                rawDataWithHighlighting: null,
                rawData: null,
                rawError: null,
                limit: 50
            }
        },
        props: ['columns'],
        methods: {
            toggleRaw: function () {
                this.showRaw = !this.showRaw;
            },
            editLimit: function () {
                let newLimit = window.prompt("Please enter the maximum number of resources to process:", this.limit);
                let newValue = parseInt(newLimit);
                if (!isNaN(newValue)) {
                    this.limit = newValue;
                }
            },
            loadBundle: async function () {
                this.rawData = null;
                this.dataLoading = true;
                this.rawError = null;

                let url = (<any>window).searchEditor.getValue();
                let response = await fetch("/redirect/" + url);
                if (response.ok) {
                    this.rawData = await response.text();
                } else {
                    this.rawError = (response.status + " " + response.statusText) + "\n" + stringifyHeaders(response.headers);
                    console.log(response.headers);
                    this.rawError += "\n\n" + await response.text();
                    this.showRaw = true;
                    this.tableData = null;
                }
                this.dataLoading = false;
            },
            reEvaluateHighlighting: function () {
                monaco.editor.colorize(this.rawData, 'json', {})
                    .then((it: string) => this.rawDataWithHighlighting = it);
            },
            loadTableData: async function () {
                let params = `__limit=${this.limit}&__columns=${columnsToString(this.columns)}`;
                let response = await fetch("/fhir/?" + params, {method: 'POST', body: this.rawData});
                if (response.ok) {
                    let csvString = await response.text();
                    (<any>CSV).fetch({
                        data: csvString
                    }).done((it: TableData) => {
                        this.tableData = it;
                        this.tableError = null;
                    }).catch((it: any) => {
                            this.tableData = null;
                            this.tableError = it;
                        }
                    );
                } else {
                    this.tableData = null;
                    this.tableError = (response.status + " " + response.statusText) + "\n" + stringifyHeaders(response.headers);
                    this.tableError += "\n\n" + await response.text();
                }
            },
            importLink: function () {
                let link = window.prompt("Please insert link to import!");
                if (link) {
                    let urlToParse;
                    if (link.indexOf("/fhir/") != null) {
                        urlToParse = link.substring(link.indexOf("/fhir/") + "/fhir/".length);
                    } else {
                        urlToParse = link;
                    }

                    let urlParams = getUrlParams(urlToParse);

                    this.limit = parseInt(urlParams.get("__limit"));

                    let columns = new ColumnsParser().parseColumns(decodeURIComponent(urlParams.get("__columns")));
                    this.$emit("update-columns", columns);

                    let url = urlToParse.split("?")[0];
                    let query = [...urlParams.entries()] //TODO: Improve this somehow
                        .filter(it => it[0] != "__columns" && it[0] != "__limit")
                        .map(it => it[0] + "=" + it[1])
                        .join("&");
                    (<any>window).searchEditor.setValue(url + "?" + query);
                }

            },
            copyToClipboard: function (str: string) {
                let stringToCopy = window.location.host + str;

                const el = document.createElement('textarea');
                el.value = stringToCopy;
                el.setAttribute('readonly', '');
                el.style.position = 'absolute';
                el.style.left = '-9999px';
                document.body.appendChild(el);
                el.select();
                document.execCommand('copy');
                document.body.removeChild(el);
            },
            openUrl: function (url: string) {
                window.open(url);
            }
        },
        computed: {
            downloadUrl: function () {
                let params = `__limit=${this.limit}&__columns=${columnsToString(this.columns)}`;
                let fhirQuery = (<any>window).searchEditor.getValue();
                if (fhirQuery.endsWith("?")) {
                    return "/fhir/" + fhirQuery + params;
                } else if (fhirQuery.includes("?")) {
                    return "/fhir/" + fhirQuery + "&" + params;
                } else {
                    return "/fhir/" + fhirQuery + "?" + params;
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
            columns: function (newData: Column[], oldData: Column[]) {
                this.loadTableData();
            }
        }
    }
</script>