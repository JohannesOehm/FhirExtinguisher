const path = require('path');
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin');
const CopyPlugin = require('copy-webpack-plugin');
const VueLoaderPlugin = require('vue-loader/lib/plugin');
const webpack = require("webpack");

const redirectServer = "http://localhost:8081";

module.exports = {
    entry: './src/app/index.ts',
    // devtool: 'inline-source-map',
    mode: 'development',
    module: {
        rules: [
            {
                test: /\.vue$/,
                loader: 'vue-loader',
                options: {
                    esModule: true
                }
            }, {
                test: /\.tsx?$/,
                loader: 'ts-loader',
                exclude: /node_modules/,
                // resolve: {
                //     fallback: {assert: false}
                // },
                options: {
                    appendTsSuffixTo: [/\.vue$/]
                }
            }, {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            }, {
                test: /\.ttf$/,
                use: ['file-loader']
            }
        ],
    },
    plugins: [
        new MonacoWebpackPlugin({
            languages: ['json', 'xml', 'html']
        }),
        new CopyPlugin({
            patterns: [
                {from: 'src/style', to: ''},
                {from: 'src/public', to: ''}
            ]
        }),
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery"
        }),
        new VueLoaderPlugin(),
        new webpack.optimize.LimitChunkCountPlugin({
            maxChunks: 1,
        })
    ],
    resolve: {
        extensions: ['.tsx', '.ts', '.js', '.vue'],
        alias: {vue: 'vue/dist/vue.esm.js'}, //runtime renderingp
        // fallback: {
        //      "assert": require.resolve("assert/")
        // }
    },
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'dist'),
    },
    devServer: {
        contentBase: path.join(__dirname, "dist"),
        compress: true,
        port: 9000,
        proxy: {
            '/redirect': redirectServer,
            '/processBundle': redirectServer,
            '/info': redirectServer,
            '/fhir': redirectServer,
            '/fhirPath': redirectServer
        },
    }
};