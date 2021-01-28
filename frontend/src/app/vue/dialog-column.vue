<template>
  <b-modal id="modal-column" :title="title" @ok="handleOk" size="lg">
    <div class="modal-body">
      <form>
        <div class="form-group">
          <label for="addColumnName">Name</label>
          <input aria-describedby="emailHelp" class="form-control" id="addColumnName"
                 placeholder="Enter column name" type="text" v-model="name">
        </div>
        <div class="form-group">
          <label v-b-tooltip.hover
                 title="Controls how multiple elements returned by FHIRPath expression are handled.">List
            Processing Behaviour</label>
          <div class="form-control">
            <div class="form-check form-check-inline">
              <input class="form-check-inline" id="join" type="radio" v-model="type"
                     value="join"/>
              <label class="form-check-label" for="join" v-b-tooltip.hover
                     title="concat returned elements with separator string">join("<input
                  :disabled="type !== 'join'"
                  size="3" type="text"
                  style="max-height: 25px;"
                  v-model="joinStr"/>")</label>
            </div>
            <!--                                <div class="form-check form-check-inline">-->
            <!--                                    <input class="form-check-inline" id="explode" type="radio" v-model="type"-->
            <!--                                           value="explode"/>-->
            <!--                                    <label class="form-check-label" for="explode" v-b-tooltip.hover-->
            <!--                                           title="create new row for each element returned by the FHIRPath expression">explode</label>-->
            <!--                                </div>-->
            <div class="form-check form-check-inline">
              <input class="form-check-inline" id="explodeWide" type="radio" v-model="type"
                     value="explodeWide"/>
              <label class="form-check-label" for="explodeWide" v-b-tooltip.hover
                     title="create new column(s) for each element returned by FHIRPath expression">explodeWide</label>
            </div>
            <div class="form-check form-check-inline">
              <input class="form-check-inline" id="explodeLong" type="radio" v-model="type"
                     value="explodeLong"/>
              <label class="form-check-label" for="explodeLong" v-b-tooltip.hover
                     title="create new row for each element returned by the FHIRPath expression">explodeLong</label>
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
                 v-model="expression" v-on:keyup="evaluateExpression(expression)">
          <small class="form-text">
            <span class="text-danger" v-if="error != null">{{ error }}</span>
          </small>
          <small class="form-text">
            <a class="text-muted" href="http://hl7.org/fhirpath/"
               target="_blank">Open FHIRPath Specification...</a>
          </small>
        </div>
        <div class="form-group" v-if="type==='explodeWide' || type==='explodeLong'">
          <label>Subcolumns</label>
          <div class="form-row form-group" v-if="type==='explodeWide'">
            <div class="col">
              <label for="disc" class="col-form-label col-form-label-sm">Discriminator:</label>
            </div>
            <div class="col col-9">
              <input id="disc" type="text" class="form-control" placeholder="Expression"
                     v-model="discriminator"/>
            </div>
          </div>
          <div class="form-row form-group" v-for="(subcolumn, idx) in subcolumns">
            <div class="col col-3">
              <input type="text" class="form-control" placeholder="Name"
                     v-model="subcolumn.name"/>
            </div>
            <div class="col">
              <input type="text" class="form-control" placeholder="Expression"
                     v-model="subcolumn.expression"/>
            </div>
            <div class="col col-1 text-center">
              <a class="align-items-center text-muted" href="#"
                 v-on:click="removeSubColumn(idx)">
                <svg class="feather" fill="none" height="24"
                     stroke="currentColor"
                     stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                     viewBox="0 0 448 512"
                     width="24" xmlns="http://www.w3.org/2000/svg">
                  <path
                      d="M432 32H312l-9.4-18.7A24 24 0 0 0 281.1 0H166.8a23.72 23.72 0 0 0-21.4 13.3L136 32H16A16 16 0 0 0 0 48v32a16 16 0 0 0 16 16h416a16 16 0 0 0 16-16V48a16 16 0 0 0-16-16zM53.2 467a48 48 0 0 0 47.9 45h245.8a48 48 0 0 0 47.9-45L416 128H32z"
                      fill="currentColor"></path>
                </svg>
              </a>
            </div>
          </div>
          <div class="form-group form-row">
            <a class="text-muted" href="#" title="ADD SUBCOLUMN..." v-on:click="addSubColumn">
              <svg class="feather feather-plus-circle" fill="none" height="15" width="15"
                   stroke="currentColor"
                   stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                   viewBox="0 0 24 24"
                   xmlns="http://www.w3.org/2000/svg">
                <line x1="12" x2="12" y1="0" y2="24"/>
                <line x1="0" x2="24" y1="12" y2="12"/>
              </svg>
              ADD SUBCOLUMN...
            </a>
          </div>
        </div>
      </form>
    </div>
  </b-modal>
</template>

<script lang="ts">
import * as $ from 'jquery';
import {VmColumn, VmSubColumn} from "./my-app.vue";
import * as _ from "lodash";


export default {
  name: "DialogColumn",
  data: function (): { name: string, type: string, joinStr: string, expression: string, subcolumns: VmSubColumn[], discriminator: string, error: string | null } {
    return {
      name: "",
      type: "join",
      joinStr: ", ",
      expression: "",
      subcolumns: [],
      discriminator: "%index",
      error: null
    }
  },
  methods: {
    handleOk: function () {
      this.$emit("clicked-okay", this.getData())
    },
    getData: function (): VmColumn {
      let shallowCopy = JSON.parse(JSON.stringify(this.subcolumns)); //remove Vue.js magic from object
      let subColumns: VmSubColumn[];
      if (this.type === "explodeWide") {
        subColumns = shallowCopy.concat({name: "$disc", expression: this.discriminator})
      } else if (this.type === "explodeLong") {
        subColumns = shallowCopy;
      }

      let retVal = {
        name: this.name,
        type: this.type === "join" ? "join(\"" + this.joinStr + "\")" : this.type,
        expression: this.expression,
        subColumns: subColumns
      };
      console.log(retVal);
      return retVal;
    },
    addSubColumn: function () {
      this.subcolumns.push({name: "", expression: ""})
    },
    removeSubColumn: function (index: number) {
      this.subcolumns.splice(index, 1)
    },
    evaluateExpression: _.debounce(async function (fhirpath: string): Promise<void> {
      try {
        let response = await fetch("fhirPath?expr=" + encodeURIComponent(fhirpath))
        if (response.status == 200) {
          this.error = null;
        } else if (response.status == 400) {
          this.error = await response.text();
        } else {
          this.error = response.status + " " + response.statusText;
          this.error += await response.text();
        }
      } catch (e) {
        this.error = e.toString();
      }

    }, 300)
  },
  watch: {
    visible: function (newData: boolean, oldData: boolean) {
      if (newData === true) {
        (<any>$('#addColumn')).modal('show');
      } else {
        (<any>$('#addColumn')).modal('hide');
      }
    },
    data: function (newData: VmColumn, oldData: VmColumn) {
      this.name = newData.name;
      this.type = newData.type.startsWith("join") ? "join" : newData.type;
      this.joinStr = newData.type.startsWith("join(\"") ? /join\(\s*"([^"]*)"\s*\)/.exec(newData.type)[1] : "";
      this.subcolumns = (newData.subColumns ?? []).filter(it => it.name !== "$disc");
      this.expression = newData.expression;
      let disc = (newData.subColumns ?? []).filter((it: VmSubColumn) => it.name === "$disc");
      if (disc.length != 0) {
        this.discriminator = disc[0].expression;
      } else {
        this.discriminator = "%index";
      }
    }
  },
  mounted: function () {
  },
  props: ['data', 'title']
}
</script>