<template>
  <b-modal @ok="handleOk" id="modal-query-save" title="Save Query">
    <form>
      <div class="form-group">
        <input aria-describedby="emailHelp" class="form-control" id="addColumnName"
               placeholder="Enter Query Name" type="text" v-model="name" >
      </div>
    </form>
  </b-modal>
</template>

<script lang="ts">
export default {
  name: "DialogQuerySave",
  data: function (): { name: string } {
    return {
      name: "",
    }
  },
  methods: {
    handleOk: async function () {
      try {
        let result = await fetch(`query/${this.name}`, {method: "POST", body: this.$parent.getDownloadUrl()})
      } catch (e) {
        alert("Error when saving: "+e);
        console.log(e);
      }
    }
  },
}
</script>

<style scoped>

</style>