import Vue from 'vue'
import VueRouter from "vue-router";

require("./css/index.css");

Vue.use(VueRouter);

Vue.component("app-nav",{
    el:"#v-app-nave"
});


const app = new Vue({
    created: function () {

    },

    methods: {
        sayHello: function () {
            return this.message;
        }
    },

    data: {
        message: 'hello vue'
    },
    computed: {
        say: function () {
            return this.message;
        }
    }
}).$mount('#app');

