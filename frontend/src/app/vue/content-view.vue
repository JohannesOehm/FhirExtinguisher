<template>
    <div>
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pb-2 mb-3 border-bottom">
            <h1 class="h2">Results</h1>
            <div class="btn-toolbar mb-2 mb-md-0">
                <div class="btn-group mr-2">
                    <button class="btn btn-sm btn-outline-secondary" v-bind:class="{ active: showRaw }"
                            v-on:click="toggleRaw">Raw View
                    </button>
                </div>
                <div class="btn-group mr-2">
                    <button class="btn btn-sm btn-outline-secondary">Copy Link</button>
                    <button class="btn btn-sm btn-outline-secondary">Download</button>
                </div>
                <button class="btn btn-sm btn-outline-secondary" v-on:click="editLimit">Download Limit:
                    {{limit}}
                </button>
            </div>
        </div>
        <div class="table-responsive" v-if="!showRaw">
            <table class="table table-striped table-sm" v-if="tableData != null">
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
            </table>
            <div v-else>
                Tabledata is not available.
            </div>
        </div>
        <div id="rawView" v-else>
            <div v-html="rawDataWithHighlighting">
            </div>
        </div>

    </div>
</template>

<script type="ts">
    import * as CSV from '../csv.js'
    //
    // type TableData = { records: string[][], fields: string[], metadata: any };

    export default {
        name: "ContentView",
        data: function () {
            return {
                showRaw: true,
                tableData: null,
                rawDataWithHighlighting: null
            }
        },
        props: ['limit', 'rawData'],
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
                    .then(it => this.rawDataWithHighlighting = it);
            },
            loadTableData: function () {
                let params = `__limit=${this.limit}&__columns=${columnsToString(columnsListVue.$data.columns)}`;
                fetch("/fhir/?" + params, {method: 'POST', body: this.rawData})
                    .then(res => res.text())
                    .then(csvString => {
                        CSV.fetch({
                            data: csvString
                        }).done(it => this.tableData = it)
                    });
            }
        },
        watch: {
            rawData: function (newData, oldData) {
                this.rawDataWithHighlighting = "Loading Highlighter...";
                this.reEvaluateHighlighting();
                this.tableData = null;
                this.loadTableData();
            }
        }
    }
</script>