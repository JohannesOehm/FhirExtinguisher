<template>
  <b-modal @abort="handleAbort" @ok="handleOk" id="modal-questionnaire" title="Import Questionnaire">
    <form>
      <div class="form-group">
        <div class="form-check">
          <input class="form-check-input" id="fileRadio" name="serverOrFile" type="radio"
                 v-model="serverOrFile" value="file">
          <label class="form-check-label" for="fileRadio">
            Import from file:
          </label>
        </div>
        <div class="custom-file">
          <input class="form-control custom-file-input" id="questionnaireFile" placeholder="" type="file"
                 v-on:change="handleChange">
          <label :class="{myDisabled: serverOrFile !== 'file'}" class="custom-file-label"
                 for="questionnaireFile" id="questionnaireFileLabel">Choose
            file...</label>
        </div>
      </div>
      <hr/>
      <div class="form-group">
        <div class="form-check">
          <input class="form-check-input" id="serverRadio" name="serverOrFile" type="radio"
                 v-model="serverOrFile" value="server">
          <label class="form-check-label" for="serverRadio">
            Select from server:
          </label>
        </div>
        <label for="questionnaireId">Select from server:</label>
        <div class="input-group">
          <div class="input-group-prepend">
            <label class="input-group-text" for="questionnaireId">Resource:</label>
          </div>
          <select :disabled="serverOrFile !== 'server'" class="custom-select" id="questionnaireId"
                  v-model="questionnaireSelected">
            <option v-bind:value="data.id" v-for="data in questionnaires">
              {{ data.id }} - {{ data.title }} {{ data.url ? " (" + data.url + ")" : "" }}
            </option>
          </select>
        </div>
      </div>
      <hr/>
      <div class="form-group">
        <div class="custom-control custom-checkbox">
          <input class="custom-control-input" id="addOrReplace" type="checkbox" v-model="replace">
          <label class="custom-control-label" for="addOrReplace">Replace existing columns</label>
        </div>
        <div class="custom-control custom-checkbox">
          <input class="custom-control-input" id="changeUrl" type="checkbox" v-model="changeQuery">
          <label class="custom-control-label" for="changeUrl">Update query URL</label>
        </div>
        <div class="custom-control custom-checkbox" style="margin-left: 25px;">
          <input :disabled="!changeQuery" class="custom-control-input" id="execute" type="checkbox"
                 v-model="execute">
          <label class="custom-control-label" for="execute">Execute immediately</label>
        </div>
      </div>
    </form>
  </b-modal>
</template>


<script lang="ts">
import {Column} from "../index";

type QuestionnaireSummary = {
  id: string,
  url: string,
  title: string,
  fullUrl: string
}

function getItems(items: any[], path: string[]): Column[] {
  let result: Column[] = [];
  for (let item of items) {
    if (item.type && item.type !== "group" && item.type !== "display") {
      let fullPath = path.concat(item.linkId);
      result.push({
        name: "items/" + fullPath.join("/"),
        type: 'join(", ")',
        expression: "QuestionnaireResponse." + (fullPath.map(it => `item.where(linkId='${it}')`).join(".")) + ".answer.value"
      });
    }
    if (item.item) {
      let results2 = getItems(item.item, path.concat(item.linkId));
      for (let result2 of results2) { //TODO: addAll()
        result.push(result2);
      }
    }
  }
  return result;
}

export default {
  name: "DialogQuestionnaireTs",
  data: function (): { questionnaires: QuestionnaireSummary[], questionnaireSelected: string, replace: boolean, changeQuery: boolean, execute: boolean, serverOrFile: "file" | "server" } {
    return {
      questionnaires: [],
      questionnaireSelected: null,
      replace: true,
      changeQuery: true,
      execute: true,
      serverOrFile: "file"
    };
  },
  methods: {
    handleOk: function () {
      if (this.serverOrFile === "file") {
        let input = <HTMLInputElement>document.getElementById("questionnaireFile");
        if (input.files.length === 0) {
          alert("Please select a file!");
          return;
        }
        let reader = new FileReader();
        reader.onload = (e: any) => {
          let text = e.target.result;
          console.log(text);
          let parsedQuestionnaire = JSON.parse(text);
          this.convertQuestionnaire(parsedQuestionnaire);
        };
        reader.onerror = function () {
          alert("Error while loading file.");
        };
        reader.readAsText(input.files[0]);
      } else {
        let questionnaireId = (<HTMLInputElement>document.getElementById("questionnaireId")).value;
        fetch("redirect/Questionnaire/" + questionnaireId + "?_format=json", {headers: {"Accept": "application/fhir+json"}})
            .then(res => res.json())
            .then(res => {
              console.log(res);
              this.convertQuestionnaire(res);
            })
            .catch((e: any) => {
              alert("Could not retrieve resource from server: " + e);
              console.log(e);
            });

      }
    },
    handleChange: function (e: any) {
      if (e.target.files.length !== 0) {
        document.getElementById("questionnaireFileLabel").innerText = e.target.files[0].name;
      } else {
        document.getElementById("questionnaireFileLabel").innerText = "Choose file...";
      }
    },
    handleAbort: function () {
      this.questionnaireSelected = null;
      (<HTMLInputElement>document.getElementById("questionnaireFile")).value = null;
    },
    convertQuestionnaire: function (questionnaire: any) {
      let result: Column[] = getItems(questionnaire.item, []);
      result.unshift(
          {name: "id", type: 'join("")', expression: "getIdPart(QuestionnaireResponse.id)"},
          {name: "basedOn", type: 'join(" ")', expression: "QuestionnaireResponse.basedOn"},
          {name: "status", type: 'join(" ")', expression: "QuestionnaireResponse.status"},
          {name: "subject", type: 'join(" ")', expression: "QuestionnaireResponse.subject"},
          {name: "authored", type: 'join(" ")', expression: "QuestionnaireResponse.authored"},
      );

      this.$emit("update-columns", result, this.replace);
      if (this.changeQuery) {
        let url = `QuestionnaireResponse?questionnaire=${questionnaire.id}`;
        this.$emit("update-url", url);
        if (this.execute) {
          this.$emit("start-request", url);
        }
      }
    }
  },
  mounted: function () {
    fetch("redirect/Questionnaire?_summary=true")
        .then(res => res.json())
        .then(bundle => {
          if (bundle.total === 0) { //res.entry might be undefined in this case just use a empty list
            this.questionnaires = [];
          } else {
            this.questionnaires = bundle.entry.map((it: any) => ({
              id: it.resource.id,
              url: it.resource.url,
              title: it.resource.title,
              fullUrl: it.fullUrl
            }));
          }
        });
  }
}

</script>