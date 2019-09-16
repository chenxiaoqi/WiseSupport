import Vue from 'vue'
import VueRouter from 'vue-router';

require('bootstrap/dist/css/bootstrap-reboot.css');
require('bootstrap/dist/css/bootstrap.css');
require('bootstrap/dist/css/bootstrap-grid.css');
require('./css/index.css');

Vue.use(VueRouter);

Vue.component('app-nav',{
    template:'#v-app-nave'
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

