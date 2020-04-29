/**
 * 页面装入时这条语句会被执行，这样应用就不需要在页面中声明activex控件的Object节点
 */
var object = document.createElement("object");
//object.setAttribute("id","spwebOcx");
//object.setAttribute("classid","clsid:6D1C42B2-5079-4C0B-8897-30E05AFC0284");
//object.setAttribute("codebase","SPICC.cab#version=5.0.0.0");
object.setAttribute("id","caOcx");
object.setAttribute("classid","clsid:16F2448E-8C16-11D1-9A11-0080C8E1561F");
document.documentElement.firstChild.appendChild(object);
//document.write("<object  id=\"spwebOcx\" " + "classid=\"clsid:6D1C42B2-5079-4C0B-8897-30E05AFC0284\" " + "codebase=\"SPICC.cab#version=5.0.0.0\" " + "standby=\"正在下载客户端控件，请稍候...\">" + "</object>");
//if (window.location.href.indexOf("index.html") > 0) {
//
//}



/**
 * XMLHttpRequest对象的封装实现，用户可以通过这个类的对象与服务器端进行交互
 * 使用方法为 var spweb = new SPWebConnector();   这样就可以建立一个与服务器端交互的对象
 */
var SPWebConnector = function() {
    if (window.XMLHttpRequest) {
        //针对非IE浏览器以及IE7创建XMLHttpRequest对象
        this.xmlHttp = new XMLHttpRequest();
        //部分Mozilla浏览器需要设置MimeType为"text/xml"，否则处理服务器返回时会出错
        if (this.xmlHttp.overrideMimeType) {
            this.xmlHttp.overrideMimeType("text/xml");
        }
    } else if(window.ActiveXObject) {
        //针对IE5和IE6浏览器创建XMLHttpRequest对象
        var MSXML = ['MSXML2.XMLHTTP.5.0','MSXML2.XMLHTTP.4.0',
            'MSXML2.XMLHTTP.3.0','MSXML2.XMLHTTP','Microsoft.XMLHTTP'];
        for (var n = 0; n < MSXML.length; n++) {
            try {
                this.xmlHttp = new ActiveXObject(MSXML[n]);
                break;
            } catch(e) {}
        }
    }
}

/**
 * 定义用户向服务器端发送数据的方法
 * send方法的使用方式如下:
 *   var spweb = new SPWebConnector(); //新建连接对象
 *   spweb.send("POST","SPWebServlet","name=123",usercallcak,failcallback);
 * @method 表示http请求的方法
 * @url 表示http请求的地址
 * @data 表示向服务器端发送的数据
 * @usercallback 表示用户处理响应数据的方法名,这个由用户自定义,方法的第一个参数是文本形式的返回数据,第二个参数是XML格式的dom对象
 * @failcallback 表示用户处理响应失败的方法名,这个由用户自定义,方法的第一个参数是http的状态码,第二个参数是http的状态信息
 * @param 用户自定义参数信息，在调用usercallback和failcallback时会将这个参数作为两个方法的第三参数传入
 */
SPWebConnector.prototype.send = function(method,url,data,usercallback,failcallback,param) {
    if (this.xmlHttp == null) {
        alert("当前浏览器不支持系统，请更换新的浏览器，如IE6");
        return;
    }
    var tempxmlhttp = this.xmlHttp;
    this.xmlHttp.onreadystatechange = function() {
        if (tempxmlhttp.readyState == 4) {
            if (tempxmlhttp.status == 200) {
                if (usercallback) {
                    usercallback(tempxmlhttp.responseText,tempxmlhttp.responseXML,param);
                } else {
                    alert("用户没有设置处理响应数据的函数,响应数据内容为:" + tempxmlhttp.responseText);
                }
            } else {
                if (failcallback) {
                    failcallback(tempxmlhttp.status,tempxmlhttp.statusText,param);
                } else {
                    alert("用户没有设置处理响应失败的函数,http的状态码为:" + tempxmlhttp.status
                        + ", http的状态信息为:" + tempxmlhttp.statusText);
                }
            }
        }
    }
    if (method.toUpperCase() != "GET" && method.toUpperCase() != "POST") {
        alert("用户提供的http请求方式不是GET或POST");
        return;
    }
    var index = url.indexOf("?");
    var tempArray;
    var temp;
    if (index > 0) {
        var tempStr = url.substring(index + 2);
        url = url.substring(0,index + 1);
        tempArray = tempStr.split("&");
        for(var i = 0; i < tempArray.length; i++){
            temp = tempArray[i].split("=");
            url += temp[0] + "=" + spEncodeURI(temp[1]);
            if (i < tempArray.length - 1){
                url += "&";
            }
        }
        url += "&t=" + (new Date()).toString();
    } else {
        url = url + "?t=" + (new Date()).toString();
    }
    this.xmlHttp.open(method,url,true);
    if (method.toUpperCase() == "POST") {
        this.xmlHttp.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
    }
    if (data != null){
        tempArray = data.split("&");
        data = "";
        for(var i = 0; i < tempArray.length; i++){
            temp = tempArray[i].split("=");
            data += temp[0];
            for(var j = 1; j < temp.length; j++){
                data += "=" + spEncodeURI(temp[j]);
            }
            if (i < tempArray.length - 1){
                data += "&";
            }
        }
    }
    this.xmlHttp.send(data);
}

/**
 * 将给定的字符串，按模式串进行分割
 * @param source 要分割的字符串
 * @param split 分割的模式串
 * @return 返回分割后的字符串数组
 */
function splitString(source, split) {
    if (source == null || source.length < 1) {
        return new Array();
    }
    var strArray;
    if (split == null || split.length < 1) {
        strArray = new Array(1);
        strArray[0] = source;
        return strArray;
    } else {
        strArray = source.split(split);
        return strArray;
    }
}

/**
 * 将输入的字符串转换成http传输允许的字符串，该函数可以处理中文和特殊字符。
 * @param str 待转换的字符串
 * @return 转换后的字符串
 */
function spEncodeURI(str) {
    var out, i, j, len, c, c2;
    out = [];
    len = str.length;
    for (i = 0, j = 0; i < len; i++, j++) {
        c = str.charCodeAt(i);
        if (c <= 0x7f) {
            // out[j] = str.charAt(i);
            out[j] = "%" + c.toString(16);
        } else if (c <= 0x7ff) {
            // out[j] = String.fromCharCode(0xc0 | (c >>> 6),
            // 		0x80 | (c & 0x3f));
            out[j] = "%" + (0xc0 | (c >>> 6)).toString(16) + "%" + (0x80 | (c & 0x3f)).toString(16);
        } else if (c < 0xd800 || c > 0xdfff) {
            // out[j] = String.fromCharCode(0xe0 | (c >>> 12),
            // 0x80 | ((c >>> 6) & 0x3f),
            // 0x80 | (c & 0x3f));
            out[j] = "%" + (0xe0 | (c >>> 12)).toString(16) + "%" + (0x80 | ((c >>> 6) & 0x3f)).toString(16)
                + "%" + (0x80 | (c & 0x3f)).toString(16);
        } else {
            if (++i < len) {
                c2 = str.charCodeAt(i);
                if (c <= 0xdbff && 0xdc00 <= c2 && c2 <= 0xdfff) {
                    c = ((c & 0x03ff) << 10 | (c2 & 0x03ff)) + 0x010000;
                    if (0x010000 <= c && c <= 0x10ffff) {
                        // out[j] = String.fromCharCode(0xf0 | ((c >>> 18) & 0x3f),
                        // 0x80 | ((c >>> 12) & 0x3f),
                        // 0x80 | ((c >>> 6) & 0x3f),
                        // 0x80 | (c & 0x3f));
                        out[j] = "%" + (0xf0 | ((c >>> 18) & 0x3f)).toString(16)
                            + "%" + (0x80 | ((c >>> 12) & 0x3f)).toString(16)
                            + "%" + (0x80 | ((c >>> 6) & 0x3f)).toString(16)
                            + "%" + (0x80 | (c & 0x3f)).toString(16);
                    } else {
                        // out[j] = '?';
                        out[j] = "%3f";
                    }
                } else {
                    i--;
                    // out[j] = '?';
                    out[j] = "%3f";
                }
            } else {
                i--;
                // out[j] = '?';
                out[j] = "%3f";
            }
        }
    }
    return out.join('');
}


/**
 * 封装获取元素节点的方法
 * 输入参数可以是1-n个节点的id属性值
 * 返回值是一个元素节点或一个包含所有元素节点的数组
 */
function $() {
    var elements = new Array();
    for (var i = 0; i < arguments.length; i++) {
        var element = arguments[i];
        if (typeof element == 'string') {
            element = document.getElementById(element);
        }
        if (arguments.length == 1) {
            return element;
        }
        elements.push(element);
    }
    return elements;
}

/**
 * 封装获取元素节点的value属性值
 * 输入参数可以是1-n个节点的id属性值
 * 返回值是一个元素节点或一个包含所有元素节点的value值的数组
 */
function $V() {
    var values = new Array();
    for (var i = 0; i < arguments.length; i++) {
        var value = arguments[i];
        if (typeof value == 'string') {
            var tempnode = document.getElementById(value);
            if (tempnode == null || !(tempnode.value)) {
                value = null;
            } else {
                value = document.getElementById(value).value;
            }
        }
        if (arguments.length == 1) {
            return value;
        }
        values.push(value);
    }
    return values;
}

/**
 * 封装获取元素节点中包含的唯一文本节点内容的方法
 * 输入参数可以是1-n个节点的id属性值
 * 返回值是一个元素节点或一个包含所有元素节点内部的唯一为本节点内容的数据
 */
function $T() {
    var texts = new Array();
    for (var i = 0; i < arguments.length; i++) {
        var text = arguments[i];
        if (typeof text == 'string') {
            var temp = document.getElementById(text);
            if (temp == null) {
                text = null;
            } else {
                var tempnode = document.getElementById(text).firstChild;
                if (tempnode == null) {
                    text = null;
                } else {
                    text = tempnode.nodeValue;
                }
            }
        }
        if (arguments.length == 1) {
            return text;
        }
        texts.push(text);
    }
    return texts;
}

/**
 * 新建一个元素节点
 * @param ename 节点名称
 * @return 元素节点对象
 */
function $CE(ename) {
    return document.createElement(ename);
}

/**
 * 新建一个文本节点
 * @param content 文本节点内容
 * @return 文本节点
 */
function $CT(content) {
    return document.createTextNode(content);
}

/**
 * 公共的显示提示消息的方法，用于统一处理返回消息是普通文字和一段script脚本的问题
 * @param message 表示要被显示的消息
 */
function alertMessage(message){
    var startString = "<script type=\"text/javascript\">";
    var startIndex = message.indexOf(startString);
    var startLength = startString.length;
    var endString = "<\/script>";
    var endIndex = message.lastIndexOf(endString);
    var endLength = endString.length;

    if (startIndex >= 0 && endIndex >= 0) {
        var scriptString = message.substring(startIndex + startLength,endIndex);
        eval(scriptString);
    } else {
        alert(message);
    }
}	