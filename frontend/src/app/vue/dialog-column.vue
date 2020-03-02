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
                                   placeholder="Enter column name" type="text" v-model="data.name">
                        </div>
                        <div class="form-group">
                            <label for="addColumnType">Type</label>
                            <input class="form-control" id="addColumnType" placeholder='explode or join(" ")'
                                   type="text"
                                   v-model="data.type">
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
                    <button class="btn btn-primary" type="button" v-on:click="$emit('clicked-okay', data)">Save
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
            return {}
        },
        methods: {},
        watch: {
            visible: function (newData: boolean, oldData: boolean) {
                if (newData === true) {
                    (<any>$('#addColumn')).modal('show');
                } else {
                    (<any>$('#addColumn')).modal('hide');
                }
            }
        },
        props: ['visible', 'data', 'title']
    }
</script>