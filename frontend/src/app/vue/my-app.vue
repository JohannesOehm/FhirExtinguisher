<template>
    <div id="app">
        <Searchbar :endpoint-url="endpointUrl" @startRequest="handleRequest"/>
        <div class="container-fluid">
            <div class="row">
                <div class="col-md-2 d-none d-md-block bg-light sidebar">
                    <div class="sidebar-sticky">
                        <ColumnsView :columns="columns" @addColumn="handleAddColumn"
                                     @editColumn="handleEditColumn"/>
                    </div>
                </div>
                <div class="col-md-9 ml-sm-auto col-lg-10 pt-3 px-4" id="tableOrRaw" role="main">
                    <ContentView :columns="columns" :fhir-query="fhirQuery" :limit="limit" :rawData="rawData"/>
                </div>
            </div>
        </div>
        <DialogColumn :data="dialog.data" :title="dialog.title" :visible="dialog.visible"
                      @clicked-abort="handleDialogAbort" @clicked-okay="handleDialogSubmit"/>
    </div>
</template>


<script lang="ts">
    import Searchbar from './searchbar.vue';
    import ColumnsView from './columns-view.vue';
    import DialogColumn from './dialog-column.vue';
    import ContentView from './content-view.vue';

    type Column = { name: string, type: string, expression: string };
    type DialogConfig = {
        visible: boolean, title: string, mode: "add" | "edit", idx: number, data: Column
    };

    export default {
        name: "MyApp",
        data: function (): { columns: any[], limit: number, rawData: string, endpointUrl: string, dialog: DialogConfig, fhirQuery: string } {
            return {
                columns: [{name: "id", type: 'join(" ")', expression: "getIdPart(Patient.id)"},
                    {
                        name: "ssn",
                        type: 'join(" ")',
                        expression: "Patient.identifier.where(system='http://hl7.org/fhir/sid/us-ssn').value"
                    },
                    {name: "name.first", type: 'join(" ")', expression: "Patient.name[0].given"},
                    {name: "name.last", type: 'join(" ")', expression: "Patient.name[0].family"}
                ],
                limit: 50,
                rawData: null,
                endpointUrl: "http://url/to/fhir/endpoint",
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
        components: {DialogColumn, Searchbar, ColumnsView, ContentView},
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
                this.fhirQuery = url;
                fetch("/redirect/" + url)
                    .then(res => res.text())
                    .then(res => {
                        this.rawData = res;
                        console.log(res);
                    });
            }
        },
        mounted: function () {
            fetch("/info")
                .then(res => res.json())
                .then(res => {
                    this.endpointUrl = res.server + "/";
                });

        }
    }
</script>