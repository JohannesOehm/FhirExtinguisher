import {KeyCode} from "monaco-editor";
<template>
    <nav class="navbar navbar-dark sticky-top bg-dark flex-md-nowrap p-0">
        <b-dropdown variant="link" toggle-class="text-decoration-none" no-caret>
            <template v-slot:button-content>
                <img src="logo.png" style="height:28px; width:28px;"/><span class="sr-only">FhirExtinguisher</span>
            </template>
            <b-dropdown-item href="#" v-b-modal.modal-cheat-sheet>FHIR Search Cheat Sheet</b-dropdown-item>
            <b-dropdown-item href="#" v-b-modal.modal-query-save>Save query...</b-dropdown-item>
            <b-dropdown-item href="#" v-b-modal.modal-query-load>Load query...</b-dropdown-item>
            <b-dropdown-item href="#" v-b-modal.modal-about>About</b-dropdown-item>
        </b-dropdown>
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
import {UrlTokensProvider} from "../url-grammar-antlr";

type TableData = { records: string[][], fields: string[], metadata: any };


export default {
  name: 'Searchbar',
  props: ['endpointUrl'],
  mounted: function () {
    let that = this;
    this.editor = (function () {
      monaco.languages.register({id: 'url'});

      monaco.languages.setTokensProvider("url", new UrlTokensProvider());

      monaco.languages.registerCompletionItemProvider("url", new URLCompletionItemProvider());

      let literalFg = '#000000';
      let idFg = '#344482';
      let symbolsFg = '#555555';
      let keywordFg = '#7132a8';
      let errorFg = '#ff0000';
      let valueSep = '#990000';
      monaco.editor.defineTheme('myCoolTheme', {
        base: 'vs',
        inherit: true,
        colors: {},
        rules: [
          {token: 'string.url', foreground: literalFg},
          {token: 'path_string.url', foreground: literalFg, fontStyle: 'bold'},
          {token: 'path_separator.url', foreground: symbolsFg},
          {token: 'query_path_separator.url', foreground: symbolsFg, fontStyle: 'bold'},
          {token: 'searchparameter_separator.url', foreground: symbolsFg},
          {token: 'modifier_separator.url', foreground: symbolsFg},
          {token: 'modifier.url', foreground: literalFg, fontStyle: 'italic'},
          {token: 'keyval_separator.url', foreground: symbolsFg},
          {token: 'value_separator.url', foreground: valueSep, fontStyle: 'bold'},
          {token: 'composite_separator.url', foreground: valueSep},
          {token: 'bar_separator.url', foreground: valueSep},
          {token: 'error.url', foreground: errorFg}
        ]
      });

      let element = document.getElementById("searchbar");
      element.innerHTML = "";
      let searchEditor = monaco.editor.create(element, {
        value: "Patient?",
        language: "url",
        minimap: {enabled: false},
        lineNumbers: 'off',
        glyphMargin: false,
        folding: false,
        scrollbar: {
          vertical: "hidden",
          horizontal: "auto"
        },
        fontSize: 16,
        theme: "myCoolTheme",
        scrollBeyondLastLine: false,
        overviewRulerLanes: 0,
        overviewRulerBorder: false, //Still not perfect
        hideCursorInOverviewRuler: true,
        // lineDecorationsWidth: 0,
        // lineNumbersMinChars: 0
      });
      (<any>window).searchEditor = searchEditor;
      window.addEventListener("resize", function () {
        (<any>window).searchEditor.layout();
      });
      // let myBinding = searchEditor.addCommand(monaco.KeyCode.Enter,
      // );
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


      return searchEditor;
    })();
    window.addEventListener('paste', (e: ClipboardEvent) => {
      let items = e.clipboardData.items;
      if (this.editor.hasTextFocus()) {
        window.setTimeout(() => {
          if (this.editor.getValue().includes("\n")) {
            this.setQueryUrl(this.editor.getValue().replaceAll("\n", "").replaceAll("\r", ""));
          }
        }, 250);
      }
    });
    // let selection = this.editor.getSelection();
    // for (let i = 0; i < items.length; i++) {
    //   let item = items[0];
    //   console.log("item", item)
    //   if (item.type === "text/plain" || item.type === "vscode-editor-data") {
    //     item.getAsString( (content)  => {
    //       console.log("content", content);
    //       this.editor.executeEdits("", [{
    //           range: new monaco.Range(selection.endLineNumber, selection.endColumn, selection.endLineNumber, selection.endColumn),
    //           text: content.replace("\n", "")
    //         }])
    //       let {endLineNumber, endColumn} = this.editor.getSelection()
    //       this.editor.setPosition({lineNumber: endLineNumber, column: endColumn})
    //     });
    //   }
    // }
    // }
    // });
  },
        methods: {
            setQueryUrl: function (url: string) {
                this.editor.setValue(url);
            }
        }
    }


</script>