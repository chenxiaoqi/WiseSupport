import Vue from 'vue'
import VueRouter from 'vue-router';

require('bootstrap/dist/css/bootstrap-reboot.css');
require('bootstrap/dist/css/bootstrap.css');
require('bootstrap/dist/css/bootstrap-grid.css');
require('./css/index.css');

Vue.use(VueRouter);

let router = new VueRouter({
    routes: [
        {
            path:'/booking',
            component: {
                template:'#tpl-booking'
            }
        },
        {
            path:'/my',
            component:{
                template:'<div>my</div>'
            }
        },
        {
            path:'/announcement',
            component:{
                template:'<div>annoucement</div>'
            }
        }
    ],
    mode:'hash'
});


const app = new Vue({
    router,
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

