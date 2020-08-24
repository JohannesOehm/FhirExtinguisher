<template>
  <b-modal id="modal-cheat-sheet" title="FHIR Search API Cheat Sheet" size="xl">
    <h5>Combining Criteria</h5>
    <table>
      <tr>
        <td><code class="highlight-url">Patient?language=FR&language=NL</code></td>
        <td>Patient must speak FR <strong>and</strong> NL</td>
      </tr>
      <tr>
        <td><code class="highlight-url">Patient?language=FR,NL</code></td>
        <td>Patient must speak FR <strong>or</strong> NL</td>
      </tr>
      <tr>
        <td><code class="highlight-url">Patient?language=FR,NL&language=EN</code></td>
        <td>Patient must speak EN <strong>and</strong> (FR <strong>or</strong> NL)</td>
      </tr>
      <tr>
        <td><code class="highlight-url">Observation?code-value-quantity=2823-3$gt5.4||mmol/L</code></td>
        <td>Search for all Observation with a potassium value of >5.4 mmol/L using <b>composite parameters</b>.
        </td>
      </tr>
    </table>

    <h5>Modifiers</h5>
    <table>
      <tr>
        <td><code class="highlight-url">Patient?name:missing=false</code></td>
        <td>Return patients iff element "name" is present</td>
      </tr>
      <tr>
        <td><code class="highlight-url">Patient?name:exact=John</code></td>
        <td>String parameters only. Matches the entire parameter case-sensitive (Normal string search is
          case-insensitive and might match only the prefix)
        </td>
      </tr>
      <tr>
        <td><code class="highlight-url">Patient?name:contains=John</code></td>
        <td>String parameters only. Matches anywhere in the text</td>
      </tr>
    </table>
    Please check out the IntelliSense documentation for help with the other modifiers.

    <h5>Token</h5>
    This SearchParameter type is used for Codings, CodeableConcepts, Identifier, ContactPoint, code datatypes.
    Matching tokens is usually case-sensitive. For more information, please consult <a
      href="https://www.hl7.org/fhir/search.html#token">FHIR documentation</a>.

    <h5>Reference</h5>
    <table>
      <tr>
        <td><code class="highlight-url">Observation?subject=123</code></td>
        <td> The logical [id] of a resource using a local reference (i.e. a relative reference)</td>
      </tr>
      <tr>
        <td><code class="highlight-url">Observation?subject=Patient/123</code></td>
        <td> The logical [id] of a resource of a specified type using a local reference</td>
      <tr>
        <td><code class="highlight-url">Observation?subject=http://fh.ir/Patient/123</code></td>
        <td> A reference to a resource by its absolute location, or by it's canonical URL</td>
      </tr>
      <tr>
        <td><code class="highlight-url">Observation?subject.name=Peter</code></td>
        <td>Search for all observations whose subject's name is "Peter" (chaining).</td>
      </tr>
      <tr>
        <td><code class="highlight-url">Patient?_has:Observation:patient:code=1234-5</code></td>
        <td>Search for all Patients with a specific Observation (reverse chaining).</td>
      </tr>
    </table>

    <h5>Prefixes (only for number, date and quantity parameters)</h5>
    <table style="border-collapse:separate; border-spacing: 10px 2px;text-align: center">
      <tr>
        <td><code>eq</code></td>
        <td><code>ne</code></td>
        <td><code>gt</code></td>
        <td><code>lt</code></td>
        <td><code>le</code></td>
        <td><code>ge</code></td>
        <td><code>ap</code></td>
        <td><code>sa</code></td>
        <td><code>eb</code></td>
      </tr>
      <tr>
        <td>=</td>
        <td>≠</td>
        <td>&lt;</td>
        <td>&gt;</td>
        <td>≤</td>
        <td>≥</td>
        <td>≈</td>
        <td>starts after</td>
        <td>ends before</td>
      </tr>
    </table>

    <h5>Numbers</h5>
    <table>
      <tbody>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:blue">100</span></code></td>
        <td>Values that equal 100, to 3 significant figures precision, so this is actually searching for values
          in the range [99.5 ... 100.5)
        </td>
      </tr>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:blue">100.00</span></code></td>
        <td>Values that equal 100, to 5 significant figures precision, so this is actually searching for values
          in the range [99.995 ... 100.005)
        </td>
      </tr>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:blue">1e2</span></code></td>
        <td>Values that equal 100, to 1 significant figures precision, so this is actually searching for values
          in the range [95 ... 105)
        </td>
      </tr>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:red">lt</span><span
            style="color:blue">100</span></code></td>
        <td>Values that are less than exactly 100</td>
      </tr>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:red">le</span><span
            style="color:blue">100</span></code></td>
        <td>Values that are less or equal to exactly 100</td>
      </tr>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:red">gt</span><span
            style="color:blue">100</span></code></td>
        <td>Values that are greater than exactly 100</td>
      </tr>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:red">ge</span><span
            style="color:blue">100</span></code></td>
        <td>Values that are greater or equal to exactly 100</td>
      </tr>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:red">ne</span><span
            style="color:blue">100</span></code></td>
        <td>Values that are not equal to 100 (actually, in the range 99.5 to 100.5)</td>
      </tr>
      </tbody>
    </table>

    <h5>Quantity</h5>
    <table>
      <tr>
        <td><code><span class="mtk1">[parameter]</span>=<span style="color:red">[prefix]</span><span
            style="color:blue;">[number]</span>|<span style="color:orange">[system]</span>|<span
            style="color:green">[code]</span></code></td>
      </tr>
      <tr>
        <td><code><span class="mtk1 mtkb">Observation</span><span class="mtk14 mtkb">?</span><span class="mtk1">value-quantity</span>=<span
            style="color:red">le</span><span
            style="color:blue">5.4</span>|<span style="color:orange;">http://unitsofmeasure.org</span>|<span
            style="color:green;">mg</span></code></td>
      </tr>
      <tr>
        <td><code><span class="mtk1 mtkb">Observation</span><span class="mtk14 mtkb">?</span><span class="mtk1">value-quantity</span>=<span
            style="color:blue">5.4</span>||<span
            style="color:green;">mg</span></code></td>
      </tr>
      <tr>
        <td><code><span class="mtk1 mtkb">Observation</span><span class="mtk14 mtkb">?</span><span class="mtk1">value-quantity</span>=<span
            style="color:blue">5.4</span></code></td>
      </tr>
    </table>
    <h5>Date</h5>
    <code>yyyy-mm-ddThh:mm:ss[Z|(+|-)hh:mm]</code> (standard XML format)

    <h5>Escaping</h5>
    <table>
      <tr>
        <td><code class="highlight-url" data-lang="url">Patient?name=John,Jack</code></td>
        <td>Search for all Patients named either "John" or "Jack"</td>
      </tr>
      <tr>
        <td><code class="highlight-url" data-lang="url">Patient?name=John\,Jack</code></td>
        <td>Search for all Patients named "John,Jack"</td>
      </tr>
      <tr>
        <td><code class="highlight-url" data-lang="url">Patient?name=John\\Jack</code></td>
        <td>Search for all Patients named "John\Jack"</td>
      </tr>
      <tr>
        <td><code class="highlight-url" data-lang="url">Patient?name=John\Jack</code></td>
        <td>not valid</td>
      </tr>
    </table>
    <code>|</code> and <code>$</code> must be escaped using <code>\|</code> and <code>\$</code>!


  </b-modal>
</template>

<script lang="ts">
import * as monaco from "monaco-editor";


export default {
  name: "DialogCheatSheet",
  mounted() {
    this.$root.$on('bv::modal::shown', (bvEvent: any, modalId: any) => {
      let elements = document.querySelectorAll(".highlight-url");
      for (let element of elements) {
        monaco.editor.colorize(element.innerHTML.replace("&amp;", "&"), "url", {})
            .then((it: any) => {
              return element.innerHTML = it;
            });
        element.classList.add("highlighted")
      }
    })
  }
}
</script>