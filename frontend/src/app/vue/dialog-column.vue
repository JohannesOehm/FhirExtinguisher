<template>
    <div class="modal" id="addColumn" role="dialog" tabindex="-1">
        <div class="modal-dialog" id="addColumnForm" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">{{title}}</h5>
                    <button aria-label="Close" class="close" type="button" v-on:click="$emit('clicked-abort')">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <form>
                        <div class="form-group">
                            <label for="addColumnName">Name</label>
                            <input aria-describedby="emailHelp" class="form-control" id="addColumnName"
                                   placeholder="Enter column name" type="text" v-model="name">
                        </div>
                        <div class="form-group">
                            <label>Type</label>
                            <div class="form-control">
                                <div class="form-check form-check-inline">
                                    <input class="form-check-inline" id="join" type="radio" v-model="type"
                                           value="join"/>
                                    <label class="form-check-label" for="join">join("<input :disabled="type !== 'join'"
                                                                                            size="3" type="text"
                                                                                            v-model="joinStr"/>")</label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-inline" id="explode" type="radio" v-model="type"
                                           value="explode"/>
                                    <label class="form-check-label" for="explode">explode</label>
                                </div>
                            </div>
                            <!--                            <input class="form-control" id="addColumnType" placeholder='explode or join(" ")'-->
                            <!--                                   type="text"-->
                            <!--                                   v-model="data.type">-->
                        </div>
                        <div class="form-group">
                            <label for="addColumnExpression">FHIRPath</label>
                            <!-- div class="border" id="fhirPathEditor" style="min-height:38px;"><span
                                    onclick="loadFhirPathMonaco();">Patient.name.first().given.first()</span></div -->
                            <input class="form-control" id="addColumnExpression" type="text"
                                   v-model="data.expression"
                                   value='join(", ")'>
                            <small class="form-text">
                                <a class="text-muted" href="http://hl7.org/fhirpath/"
                                   target="_blank">Open FHIRPath Specification...</a>
                            </small>
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-primary" type="button" v-on:click="$emit('clicked-okay', getData())">Save
                        changes
                    </button>
                    <button class="btn btn-secondary" type="button" v-on:click="$emit('clicked-abort')">Abort</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import * as $ from 'jquery';

    type Column = { name: string, type: string, expression: string }

    export default {
        name: "DialogColumn",
        data: function () {
            return {
                name: "",
                type: "join",
                joinStr: ", ",
                expression: ""
            }
        },
        methods: {
            getData: function (): Column {
                return {
                    name: this.name,
                    type: this.type === "join" ? "join(\"" + this.joinStr + "\")" : this.type,
                    expression: this.expression
                }
            }
        },
        watch: {
            visible: function (newData: boolean, oldData: boolean) {
                if (newData === true) {
                    (<any>$('#addColumn')).modal('show');
                } else {
                    (<any>$('#addColumn')).modal('hide');
                }
            },
            data: function (newData: Column, oldData: Column) {
                this.name = newData.name;
                this.type = newData.type.startsWith("join") ? "join" : "explode";
                this.joinStr = newData.type.startsWith("join(\"") ? /join\(\s*"([^"])*"\s*\)/.exec(newData.type)[1] : "";
                this.expression = newData.expression;
            }
        },
        mounted: function () {
            $('#addColumn').on('hidden.bs.modal', () => {
                this.visible = false;
            });
        },
        props: ['visible', 'data', 'title']
    }
</script>