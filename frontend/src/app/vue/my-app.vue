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
                               ref="content"/>
            </div>
        </div>
        <DialogColumn :data="dialog.data" :title="dialog.title" :visible="dialog.visible"
                      @clicked-abort="handleDialogAbort" @clicked-okay="handleDialogSubmit"/>
        <DialogQuestionnaireTs @start-request="handleRequest" @update-columns="updateColumns" @update-url="updateUrl"/>
        <DialogResource :fhirVersion="fhirVersion" @update-columns="updateColumns"/>
        <DialogCheatSheet/>
        <DialogAbout/>
        <DialogQueryLoad @import-link="importLink"/>
        <DialogQuerySave/>
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
    import DialogAbout from './dialog-about.vue';
    import DialogCheatSheet from "./dialog-cheat-sheet.vue";
    import {ColumnsParser} from "../column-parser-antlr";
    // import * as $ from ''

    // (<any>$("#sidebar")).resizable({handles: "e"});

    export function columnsToString(columns: Column[]) {
        function escape(str: string) {
            return str.replace(",", "\\,").replace("\\", "\\:")
        }

        return columns.map((it: Column) => {
            let name = it.name.replace(":", "\\:").replace("@", "\\@");
            let type = it.type.replace(":", "\\:");
            if (it.subColumns) {
                type += "(" + it.subColumns.map(it => escape(it.name) + ":" + escape(it.expression)) + ")";
            }
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

    type ParsedUrl = { limit: number, url: string, columns: Column[] };

    export function parseLink(link: string): ParsedUrl {
        let urlToParse: string;
        if (link.indexOf("/fhir/") != null) {
            urlToParse = link.substring(link.indexOf("/fhir/") + "/fhir/".length);
        } else {
            urlToParse = link;
        }

        let urlParams = getUrlParams(decodeURI(urlToParse));

        let limit = parseInt(urlParams.get("__limit"));

        let columns = new ColumnsParser().parseColumns(decodeURIComponent(urlParams.get("__columns")));

        let url = urlToParse.split("?")[0];
        let query = [...urlParams.entries()] //TODO: Improve this somehow
            .filter(it => it[0] != "__columns" && it[0] != "__limit")
            .map(it => it[0] + "=" + it[1])
            .join("&");
        return {url: url + "?" + query, columns, limit}
    }

    im
    export type Column = { name: string, type: string, expression: string, subColumns?: SubColumn[] }
    export type SubColumn = { name: string, expression: string };

    type DialogConfig = {
        visible: boolean, title: string, mode: "add" | "edit", idx: number, data: Column
    };

    export default {
        name: "MyApp",
        data: function (): { limit: number, columns: any[], rawData: string, endpointUrl: string, dialog: DialogConfig, fhirQuery: string, fhirVersion: "r4" | "stu3" } {
            return {
                columns: [{name: "id", type: 'join(" ")', expression: "getIdPart(Patient.id)"},
                    {
                        name: "ssn",
                        type: 'join(" ")',
                        expression: "Patient.identifier.where(system='http://hl7.org/fhir/sid/us-ssn').value"
                    }, {
                        name: "name", type: "explodeWide", expression: "Patient.name", subColumns: [
                            {name: "$disc", "expression": "$this.use"},
                            {name: "given", "expression": "$this.given[0]"},
                            {name: "family", "expression": "$this.family"},
                        ]
                    }, {
                        name: "managingOrganization",
                        type: 'join(" ")',
                        expression: "Patient.managingOrganization.resolve().name"
                    },
                ],
                rawData: null,
                endpointUrl: "http://url/to/fhir/endpoint",
                fhirVersion: "r4",
                fhirQuery: "",
                dialog: {
                    visible: false,
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
            DialogQueryLoad,
            DialogQuerySave,
        },
        methods: {
            handleAddColumn: function () {
                this.dialog.visible = true;
                this.dialog.title = "Add Column...";
                this.dialog.mode = "add";
                this.dialog.data = {
                    name: "",
                    type: 'join(", ")',
                    expression: ""
                }
            },
            handleEditColumn: function (idx: number) {
                this.dialog.visible = true;
                this.dialog.title = "Edit Column...";
                this.dialog.mode = "edit";
                this.dialog.idx = idx;
                this.dialog.data = {
                    name: this.columns[idx].name,
                    type: this.columns[idx].type,
                    expression: this.columns[idx].expression,
                    subColumns: this.columns[idx].subColumns
                }
            },
            handleDialogAbort: function () {
                this.dialog.visible = false;
            },
            handleDialogSubmit: function (data: Column) {
                this.dialog.visible = false;
                if (this.dialog.mode === "add") {
                    this.columns.push(data);
                } else {
                    this.columns.splice(this.dialog.idx, 1, data);
                }
            },
            handleRequest: function () {
                this.$refs.content.loadBundle()
            },
            updateColumns: function (columns: Column[], replace: Boolean) {
                if (replace) {
                    this.columns = columns;
                } else {
                    for (let column of columns) {
                        this.columns.push(column);
                    }
                }
            },
            updateUrl: function (url: string) {
                this.$refs.searchbar.setQueryUrl(url);
            },
            updateLimit: function (newLimit: number) {
                this.limit = newLimit;
            },
            importLink: function (link: string) {
                (<any>window).searchEditor.setValue(parseLink(link).url);
                this.$emit("update-columns", parseLink(link).columns, true);
                this.limit = parseLink(link).limit;
            },
            getDownloadUrl: function () {
                let params = encodeURI(`__limit=${this.limit}&__columns=${columnsToString(this.columns)}`);
                let fhirQuery = (<any>window).searchEditor?.getValue() ?? "";
                if (fhirQuery.endsWith("?")) {
                    return "/fhir/" + fhirQuery + params;
                } else if (fhirQuery.includes("?")) {
                    return "/fhir/" + fhirQuery + "&" + params;
                } else {
                    return "/fhir/" + fhirQuery + "?" + params;
                }

            }
        },
        computed: {},
        mounted: function () {
            fetch("/info")
                .then(res => res.json())
                .then(res => {
                    this.endpointUrl = res.server + "/";
                    this.fhirVersion = res.version;
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
                    console.log("dragging", e);
                    document.getElementById("sidebar").style.width = e.pageX + "px";
                }
            });
            window.addEventListener("mouseup", function (e: MouseEvent) {
                if (dragging) {
                    // e.preventDefault();
                    console.log("dragend", e);
                    document.body.style.cursor = null;
                    dragging = false;
                }
            });
        }
    }
</script>