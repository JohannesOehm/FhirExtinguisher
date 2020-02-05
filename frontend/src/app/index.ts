import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';

import * as monaco from 'monaco-editor';

import Vue from 'vue';
import {URLCompletionItemProvider} from "./url-completionitemprovider";


type Column = { name: string, type: string, expression: string }

let myModel: { columns: Column[] } = {
    columns: [
        {name: "name", type: "explode", expression: "Patient.name"}
    ]
};

Vue.config.errorHandler = (err, vm, info) => {
    console.log({err, vm, info});
};

let columnsView = new Vue({
    data: myModel,
    el: "#columnsView"
});

(<any>window).myModel = myModel;


(<any>window).handleAddColumn = function () {
    console.log("hello woooorld!!!");
};

(<any>window).loadFhirPathMonaco = function () {

    let element = document.getElementById("fhirPathEditor");
    let value = element.innerText;
    element.innerHTML = "";
    let fhirPathEditor = monaco.editor.create(element, {
        value: value,
        language: "json",
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
    var myBinding = fhirPathEditor.addCommand(monaco.KeyCode.Enter,
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

