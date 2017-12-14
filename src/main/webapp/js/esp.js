!function (a, b) {
    "use strict";
    "function" == typeof define && define.amd ? define(["jquery"], b) : "object" == typeof exports ? module.exports = b(require("jquery")) : a.ESP = b(a.jQuery)
}(this, function ($) {
    "use strict";

    var docCookies = {
        getItem: function (sKey) {
            return decodeURIComponent(document.cookie.replace(new RegExp("(?:(?:^|.*;)\\s*" + encodeURIComponent(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=\\s*([^;]*).*$)|^.*$"), "$1")) || null;
        },
        setItem: function (sKey, sValue, vEnd, sPath, sDomain, bSecure) {
            if (!sKey || /^(?:expires|max\-age|path|domain|secure)$/i.test(sKey)) {
                return false;
            }
            var sExpires = "";
            if (vEnd) {
                switch (vEnd.constructor) {
                    case Number:
                        sExpires = vEnd === Infinity ? "; expires=Fri, 31 Dec 9999 23:59:59 GMT" : "; max-age=" + vEnd;
                        break;
                    case String:
                        sExpires = "; expires=" + vEnd;
                        break;
                    case Date:
                        sExpires = "; expires=" + vEnd.toUTCString();
                        break;
                }
            }
            document.cookie = encodeURIComponent(sKey) + "=" + encodeURIComponent(sValue) + sExpires + (sDomain ? "; domain=" + sDomain : "") + (sPath ? "; path=" + sPath : "") + (bSecure ? "; secure" : "");
            return true;
        },
        removeItem: function (sKey, sPath, sDomain) {
            if (!sKey || !this.hasItem(sKey)) {
                return false;
            }
            document.cookie = encodeURIComponent(sKey) + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT" + ( sDomain ? "; domain=" + sDomain : "") + ( sPath ? "; path=" + sPath : "");
            return true;
        },
        hasItem: function (sKey) {
            return (new RegExp("(?:^|;\\s*)" + encodeURIComponent(sKey).replace(/[\-\.\+\*]/g, "\\$&") + "\\s*\\=")).test(document.cookie);
        }
    }, anonymousUser = 'anonymous';

    /** 登录 */
    function login(baseUri, user, password, verificationCode, cb) {
        user = user || anonymousUser;
        var params = {name: user, password: password, cookie: document.cookie};
        $.isFunction(verificationCode) ? (cb = verificationCode) : $.extend(params, {verificationCode: verificationCode});
        $.support.cors = true;
        $.post(baseUri + '/api/PmpUserApi/login?_=' + Date.now(), params).done(function (data, textStatus, jqXHR) {
            var result = data.obj;
            docCookies.setItem('ESP_USER', result.name);
            docCookies.setItem('ESP_PASSWORD', password);
            docCookies.setItem('ESP_TOKEN', result.token);
            $.isFunction(cb) && cb(data);
        }).fail(function (jqXHR, textStatus, errorThrown) {
            $.isFunction(cb) && cb({'ok': false, 'message': jqXHR.responseText});
        });
    }

    /** 退出 */
    function logout(baseUri, cb) {
        $.support.cors = true;
        $.ajax({
            method: 'POST',
            headers: {authorization: docCookies.getItem('ESP_TOKEN')},
            data: {cookie: document.cookie},
            url: baseUri + '/api/PmpUserApi/logout?_=' + Date.now()
        }).done(function (data, textStatus, jqXHR) {
            docCookies.removeItem('ESP_USER');
            docCookies.removeItem('ESP_PASSWORD');
            docCookies.removeItem('ESP_TOKEN');
            $.isFunction(cb) && cb(data);
        }).fail(function (jqXHR, textStatus, errorThrown) {
            $.isFunction(cb) && cb(jqXHR.status === 450 ? {'ok': true} : {'ok': false, 'message': jqXHR.responseText});
        });
    }

    function request(baseUri, method, className, methodName, params, cb) {
        var strs = method.split("_");
        $.support.cors = true;
        $.ajax({
            async: 1 === strs.length,
            method: strs[strs.length - 1],
            headers: {authorization: docCookies.getItem('ESP_TOKEN')},
            data: params,
            url: baseUri + '/api/' + className + '/' + methodName + '?_=' + Date.now()
        }).done(function (data, textStatus, jqXHR) {
            $.isFunction(cb) && cb(data);
        }).fail(function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status === 450) {
                login(baseUri, docCookies.getItem('ESP_USER'), docCookies.getItem('ESP_PASSWORD'), function (data) {
                    data.ok ? request(baseUri, method, className, methodName, params, cb) : ($.isFunction(cb) && cb(data));
                });
            } else {
                $.isFunction(cb) && cb({'ok': false, 'message': jqXHR.responseText});
            }
        });
    }

    /** ESP系统日志记录: 0-登录;1-退出;2-搜索;3-浏览;4-收藏;5-下载 */
    var log = {};
    $.each(['login', 'logout', 'search', 'browse', 'collect', 'download'], function (index, ele) {
        log[ele] = function (baseUri, params, cb) {
            request(baseUri, 'POST', 'PmpUserApi', 'log', {
                type: index,
                jsonStr: JSON.stringify($.extend({cookie: document.cookie, username: docCookies.getItem('ESP_USER')}, params))
            }, cb);
        };
    });

    /** 发送HTTP请求 */
    var http = {};
    $.each(['get', 'sync_get', 'post', 'sync_post'], function (index, ele) {
        http[ele] = function (baseUri, className, methodName, params, cb) {
            request(baseUri, ele.toUpperCase(), className, methodName, params, cb);
        };
    });

    return {login: login, logout: logout, log: log, http: http};
});
