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
                <MyContentView :columns="columns" :fhir-query="fhirQuery" :rawData="rawData"/>
            </div>
        </div>
        <DialogColumn :data="dialog.data" :title="dialog.title" :visible="dialog.visible"
                      @clicked-abort="handleDialogAbort" @clicked-okay="handleDialogSubmit"/>
        <DialogQuestionnaireTs @start-request="handleRequest" @update-columns="updateColumns" @update-url="updateUrl"/>
        <DialogResource :fhirVersion="fhirVersion" @update-columns="updateColumns"/>
        <DialogAbout/>
    </div>
</template>


<script lang="ts">
    import Searchbar from './searchbar.vue';
    import ColumnsView from './columns-view.vue';
    import DialogColumn from './dialog-column.vue';
    import MyContentView from './my-content-view.vue';
    import DialogQuestionnaireTs from './dialog-questionnaire-ts.vue';
    import DialogResource from './dialog-resource.vue';
    import DialogAbout from './dialog-about.vue';

    import * as $ from 'jquery';
    // import * as $ from ''

    // (<any>$("#sidebar")).resizable({handles: "e"});


    type Column = { name: string, type: string, expression: string };
    type DialogConfig = {
        visible: boolean, title: string, mode: "add" | "edit", idx: number, data: Column
    };

    export default {
        name: "MyApp",
        data: function (): { columns: any[], rawData: string, endpointUrl: string, dialog: DialogConfig, fhirQuery: string, fhirVersion: "r4" | "stu3" } {
            return {
                columns: [{name: "id", type: 'join(" ")', expression: "getIdPart(Patient.id)"},
                    {
                        name: "ssn",
                        type: 'join(" ")',
                        expression: "Patient.identifier.where(system='http://hl7.org/fhir/sid/us-ssn').value"
                    },
                    {name: "firstnames", type: 'join(" ")', expression: "Patient.name[0].given"},
                    {name: "lastname", type: 'join(" ")', expression: "Patient.name[0].family"},
                    {
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
                }
            }
        },
        components: {
            DialogColumn,
            Searchbar,
            ColumnsView,
            MyContentView,
            DialogQuestionnaireTs,
            DialogResource,
            DialogAbout
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
                    expression: this.columns[idx].expression
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
            handleRequest: function (url: string) {
                this.rawData = "Loading...";
                this.fhirQuery = url;
                fetch("/redirect/" + url)
                    .then(res => res.text())
                    .then(res => {
                        this.rawData = res;
                    });
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
            }
        },
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