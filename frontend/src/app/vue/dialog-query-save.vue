<template>
    <b-modal @ok="handleOk" id="modal-query-save" title="Save Query">
        <div class="form-group">
            <input class="form-control" id="addColumnName"
                   placeholder="Enter Query Name" type="text" v-model="name">
        </div>
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
                    let response = await fetch(`query-storage/${this.name}`, {
                      method: "POST",
                      body: this.$parent.getDownloadUrl()
                    })
                    if (response.status == 409) {
                      let overwrite = await this.$bvModal.msgBoxConfirm("Name already exists. Do you want to overwrite it?");
                        if (overwrite) {
                            let response = await fetch(`query/${this.name}?force=true`, {
                                method: "POST",
                                body: this.$parent.getDownloadUrl()
                            })
                        }
                    }
                } catch (e) {
                    alert("Error when saving: " + e);
                    console.log(e);
                }
            }
        },
    }
</script>

<style scoped>

</style>