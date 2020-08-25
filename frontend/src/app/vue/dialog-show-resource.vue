<template>
  <b-modal id="modal-show-resource" title="Raw resource" size="xl">
    <pre>{{ text }}</pre>
  </b-modal>
</template>

<script lang="ts">


export default {
  name: "DialogShowResource",
  mounted() {
    this.$root.$on('bv::modal::shown', (bvEvent: any, modalId: any) => {
    })
  },
  props: ["resource"],
  computed: {
    text: function () {
      try {
        return JSON.stringify(JSON.parse(this.resource), null, 2);
      } catch (e) {
        return "";
      }
    },
  }
}
</script>