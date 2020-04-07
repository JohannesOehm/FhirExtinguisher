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
                    <button @click="openUrl(downloadUrl)" class="btn btn-sm btn-outline-secondary" type="submit">
                        Download
                    </button>
                </div>
                <button class="btn btn-sm btn-outline-secondary" v-on:click="editLimit">Download Limit:
                    {{limit}}
                </button>
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
                    This is only a preview of the first Bundle returned by the server. Click download for the full data.
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
                Please enter a valid FHIR Query URL and press GET!
            </div>
        </div>

    </div>
</template>

<script lang="ts">
    import * as CSV from '../csv.js';
    import * as monaco from "monaco-editor";


    type TableData = { records: string[][], fields: string[], metadata: any };
    type Column = { name: string, type: string, expression: string };


    function columnsToString(columns: Column[]) {
        return columns.map((it: Column) => `${it.name}@${it.type.replace(",", "%2C").replace(":", "%3A")}:${it.expression}`).join(",");
    }


    export default {
        name: "ContentView",
        data: function (): { showRaw: boolean, tableData: TableData, tableError: string, rawDataWithHighlighting: string, limit: number } {
            return {
                showRaw: true,
                tableData: null,
                tableError: null,
                rawDataWithHighlighting: null,
                limit: 50
            }
        },
        props: ['rawData', 'columns', 'fhirQuery'],
        methods: {
            toggleRaw: function () {
                this.showRaw = !this.showRaw;
            },
            editLimit: function () {
                let newLimit = window.prompt("Please enter the new limit parameter:", this.limit);
                let newValue = parseInt(newLimit);
                if (!isNaN(newValue)) {
                    this.limit = newValue;
                }
            },
            reEvaluateHighlighting: function () {
                monaco.editor.colorize(this.rawData, 'json', {})
                    .then((it: string) => this.rawDataWithHighlighting = it);
            },
            loadTableData: async function () {
                let params = `__limit=${this.limit}&__columns=${columnsToString(this.columns)}`;
                console.log("Hallo Welt");
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
                    this.tableError = await response.text();
                    console.log(response);
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
                if (this.fhirQuery.endsWith("?")) {
                    return "/fhir/" + this.fhirQuery + params;
                } else if (this.fhirQuery.includes("?")) {
                    return "/fhir/" + this.fhirQuery + "&" + params;
                } else {
                    return "/fhir/" + this.fhirQuery + "?" + params;
                }

            }
        },
        watch: {
            rawData: function (newData: string, oldData: string) {
                this.rawDataWithHighlighting = "Loading Highlighter...";
                this.reEvaluateHighlighting();
                this.tableData = null;
                this.loadTableData();
            },
            columns: function (newData: Column[], oldData: Column[]) {
                this.loadTableData();
            }
        }
    }
</script>