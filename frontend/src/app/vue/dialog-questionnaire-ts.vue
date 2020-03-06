<template>
    <b-modal @abort="handleAbort" @ok="handleOk" id="modal-questionnaire" title="Import Questionnaire">
        <form>
            <div class="form-group">
                <label for="questionnaireFile">Import from file:</label>
                <div class="custom-file">
                    <input class="form-control custom-file-input" id="questionnaireFile" placeholder="" type="file"
                           v-on:change="handleChange">
                    <label class="custom-file-label" for="questionnaireFile" id="questionnaireFileLabel">Choose
                        file...</label>
                </div>
            </div>
            <hr/>
            <div class="form-group">
                <label for="questionnaireId">Select from server:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <label class="input-group-text" for="questionnaireId">Resource:</label>
                    </div>
                    <select class="custom-select" id="questionnaireId" v-model="questionnaireSelected">
                        <option v-bind:value="data.id" v-for="data in questionnaires">
                            {{ data.id }} - {{data.title}} {{data.url ? " ("+data.url+")" : ""}}
                        </option>
                    </select>
                </div>
            </div>
        </form>
    </b-modal>
</template>


<script lang="ts">

    type Foo = { bar: string };

    type QuestionnaireSummary = {
        id: string,
        url: string,
        title: string,
        fullUrl: string
    }


    export default {
        name: "DialogQuestionnaireTs",
        data: function (): { questionnaires: QuestionnaireSummary[], questionnaireSelected: string } {
            return {
                questionnaires: [],
                questionnaireSelected: null
            };
        },
        methods: {
            handleOk: function () {
                let input = <HTMLInputElement>document.getElementById("questionnaireFile");
                if (input.files.length !== 0) {
                    let reader = new FileReader();
                    reader.onload = function () {
                        let text = reader.result;
                        console.log(text);
                    };
                    reader.onerror = function () {
                        alert("Error while loading file.");
                    };
                    reader.readAsText(input.files[0]);
                } else {
                    let questionnaireId = (<HTMLInputElement>document.getElementById("questionnaireId")).value;
                    fetch("/redirect/Questionnaire/" + questionnaireId + "?_format=json", {headers: {"Accept": "application/fhir+json"}})
                        .then(res => res.json())
                        .then(res => console.log(res))
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
            }
        },
        mounted: function () {
            fetch("/redirect/Questionnaire?_summary=true")
                .then(res => res.json())
                .then(res => {
                    this.questionnaires = res.entry.map((it: any) => ({
                        id: it.resource.id,
                        url: it.resource.url,
                        title: it.resource.title,
                        fullUrl: it.fullUrl
                    }));
                });
        }
    }

</script>