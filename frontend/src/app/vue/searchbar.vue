<template>
    <nav class="navbar navbar-dark sticky-top bg-dark flex-md-nowrap p-0">
        <a :href="endpointUrl" class="navbar-brand col-sm-3 col-md-2 mr-0" href="#" id="fhirServerUrl" target="_blank">{{endpointUrl}}</a>
        <div aria-label="Search" class="form-control form-control-dark w-100" id="searchbar"
             style="padding:0;margin:2px;">
            SearchBar
        </div>
        <ul class="navbar-nav px-3">
            <li class="nav-item text-nowrap">
                <a @click="$emit('startRequest', editor.getValue())" class="nav-link" href="#">GET</a>
            </li>
        </ul>
    </nav>
</template>

<script lang="ts">
    import * as monaco from "monaco-editor";
    import {editor} from "monaco-editor";
    import {URLCompletionItemProvider} from "../url-completionitemprovider";
    import IStandaloneCodeEditor = editor.IStandaloneCodeEditor;

    export default {
        name: 'Searchbar',
        props: ['endpointUrl'],
        mounted: function () {
            this.editor = (function () {
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
                    function (args) {
                        console.log("Enter was suppressed!", args)
                    });


                // monaco.languages.setTokensProvider("url", )

                return searchEditor;
            })();
        }
    }


</script>