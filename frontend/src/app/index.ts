import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap-vue/dist/bootstrap-vue.css'

import Vue from 'vue';
import App from './vue/my-app.vue';
import BootstrapVue, {IconsPlugin, ModalPlugin} from "bootstrap-vue";

import './url-grammar-antlr'

Vue.use(BootstrapVue);
Vue.use(IconsPlugin);
Vue.use(ModalPlugin);


new Vue({
    // @ts-ignore
    render: h => h(App)
}).$mount("#app");


export type Column = { name: string, type: string, expression: string, subColumns?: SubColumn[] }

export type SubColumn = { name: string, expression: string };