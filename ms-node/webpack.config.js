const htmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');
module.exports = {
    mode: "development",
    entry: "./src/index.js",
    output: {
        path: path.resolve(__dirname) + "/dist",
        filename: "js/[name].bundle.js"
        // publicPath:"http://cdn.com"
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: {
                    loader: "babel-loader"
                }
            },
            {
                test: /\.vue$/,
                loader: 'vue-loader'
            },
            {
                test: /\.css$/,
                use: [
                    {loader: "style-loader"},
                    {loader: "css-loader"}
                ]
            }
        ]
    },
    resolve: {
        alias: {
            'vue$': 'vue/dist/vue.esm.js',
            'vue-router$': 'vue-router/dist/vue-router.esm.js',
            'vue-resource$': 'vue-resource/dist/vue-resource.esm.js',
            '@': path.resolve(__dirname, "src")
        },
        extensions: ['*', '.js', '.vue', '.json']
    },
    plugins: [
        new htmlWebpackPlugin({
            template: "./src/index.html",
            inject: 'body'
        })
    ],
    devServer: {
        host: '127.0.0.1', //fix error [WDS] Disconnected!
        proxy: {
            '/mock/*': {
                target: 'http://localhost:9090',
                secure: false
            }
        }
    }
};