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
        </form>
    </b-modal>
</template>

<script type="ts">
    import {ResourceSuggestionService} from '../ResourceSuggestionService'

    export default {
        name: "DialogResource",
        props: ["fhirVersion"],
        data: function () {
            return {
                resourceNames: ["Hallo", "Welt", "Ich", "bin", "noch", "nicht", "fertig"],
                resourceName: "Hallo",
                columnNames: [],
                columnsSelected: []
            }
        },
        methods: {
            handleOk: function () {
                this.$emit("update-columns", this.columnsSelected);
            },
            handleChange: function (e) {
                if (e.target.files.length !== 0) {
                    document.getElementById("questionnaireFileLabel").innerText = e.target.files[0].name;
                } else {
                    document.getElementById("questionnaireFileLabel").innerText = "Choose file...";
                }
            },
            handleShown: function () {
                this.resourceNames = this.$options.resourceSuggestionService.getResourceNames();
                console.log(this.resourceNames);
            }
        },
        watch: {
            resourceName: function (newData, oldData) {
                this.columnNames = this.$options.resourceSuggestionService.getResourceFields(newData);
                console.log(this.columnNames);
            }
        },
        mounted: function () {
            this.$options.resourceSuggestionService = new ResourceSuggestionService(this.fhirVersion);
        }
    }
</script>