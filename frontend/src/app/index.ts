import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap-vue/dist/bootstrap-vue.css'
import * as monaco from 'monaco-editor';
import {editor} from 'monaco-editor';
import * as $ from 'jquery';

import Vue from 'vue';
import {URLCompletionItemProvider} from "./url-completionitemprovider";
import BootstrapVue, {IconsPlugin, ModalPlugin} from "bootstrap-vue";

import * as CSV from './csv.js'
import IStandaloneCodeEditor = editor.IStandaloneCodeEditor;

Vue.use(BootstrapVue);
Vue.use(IconsPlugin);
Vue.use(ModalPlugin);

type Column = { name: string, type: string, expression: string }

let myModel: { columns: Column[] } = {
    columns: [
        {name: "id", type: 'join(" ")', expression: "Patient.id"},
        {
            name: "ssn",
            type: 'join(" ")',
            expression: "Patient.identifier.where(system='http://hl7.org/fhir/sid/us-ssn').value"
        },
        {name: "name", type: 'join(" ")', expression: "Patient.name[0].given"},

    ]
};

Vue.config.errorHandler = (err, vm, info) => {
    console.log({err, vm, info});
};

fetch("/info")
    .then(res => res.json())
    .then(res => {
        document.getElementById("fhirServerUrl").innerText = res.server + "/";
        (<HTMLAnchorElement>document.getElementById("fhirServerUrl")).href = res.server;
    });


let columnsListVue = new Vue({
    data: myModel,
    el: "#columnsView",
    methods: {
        removeColumn: function (index: number) {
            myModel.columns.splice(index, 1)
        },
        editColumn: function (index: number) {
            let target = columnsModalVue.$data.data;
            let src = myModel.columns[index];
            target.name = src.name;
            target.type = src.type;
            target.expression = src.expression;

            columnsModalVue.$data.title = "Edit Column";

            console.log("index", index);

            columnsModalVue.$data.callback = (result: Column | null) => {
                console.log("myIndex", index);
                if (result != null) {
                    myModel.columns.splice(index, 1, result);
                }
            };
            (<any>$('#addColumn')).modal('show');
        }
    }
});


let columnsModalData: { data: Column, title: string, callback: (result: Column | null) => void } = {
    data: {
        name: null,
        type: null,
        expression: null
    },
    title: "Column Editor",
    callback: it => null
};
let columnsModalVue = new Vue({
    data: columnsModalData,
    el: '#addColumnForm'
});

function columnsToString(columns: Column[]) {
    //TODO: Do the correct escapings...
    return columns.map(it => `${it.name}@${it.type}:${it.expression}`).join(",");
}

let tableOrRawVue = new Vue({
    el: '#tableOrRaw',
    data: {
        limit: 100,
        showRaw: true,
        rawData: "",
        rawDataWithHighlighting: "",
        tableData: ""
    },
    methods: {
        editLimit: function () {
            let newLimit = window.prompt("Please enter the new limit parameter:", this.limit);
            let newValue = parseInt(newLimit);
            if (!isNaN(newValue)) {
                this.limit = newValue;
            }
        },
        toggleRaw: function () {
            this.showRaw = !this.showRaw;
        },
        reEvaluateHighlighting() {
            monaco.editor.colorize(this.rawData, 'json', {})
                .then(it => this.rawDataWithHighlighting = it);
        },
        loadTableData() {
            let params = `__limit=${this.limit}&__columns=${columnsToString(columnsListVue.$data.columns)}`;
            fetch("/fhir/?" + params, {method: 'POST', body: this.rawData})
                .then(res => res.text())
                .then(csvString => {
                    (<any>CSV).fetch({
                        data: csvString
                    }).done((it: { records: string[][], fields: string[], metadata: any }) => this.tableData = it)
                });
        }
    },
    watch: {
        rawData: function (newData, oldData) {
            this.rawDataWithHighlighting = "Loading Highlighter...";
            this.reEvaluateHighlighting();
            this.tableData = null;
            this.loadTableData();
        }
    }
});


(<any>window).myModel = myModel;
(<any>window).tableOrRawVue = tableOrRawVue;


(<any>window).newColumn = function () {
    (<any>$('#addColumn')).modal('show');
    $('#addColumn').on('shown.bs.modal', function (e) {
        $("#addColumnName").focus();
        //TODO Load Monaco?
    });

    columnsModalVue.$data.title = "Add Column";
    Vue.set(columnsModalVue.$data, 'data', {
        name: null, type: null, expression: null
    });

    columnsModalVue.$data.callback = (result: Column | null) => {
        myModel.columns.push(result);
    };
};

(<any>window).handleAddColumn = function () {
    (<any>$('#addColumn')).modal('hide');
    let result = {
        name: columnsModalVue.$data.data.name,
        type: columnsModalVue.$data.data.type,
        expression: columnsModalVue.$data.data.expression,
    };

    columnsModalVue.$data.callback(result);
};

(<any>window).handleAddColumnAbort = function () {
    (<any>$('#addColumn')).modal('hide');
    columnsModalVue.$data.callback(null);
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

let searchEditor: IStandaloneCodeEditor = (function () {
    monaco.languages.register({id: 'url'});

    monaco.languages.registerCompletionItemProvider("url", new URLCompletionItemProvider());

    let element = document.getElementById("searchbar");
    element.innerHTML = "";
    searchEditor = monaco.editor.create(element, {
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
        function (args) {
            console.log("Enter was suppressed!", args)
        });


    // monaco.languages.setTokensProvider("url", )

    return searchEditor;
})();


(<any>window).downloadRaw = function () {
    fetch("/redirect/" + searchEditor.getValue())
        .then(res => res.text())
        .then(res => {
            tableOrRawVue.$data.rawData = res;
        });
};


