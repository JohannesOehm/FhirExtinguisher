<template>
    <b-modal @ok="handleOk" @shown="handleShown" id="modal-query-load" title="Load query">
        <form>
            <div class="form-group">
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label class="input-group-text" for="resourceName">Query:</label>
                    </div>
                    <select class="custom-select" id="resourceName" v-model="query">
                        <option v-bind:value="it" v-for="it in queries">
                            {{ it.name }}
                        </option>
                    </select>
                </div>
                <!--                <div class="form-group">-->
                <!--                    <div class="custom-control custom-checkbox">-->
                <!--                        <input class="custom-control-input" id="updateSearch" type="checkbox" v-model="updateSearch">-->
                <!--                        <label class="custom-control-label" for="updateSearch">{{fhirSearch}}</label>-->
                <!--                        <input class="custom-control-input" id="updateLimit" type="checkbox" v-model="updateLimit">-->
                <!--                        <label class="custom-control-label" for="updateLimit">Limit {{limit}}</label>-->
                <!--                        <input class="custom-control-input" id="updateColumns" type="checkbox" v-model="updateColumns">-->
                <!--                        <label class="custom-control-label" for="updateColumns">Columns </label>-->
                <!--                    </div>-->
                <!--                </div>-->
            </div>
        </form>
    </b-modal>
</template>

<script lang="ts">
    import {ResourceSuggestionService} from '../ResourceSuggestionService'
    import {Column} from "../index";
    import * as CSV from "../csv";
    import {parseLink} from "./my-app.vue";

    type StoredQuery = { name: string, url: string }

    export default {
        name: "DialogQueryLoad",
        data: function (): { queries: StoredQuery[], query: StoredQuery, updateSearch: true, updateLimit: true, updateColumns: true } {
            return {
                queries: [],
                query: null
            }
        },
        methods: {
            handleOk: function () {
                if (this.query) {
                    // let update = {
                    //     url: this.updateSearch ? this.query.url : ,
                    //     limit: this.query.limit,
                    //     colums: this.query.columns,
                    // }

                    this.$emit("import-link", this.query.url);
                }
            },
            handleChange: function (e: any) {
            },
            handleShown: async function () {
                let response = await fetch("/query")
                let csvString = await response.text();
                (<any>CSV).fetch({
                    data: csvString
                }).done((it: any) => {
                    let tmp = [];
                    for (let record of it.records) {
                        tmp.push({name: record[0], url: record[1]});
                    }

                    this.queries = tmp;
                }).catch((it: any) => {
                        alert(it);
                        this.tableData = null;
                    }
                );
            }
        },
        watch: {},
        computed: {
            fhirSearch: function () {
                if (this.query) {
                    return parseLink(this.query.url).url;
                } else {
                    return "";
                }
            },
            limit: function () {
                if (this.query) {
                    return parseLink(this.query.url).limit;
                } else {
                    return 0;
                }
            },
            columns: function () {
                if (this.query) {
                    return parseLink(this.query.url).columns;
                } else {
                    return [];
                }
            }
        },
        mounted: function () {
        }
    }
</script>