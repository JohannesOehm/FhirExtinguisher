<template>
  <div class="monaco-singleline">
    <!-- v-show="!showPlaceholder" -->
    <monaco-editor
        v-model="valueLocal"
        v-bind="$attrs"
        language="mySpecialLanguage"
        theme="myCoolTheme"
        :height="height"
        :diffEditor="diffEditor"
        :options="opts"
        :editorBeforeMount="onEditorBeforeMount"
        :editorMounted="onEditorMounted"
        :class="[
        showPlaceholder ? 'editor-with-placeholder' : '',
        readOnly ? 'editor-with-read-only' : '',
      ]"
    />
    <div
        v-show="showPlaceholder"
        class="placeholder"
        :style="{ fontSize: opts.fontSize }"
        @click="onPlaceholderClick"
    >
      {{ placeholder }}
    </div>
  </div>
</template>

<script>
import MonacoEditor from './monaco-editor'
// all options: https://microsoft.github.io/monaco-editor/api/interfaces/monaco.editor.istandaloneeditorconstructionoptions.html
const DefaultOptions = {
  fontSize: '14px',
  fontWeight: 'normal',
  wordWrap: 'off',
  lineNumbers: 'off',
  lineNumbersMinChars: 0,
  overviewRulerLanes: 0,
  overviewRulerBorder: false,
  hideCursorInOverviewRuler: true,
  lineDecorationsWidth: 0,
  glyphMargin: false,
  folding: false,
  scrollBeyondLastColumn: 0,
  scrollbar: {
    horizontal: 'hidden',
    vertical: 'hidden',
    // avoid can not scroll page when hover monaco
    alwaysConsumeMouseWheel: false,
  },
  // disable `Find`
  find: {
    addExtraSpaceOnTop: false,
    autoFindInSelection: 'never',
    seedSearchStringFromSelection: false,
  },
  minimap: {enabled: false},
  // see: https://github.com/microsoft/monaco-editor/issues/1746
  wordBasedSuggestions: true,
  // avoid links underline
  links: false,
  // avoid highlight hover word
  occurrencesHighlight: false,
  cursorStyle: 'line-thin',
  // hide current row highlight grey border
  // see: https://microsoft.github.io/monaco-editor/api/interfaces/monaco.editor.ieditoroptions.html#renderlinehighlight
  renderLineHighlight: 'none',
  contextmenu: false,
  // default selection is rounded
  roundedSelection: false,
  hover: {
    // unit: ms
    // default: 300
    delay: 100,
  },
  acceptSuggestionOnEnter: 'on',
  // auto adjust width and height to parent
  // see: https://github.com/Microsoft/monaco-editor/issues/543#issuecomment-321767059
  automaticLayout: true,
  // if monaco is inside a table, hover tips or completion may casue table body scroll
  fixedOverflowWidgets: true,
}

function noop() {
}

export default {
  name: 'monaco-singleline',
  components: {
    MonacoEditor,
  },
  model: {
    prop: 'value',
    event: 'change',
  },
  props: {
    value: {type: [String, Number], default: ''},
    placeholder: {type: [String, Number], default: ''},
    readOnly: {type: Boolean, default: false},
    height: {type: [String, Number], default: '21'},
    diffEditor: {type: Boolean, default: false},
    options: {
      type: Object,
      default() {
        return {}
      },
    },
    editorMounted: {type: Function, default: noop},
    editorBeforeMount: {type: Function, default: noop},
    onEnter: {type: Function, default: noop},
  },
  data() {
    return {
      // editor: '',
      optionsLocal: {},
    }
  },
  computed: {
    valueLocal: {
      get() {
        return this.value
      },
      set(value) {
        this.$emit('change', value)
      },
    },
    showPlaceholder() {
      return !this.diffEditor && !Boolean(`${this.valueLocal}`)
    },
    opts() {
      return {
        ...DefaultOptions,
        ...{readOnly: this.readOnly},
        ...this.options,
      }
    },
  },
  methods: {
    focus() {
      this.editor.focus()
    },
    onEditorBeforeMount(monaco) {
      this.editorBeforeMount(monaco)
    },
    onEditorMounted(editor, monaco) {
      this.editor = editor
      window.myEditor = editor
      // disable `Find` widget
      // see: https://github.com/microsoft/monaco-editor/issues/287#issuecomment-328371787
      // eslint-disable-next-line no-bitwise
      editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KEY_F, () => {
      })
      // disable press `Enter` in case of producing line breaks
      editor.addCommand(monaco.KeyCode.Enter, () => {

        console.log("Enter pressed!", this.editor)
        /**
         * Origin purpose: disable line breaks
         * Side Effect: If defining completions, will prevent `Enter` confirm selection
         * Side Effect Solution: always accept selected suggestion when `Enter`
         *
         * But it is hard to find out the name `acceptSelectedSuggestion` to trigger.
         *
         * Where to find the `acceptSelectedSuggestion` at monaco official documents ?
         * Below is some refs:
         * - https://stackoverflow.com/questions/64430041/get-a-list-of-monaco-commands-actions-ids
         * - command from: https://github.com/microsoft/vscode/blob/e216a598d3e02401f26459fb63a4f1b6365ec4ec/src/vs/editor/contrib/suggest/suggestController.ts#L632-L638
         * - https://github.com/microsoft/vscode/search?q=registerEditorCommand
         * - real list: https://github.com/microsoft/vscode/blob/e216a598d3e02401f26459fb63a4f1b6365ec4ec/src/vs/editor/browser/editorExtensions.ts#L611
         *
         *
         * Finally, `acceptSelectedSuggestion` appears here:
         * - `editorExtensions.js` Line 288
         */
        // if (this.editor._contentWidgets["editor.widget.suggestWidget"].widget.state !== 3) {
        //   this.onEnter(this.editor.getValue());
        // } else {
        editor.trigger('', 'acceptSelectedSuggestion')
        // }
      })
      // deal with user paste
      // see: https://github.com/microsoft/monaco-editor/issues/2009#issue-63987720
      editor.onDidPaste((e) => {
        // multiple rows will be merged to single row
        if (e.endLineNumber <= 1) {
          return
        }
        let newContent = ''
        const textModel = editor.getModel()
        const lineCount = textModel.getLineCount()
        // remove all line breaks
        for (let i = 0; i < lineCount; i += 1) {
          newContent += textModel.getLineContent(i + 1)
        }
        textModel.setValue(newContent)
        editor.setPosition({column: newContent.length + 1, lineNumber: 1})
      })
      // disable `F1` command palette
      editor.addCommand(monaco.KeyCode.F1, () => {
      })
      // --------
      this.editorMounted(editor, monaco)
    },
    onPlaceholderClick() {
      // const vm = this
      // this.$nextTick(() => {
      //   // see: https://github.com/PolymerVis/monaco-editor/issues/1#issuecomment-357378349
      //   vm.editor.layout()
      //   vm.editor.focus()
      // })
      this.editor.layout()
      this.editor.focus()
    },
  },
}
</script>

<style scoped>
.monaco-singleline {
  display: flex;
  align-items: center;
  position: relative;
  padding: 8px 8px;
  border: 1px solid #d9d9d9;
}

.monaco-singleline:hover {
  border-color: #000;
}

.monaco-singleline ::v-deep .decorationsOverviewRuler {
  display: none;
}

.placeholder {
  position: absolute;
  left: 0;
  right: 0;
  overflow: hidden;
  padding-left: inherit;
  padding-right: inherit;
  cursor: text;
  user-select: none;
  font-size: 14px;
  color: #999;
  font-style: italic;
}

.editor-with-placeholder ::v-deep .view-lines {
  opacity: 0;
}

.editor-with-read-only ::v-deep .cursors-layer > .cursor {
  opacity: 0;
}

</style>