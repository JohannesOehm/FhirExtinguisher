import {KeyCode} from "monaco-editor";
<template>
    <nav class="navbar navbar-dark sticky-top bg-dark flex-md-nowrap p-0">
        <a :href="endpointUrl" class="navbar-brand col-sm-3 col-md-3 mr-0" href="#" id="fhirServerUrl" target="_blank">{{endpointUrl}}</a>
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
    import {editor, IKeyboardEvent, KeyCode} from "monaco-editor";
    import {URLCompletionItemProvider} from "../url-completionitemprovider";
    import IStandaloneCodeEditor = editor.IStandaloneCodeEditor;

    type TableData = { records: string[][], fields: string[], metadata: any };


    export default {
        name: 'Searchbar',
        props: ['endpointUrl'],
        mounted: function () {
            let that = this;
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
                        vertical: "hidden",
                        horizontal: "auto"
                    },
                    fontSize: 16,
                    theme: "vscode-dark",
                    scrollBeyondLastLine: false,
                    overviewRulerLanes: 0,
                    overviewRulerBorder: false //Still not perfect
                    // lineDecorationsWidth: 0,
                    // lineNumbersMinChars: 0
                });
                (<any>window).searchEditor = searchEditor;
                window.addEventListener("resize", function () {
                    (<any>window).searchEditor.layout();
                });
                // let myBinding = searchEditor.addCommand(monaco.KeyCode.Enter,
                //     function (args) {
                //         if((<any>searchEditor)._contentWidgets["editor.widget.suggestWidget"].widget.state === 3){
                //
                //         } else {
                //             that.$emit('startRequest', searchEditor.getValue());
                //         }
                //     });
                searchEditor.onKeyDown(function (e: IKeyboardEvent) {
                    if (e.keyCode === KeyCode.Enter) {
                        //TODO: Maybe there is a public API for this?
                        if ((<any>searchEditor)._contentWidgets["editor.widget.suggestWidget"].widget.state !== 3) {
                            that.$emit('startRequest', searchEditor.getValue());
                            e.stopPropagation();
                            e.preventDefault();
                        } else {

                        }
                    }

                });

                // monaco.languages.setTokensProvider("url", )

                return searchEditor;
            })();
        },
        methods: {
            setQueryUrl: function (url: string) {
                this.editor.setValue(url);
            }
        }
    }


</script>