import Vue from 'vue'
import VueRouter from 'vue-router';
import axios from 'axios';

require('bootstrap/dist/css/bootstrap-reboot.css');
require('bootstrap/dist/css/bootstrap.css');
require('bootstrap/dist/css/bootstrap-grid.css');
require('./css/index.css');

Vue.use(VueRouter);
const DAY_MILS = 24 * 60 * 60 * 1000;
let router = new VueRouter({
    routes: [
        {
            path: '/booking',
            component: {
                template: '#tpl-booking',
                data() {
                    let timeRange = 16, dayRange = 3;
                    let headers = [];
                    let now = Date.now();
                    for (let i = 0; i < dayRange; i++) {
                        let date = new Date(now + i * DAY_MILS);
                        headers.push({
                            month: date.getMonth(),
                            day: date.getDate(),
                            week: date.toLocaleDateString('zh-CN', {weekday: 'short'})
                        });
                    }
                    let grid = [];
                    for (let i = 0; i < timeRange; i++) {
                        for (let j = 0; j < dayRange; j++) {
                            grid.push({dayOffset: j, timeIndex: i * 2, selected: false})
                            grid.push({dayOffset: j, timeIndex: i * 2 + 1, selected: false})
                        }
                    }
                    axios.get('/booking/booking-list').then((response)=>{
                        console.log(response);
                    });

                    return {
                        startDate: now,
                        timeRange,
                        dayRange,
                        headers,
                        grid
                    }
                },
                methods: {
                    choose: function (index) {
                        this.grid[index].selected = !this.grid[index].selected;
                    },
                    nextTimeRange: function (left) {
                        if (left) {
                            this.startDate = this.startDate - this.dayRange * DAY_MILS;
                        } else {
                            this.startDate = this.startDate + this.dayRange * DAY_MILS;
                        }
                        let headers = [];
                        for (let i = 0; i < this.dayRange; i++) {
                            let date = new Date(this.startDate + i * DAY_MILS);
                            this.headers.splice(i, 1, {
                                month: date.getMonth(),
                                day: date.getDate(),
                                week: date.toLocaleDateString('zh-CN', {weekday: 'short'})
                            })
                        }
                    }
                },
                computed: {}
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
                template: '<div>announcement</div>'
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

