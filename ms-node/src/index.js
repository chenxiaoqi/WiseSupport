import Vue from 'vue'
import VueRouter from "vue-router";

Vue.use(VueRouter);

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

