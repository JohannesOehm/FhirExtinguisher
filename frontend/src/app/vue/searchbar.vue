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
    <a :href="endpointUrl" class="navbar-brand col-sm-3 col-md-3 mr-0" href="#" id="fhirServerUrl"
       target="_blank">{{ endpointUrl }}</a>
    <div aria-label="Search" class="form-control form-control-dark w-100" id="searchbar"
         style="padding:0;margin:2px;">
      <monaco-singleline v-model="value" :options="options" :editor-before-mount="onEditorBeforeMount"
                         :on-enter="onEnter"/>
    </div>
    <ul class="navbar-nav px-3">
      <li class="nav-item text-nowrap">
        <a @click="$emit('startRequest', value)" class="nav-link" href="#">GET</a>
      </li>
    </ul>
  </nav>
</template>

<script lang="ts">
import * as monaco from "monaco-editor";
import {IKeyboardEvent, KeyCode} from "monaco-editor";
import {URLCompletionItemProvider} from "../url-completionitemprovider";
import {UrlTokensProvider} from "../url-grammar-antlr";
import MonacoSingleline from "./monaco-singleline.vue"

export default {
  name: 'Searchbar',
  props: ['endpointUrl'],
  components: {MonacoSingleline},
  mounted: function () {

  },
  data() {
    return {
      value: "Patient",
      options: {
        theme: "myCoolTheme",
        language: "url"
      }
    }
  },
  methods: {
    setQueryUrl: function (url: string) {
      this.value = url;
    },
    getFhirSearchQuery: function () {
      return this.value;
    },
    onEnter(url: string) {
      this.$emit('startRequest', url);
    },
    onEditorBeforeMount(monaco) {
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
    }
  }
}


</script>