<template>
    <div id="columnsView">
        <h6 class="sidebar-heading d-flex justify-content-between align-items-center px-3 mt-4 mb-1 text-muted">
            <span>Columns</span>
            <a class="d-flex align-items-center text-muted" href="#"
               v-on:click="$emit('addColumn')">
                <svg class="feather feather-plus-circle" fill="none" height="24"
                     stroke="currentColor"
                     stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                     viewBox="0 0 24 24"
                     width="24" xmlns="http://www.w3.org/2000/svg">
                    <line x1="12" x2="12" y1="0" y2="24"/>
                    <line x1="0" x2="24" y1="12" y2="12"/>
                </svg>
            </a>
        </h6>
        <ul class="nav flex-column mb-2">
            <li class="nav-item d-flex justify-content-between align-items-center"
                v-for="(column, idx) in columns">
                <div class="container-fluid">
                    <div class="row align-items-center">
                        <a class="nav-link col col-10" href="#" v-on:click="$emit('editColumn', idx)">
                            <div class="">
                                <span>{{column.name}}</span><span class="text-muted">@{{column.type}}</span>
                                <br>
                                <span class="text-muted text-truncate d-inline-block" style="max-width: 350px;">{{column.expression}}</span>
                            </div>
                        </a>

                        <div class="col col-2">
                            <a class="d-flex align-items-center text-muted" href="#"
                               v-on:click="removeColumn(idx)">
                                <svg class="feather feather-plus-circle" fill="none" height="24"
                                     stroke="currentColor"
                                     stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                     viewBox="0 0 448 512"
                                     width="24" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M432 32H312l-9.4-18.7A24 24 0 0 0 281.1 0H166.8a23.72 23.72 0 0 0-21.4 13.3L136 32H16A16 16 0 0 0 0 48v32a16 16 0 0 0 16 16h416a16 16 0 0 0 16-16V48a16 16 0 0 0-16-16zM53.2 467a48 48 0 0 0 47.9 45h245.8a48 48 0 0 0 47.9-45L416 128H32z"
                                          fill="currentColor"></path>
                                </svg>
                            </a></div>
                    </div>
                </div>
            </li>
        </ul>
    </div>
</template>

<script lang="ts">
    export default {
        name: "ColumnsView",
        props: ['columns', 'rawData'],
        methods: {
            removeColumn: function (index: number) {
                this.columns.splice(index, 1)
            }
        }
    }
</script>