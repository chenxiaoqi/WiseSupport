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
            path: '/booking',
            component: {
                template: '#tpl-booking',
                data() {
                    let table = [];
                    for (let i = 0; i < 7; i++) {
                        let row = [];
                        for (let j = 0; j < 16; j++) {
                            let cell = {hour:j+7,day:i};
                            row.push(cell);
                        }
                        table.push(row);
                    }
                    return {table}
                }
            }
        },
        {
            path: '/my',
            component: {
                template: '<div>my</div>'
            }
        },
        {
            path: '/announcement',
            component: {
                template: '<div>annoucement</div>'
            }
        }
    ],
    mode: 'hash'
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

