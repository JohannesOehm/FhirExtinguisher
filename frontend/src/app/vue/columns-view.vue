<template>
    <div id="columnsView">
        <h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">
            <span>Columns</span>
            <div class="d-flex align-items-center" style="display: inline;">
                <span class="text-muted">IMPORT</span>&nbsp;
                <a class="text-muted" href="#" title="Import default settings for a specific resource type"
                   v-b-modal.modal-resource>
                    RESOURCE...
                    <!--                    <img src="resource.svg" width="16" height="16">-->
                </a>&nbsp;
                <a class="text-muted" href="#" title="Make QuestionaireResponses flat" v-b-modal.modal-questionnaire>
                    QUESTIONNAIRE...
                    <!--                    <img src="questionnaire.svg" width="16" height="16">-->

                </a>&nbsp;&nbsp;&nbsp;&nbsp;
                <a class="text-muted" href="#"
                   title="Add Column..." v-on:click="$emit('addColumn')">
                    <svg class="feather feather-plus-circle" fill="none" height="30" width="30"
                         stroke="currentColor"
                         stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                         viewBox="0 0 24 24"
                         xmlns="http://www.w3.org/2000/svg">
                        <line x1="12" x2="12" y1="0" y2="24"/>
                        <line x1="0" x2="24" y1="12" y2="12"/>
                    </svg>
                </a>
            </div>
        </h6>
        <div class="container-fluid">
            <!--            <ul class="nav flex-column mb-2">-->
            <draggable :list="columns">
                <div class="nav-item d-flex justify-content-between align-items-center" style="padding: 10px;"
                     v-for="(column, idx) in columns">
                    <a class="" style="flex: 1 1 auto;" href="#" v-on:click="$emit('editColumn', idx)">
                        <div class="">
                            <span style="color:black;">{{column.name}}</span><span
                                class="text-muted">@{{column.type}}</span><span v-if="column.subColumns"
                                                                                class="text-muted">
                                ({{column.subColumns.map(it => it.name).join(", ")}})
                            </span>
                            <br>
                            <span class="text-muted text-truncate d-inline-block" style="max-width: 300px;">{{column.expression}}</span>

                        </div>
                    </a>

                    <div style="flex: 0 0 auto;">
                        <a class="nav-link d-flex align-items-center align-content-end text-muted" href="#"
                           v-on:click="removeColumn(idx)">
                            <svg class="feather" fill="none" height="24"
                                 stroke="currentColor"
                                 stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                 viewBox="0 0 448 512"
                                 width="24" xmlns="http://www.w3.org/2000/svg">
                                <path d="M432 32H312l-9.4-18.7A24 24 0 0 0 281.1 0H166.8a23.72 23.72 0 0 0-21.4 13.3L136 32H16A16 16 0 0 0 0 48v32a16 16 0 0 0 16 16h416a16 16 0 0 0 16-16V48a16 16 0 0 0-16-16zM53.2 467a48 48 0 0 0 47.9 45h245.8a48 48 0 0 0 47.9-45L416 128H32z"
                                      fill="currentColor"></path>
                            </svg>
                        </a>
                    </div>
                </div>
            </draggable>
            <!--            </ul>-->
        </div>
    </div>
</template>

<script lang="ts">
    import draggable from "vuedraggable";
    type TableData = { records: string[][], fields: string[], metadata: any };

    export default {
        name: "ColumnsView",
        props: ['columns', 'rawData'],
        methods: {
            removeColumn: function (index: number) {
                this.columns.splice(index, 1)
            }
        },
        components: {
            draggable
        }
    }
</script>