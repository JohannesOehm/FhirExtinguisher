import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap-vue/dist/bootstrap-vue.css'
import * as monaco from 'monaco-editor';
import * as $ from 'jquery';

import Vue from 'vue';
import {URLCompletionItemProvider} from "./url-completionitemprovider";
import BootstrapVue, {IconsPlugin, ModalPlugin} from "bootstrap-vue";

Vue.use(BootstrapVue);
Vue.use(IconsPlugin);
Vue.use(ModalPlugin);

type Column = { name: string, type: string, expression: string }

let myModel: { columns: Column[] } = {
    columns: [
        {name: "name", type: "explode", expression: "Patient.name"}
    ]
};

Vue.config.errorHandler = (err, vm, info) => {
    console.log({err, vm, info});
};

let columnsListVue = new Vue({
    data: myModel,
    el: "#columnsView",
    methods: {
        removeColumn: function (index: number) {
            myModel.columns.splice(index, 1)
        },
        editColumn: function (index: number) {
            let target = columnsModalVue.$data;
            let src = myModel.columns[index];
            target.name = src.name;
            target.type = src.type;
            target.expression = src.expression;
            target.$modalType = "edit";
            target.$editIdx = index;
            (<any>$('#addColumn')).modal('show');
        }
    }
});
let columnsModalData: Column & { $modalType: "add" | "edit", $editIdx: number } = {
    name: null,
    type: null,
    expression: null,
    $modalType: "add",
    $editIdx: 0
};
let columnsModalVue = new Vue({
    data: columnsModalData,
    el: '#addColumnForm'
});

(<any>window).myModel = myModel;


(<any>window).newColumn = function () {
    (<any>$('#addColumn')).modal('show');
    $('#addColumn').on('shown.bs.modal', function (e) {
        $("#addColumnName").focus();
        //TODO Load Monaco?
    });

    columnsModalData.name = null;
    columnsModalData.type = null;
    columnsModalData.expression = null;
    columnsModalData.$modalType = "add";
};

(<any>window).handleAddColumn = function () {
    (<any>$('#addColumn')).modal('hide');
    let result = {
        name: columnsModalData.name,
        type: columnsModalData.type,
        expression: columnsModalData.expression,
    };
    if (columnsModalData.$modalType === "add") {
        myModel.columns.push(result);
    } else {
        Vue.set(myModel.columns, columnsModalData.$editIdx, result);
    }

};

(<any>window).loadFhirPathMonaco = function () {
    monaco.languages.register({id: 'fhirpath'});


    let element = document.getElementById("fhirPathEditor");
    let value = element.innerText;
    element.innerHTML = "";
    let fhirPathEditor = monaco.editor.create(element, {
        value: value,
        language: "fhirpath",
        minimap: {
            enabled: false
        },
        lineNumbers: 'off',
        glyphMargin: false,
        folding: false,
        scrollbar: {
            vertical: "auto",
            horizontal: "auto"
        }
        // lineDecorationsWidth: 0,
        // lineNumbersMinChars: 0
    });
    (<any>window).fhirPathEditor = fhirPathEditor;
    let myBinding = fhirPathEditor.addCommand(monaco.KeyCode.Enter,
        function () {
            console.log("Enter was suppressed!")
        });
};


(function () {
    monaco.languages.register({id: 'url'});

    monaco.languages.registerCompletionItemProvider("url", new URLCompletionItemProvider());

    let element = document.getElementById("searchbar");
    element.innerHTML = "";
    let searchEditor = monaco.editor.create(element, {
        value: "Patient?",
        language: "url",
        minimap: {
            enabled: false
        },
        lineNumbers: 'off',
        glyphMargin: false,
        folding: false,
        scrollbar: {
            vertical: "auto",
            horizontal: "auto"
        },
        fontSize: 16,
        theme: "vscode-dark"
        // lineDecorationsWidth: 0,
        // lineNumbersMinChars: 0
    });
    (<any>window).searchEditor = searchEditor;
    var myBinding = searchEditor.addCommand(monaco.KeyCode.Enter,
        function () {
            console.log("Enter was suppressed!")
        });


    // monaco.languages.setTokensProvider("url", )

})();

