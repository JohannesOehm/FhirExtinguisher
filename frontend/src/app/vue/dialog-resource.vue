<template>
    <b-modal @ok="handleOk" @shown="handleShown" id="modal-resource" title="Resource defaults">
        <form>
            <div class="form-group">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label class="input-group-text" for="resourceName">Resource:</label>
                    </div>
                    <select class="custom-select" id="resourceName" v-model="resourceName">
                        <option v-bind:value="resourceName" v-for="resourceName in resourceNames">
                            {{ resourceName }}
                        </option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <select class="custom-select" id="fields" multiple style="min-height: 500px;" v-model="columnsSelected">
                    <option v-bind:value="columnName" v-for="columnName in columnNames">
                        {{ columnName.name }}
                    </option>
                </select>
            </div>
            <div class="form-group">
                <div class="custom-control custom-checkbox">
                    <input class="custom-control-input" id="addOrReplace" type="checkbox" v-model="replace">
                    <label class="custom-control-label" for="addOrReplace">Replace existing columns</label>
                </div>
            </div>
        </form>
    </b-modal>
</template>

<script lang="ts">
    import {ResourceSuggestionService} from '../ResourceSuggestionService'
    import {Column} from "../index";

    export default {
        name: "DialogResource",
        props: ["fhirVersion"],
        data: function (): { resourceNames: string[], resourceName: string, columnNames: Column[], columnsSelected: Column[], replace: boolean } {
            return {
                resourceNames: [],
                resourceName: null,
                columnNames: [],
                columnsSelected: [],
                replace: true
            }
        },
        methods: {
            handleOk: function () {
                this.$emit("update-columns", this.columnsSelected, this.replace);
            },
            handleChange: function (e: any) {
                if (e.target.files.length !== 0) {
                    document.getElementById("questionnaireFileLabel").innerText = e.target.files[0].name;
                } else {
                    document.getElementById("questionnaireFileLabel").innerText = "Choose file...";
                }
            },
            handleShown: function () {
                this.resourceNames = this.$options.resourceSuggestionService.getResourceNames();
                try {
                    let query = (<any>window).searchEditor.getValue();

                    let resourceName;
                    if (query.indexOf("/") !== -1) {
                        resourceName = query.substring(0, query.indexOf("/"));
                    } else if (query.indexOf("?") !== -1) {
                        resourceName = query.substring(0, query.indexOf("?"));
                    } else {
                        resourceName = query;
                    }
                    if (this.resourceNames.indexOf(resourceName) !== -1) {
                        this.resourceName = resourceName;
                    }
                } catch (e) {

                }
            }
        },
        watch: {
            resourceName: function (newData: string, oldData: string) {
                this.columnNames = this.$options.resourceSuggestionService.getResourceFields(newData);
            }
        },
        mounted: function () {
            this.$options.resourceSuggestionService = new ResourceSuggestionService(this.fhirVersion);
        }
    }
</script>