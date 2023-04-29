var http = require('http');

const json = require('./flicker.json');

http.createServer(function (request, response) {
    console.log(request.url);
    // 发送 HTTP 头部
    // HTTP 状态值: 200 : OK
    // 内容类型: text/plain
    // response.writeHead(200, {'Content-Type': 'text/plain'});

    // // 发送响应数据 "Hello World"
    // response.end('Hello World\n');

    response.writeHead(200, {'Content-Type': 'text/plain'});


    if (request.url.includes('/?method=flickr.photos.getRecent')) {
        response.end(JSON.stringify(json));
    } else {
    // 发送响应数据 "Hello World"
    response.end('Hello World\n');
    }
}).listen(8080);