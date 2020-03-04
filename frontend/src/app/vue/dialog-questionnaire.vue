<template>
    <b-modal @ok="handleOk" id="modal-questionnaire" title="Import Questionnaire">
        <form>
            <div class="form-group">
                <label for="questionnaireFile">Import file:</label>
                <div class="custom-file">
                    <input class="form-control custom-file-input" id="questionnaireFile" placeholder="" type="file"
                           v-on:change="handleChange">
                    <label class="custom-file-label" for="questionnaireFile" id="questionnaireFileLabel">Choose
                        file...</label>
                </div>
            </div>
        </form>
    </b-modal>
</template>

<script type="ts">
    export default {
        name: "DialogQuestionnaire",
        methods: {
            handleOk: function () {
                let input = document.getElementById("questionnaireFile");

                let reader = new FileReader();
                reader.onload = function () {
                    let text = reader.result;
                    console.log(text);
                };
                reader.readAsText(input.files[0]);
            },
            handleChange: function (e) {
                if (e.target.files.length !== 0) {
                    document.getElementById("questionnaireFileLabel").innerText = e.target.files[0].name;
                } else {
                    document.getElementById("questionnaireFileLabel").innerText = "Choose file...";
                }
            }
        }
    }
</script>