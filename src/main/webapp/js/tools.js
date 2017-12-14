var Tools = {

    yyyyMMddHHmmss: 'yyyyMMddHHmmss',
    yyyyMMddHHmmss_: 'yyyy-MM-dd HH:mm:ss',
    yyyyMMddHHmm: 'yyyyMMddHHmm',
    yyyyMMddHHmm_: 'yyyy-MM-dd HH:mm',
    yyyyMMdd: 'yyyyMMdd',
    yyyyMMdd_: 'yyyy-MM-dd',
    MMdd: 'MMdd',
    MMdd_: 'MM-dd',
    HHmmss: 'HHmmss',
    HHmmss_: 'HH:mm:ss',
    HHmm: 'HHmm',
    HHmm_: 'HH:mm',

    dateFormat: function (date, formatStr) {
        var str = formatStr || this.yyyyMMddHHmm;
        var Week = ['日', '一', '二', '三', '四', '五', '六'];

        str = str.replace(/yyyy|YYYY/, date.getFullYear());
        str = str.replace(/yy|YY/, (date.getYear() % 100) > 9 ? (date.getYear() % 100).toString() : '0' + (date.getYear() % 100));

        str = str.replace(/MM/, date.getMonth() + 1 > 9 ? (date.getMonth() + 1).toString() : '0' + (date.getMonth() + 1));
        str = str.replace(/M/g, date.getMonth() + 1);

        str = str.replace(/w|W/g, Week[date.getDay()]);

        str = str.replace(/dd|DD/, date.getDate() > 9 ? date.getDate().toString() : '0' + date.getDate());
        str = str.replace(/d|D/g, date.getDate());

        str = str.replace(/hh|HH/, date.getHours() > 9 ? date.getHours().toString() : '0' + date.getHours());
        str = str.replace(/h|H/g, date.getHours());
        str = str.replace(/mm/, date.getMinutes() > 9 ? date.getMinutes().toString() : '0' + date.getMinutes());
        str = str.replace(/m/g, date.getMinutes());

        str = str.replace(/ss|SS/, date.getSeconds() > 9 ? date.getSeconds().toString() : '0' + date.getSeconds());
        str = str.replace(/s|S/g, date.getSeconds());

        return str;
    },
    dateParse: function (dateStr, formatStr) {
        var yyyy = 0, MM = 0, dd = 0, HH = 0, mm = 0, ss = 0;
        var format = formatStr || this.yyyyMMddHHmm;

        if (format == this.yyyyMMddHHmmss) {
            yyyy = dateStr.substring(0, 4), MM = dateStr.substring(4, 6), dd = dateStr.substring(6, 8), HH = dateStr.substring(8, 10), mm = dateStr.substring(10, 12), ss = dateStr.substring(12, 14)
        } else if (format == this.yyyyMMddHHmmss_) {
            yyyy = dateStr.substring(0, 4), MM = dateStr.substring(5, 7), dd = dateStr.substring(8, 10), HH = dateStr.substring(11, 13), mm = dateStr.substring(14, 16), ss = dateStr.substring(17, 19)
        } else if (format == this.yyyyMMddHHmm) {
            yyyy = dateStr.substring(0, 4), MM = dateStr.substring(4, 6), dd = dateStr.substring(6, 8), HH = dateStr.substring(8, 10), mm = dateStr.substring(10, 12)
        } else if (format == this.yyyyMMddHHmm_) {
            yyyy = dateStr.substring(0, 4), MM = dateStr.substring(5, 7), dd = dateStr.substring(8, 10), HH = dateStr.substring(11, 13), mm = dateStr.substring(14, 16)
        } else if (format == this.yyyyMMdd) {
            yyyy = dateStr.substring(0, 4), MM = dateStr.substring(4, 6), dd = dateStr.substring(6, 8)
        } else if (format == this.yyyyMMdd_) {
            yyyy = dateStr.substring(0, 4), MM = dateStr.substring(5, 7), dd = dateStr.substring(8, 10)
        }
        return new Date(yyyy, MM, dd, HH, mm, ss);
    },
    dateAdd: function (date, n) {
        var time = date.valueOf();
        time += n * 1000;
        return new Date(time);
    },

    formatBytes: function (bytes, decimals) {
        if (!_.isNumber(bytes) || bytes < 0 || (decimals = parseInt(decimals) || 0) < 0) return null;

        if (bytes == 0) return '0';

        var sizes = ['B', 'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y'],
            i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024))),
            f = String((bytes / Math.pow(1024, i)).toFixed(decimals));

        return (f.indexOf('.' + new Array(decimals + 1).join('0'), f.length - (decimals + 1)) !== -1 ? f.slice(0, -decimals - 1) : f) + sizes[i];
    },

    /*
     * LOAD SCRIPTS
     * Usage:
     * Define function = myPrettyCode ()...
     * loadScript("js/my_lovely_script.js", myPrettyCode);
     */
    jsArray: "",
    loadScript: function (scriptName, callback) {
        if (this.jsArray.indexOf("[" + scriptName + "]") == -1) {

            //List of files added in the form "[filename1],[filename2],etc"
            this.jsArray += "[" + scriptName + "]";

            // adding the script tag to the head as suggested before
            var body = document.getElementsByTagName('body')[0];
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = scriptName;

            // then bind the event to the callback function
            // there are several events for cross browser compatibility
            //script.onreadystatechange = callback;
            script.onload = callback;

            // fire the loading
            body.appendChild(script);

        } else if (callback) { // changed else to else if(callback)
            //console.log("JS file already added!");
            //execute function
            callback();
        }
    },
    // LOAD AJAX PAGES
    loadURL: function (url, container, successCallback, errorCallback) {
        $.ajax({
            type: "GET",
            url: url,
            dataType: 'html',
            cache: false, // (warning: this will cause a timestamp and will call the request twice)
            beforeSend: function () {
                container.html('<h1><i class="fa fa-cog fa-spin"></i> Loading...</h1>');
            },
            success: function (data) {
                container.css({
                    opacity: '0.0'
                })
                    .html(data)
                    .delay(100)
                    .animate({
                        opacity: '1.0'
                    }, 300);

                if (successCallback) successCallback();
            },
            error: function (xhr, ajaxOptions, thrownError) {
                container.html(
                    '<h4 style="margin-top:10px; display:block; text-align:left"><i class="fa fa-warning txt-color-orangeDark"></i> Error 404! Page not found.</h4>'
                );

                if (errorCallback) errorCallback();
            },
            async: false
        });
    },

    parseQuery: function (url) {
        var urls = url.split('?');
        return urls.length > 1 ? _.chain(urls[1].split('&')).map(function (value) {
            var arr = value.split('=');
            return [decodeURIComponent(arr[0]), decodeURIComponent(arr[1])];
        }).object().value() : {};
    }
};

function isNotBlank(val) {
    if (val != null && val != "null" && val != undefined && val != "") {
        return true;
    } else {
        return false;
    }
}
/**
 * 整除
 *
 * @param exp1
 * @param exp2
 * @returns {Number}
 */
function DivMath(exp1, exp2) {
    var n1 = Math.round(exp1); // 四舍五入
    var n2 = Math.round(exp2); // 四舍五入
    var rslt = n1 / n2; // 除
    if (rslt >= 0) {
        rslt = Math.floor(rslt); // 返回小于等于原rslt的最大整数。
    } else {
        rslt = Math.ceil(rslt); // 返回大于等于原rslt的最小整数。
    }
    return rslt;
}

/**
 * 列表分页
 *
 * @param divId
 * @param currentPage
 *            当前页
 * @param pageSize
 *            每页数量
 * @param totalNumber
 *            总页数
 * @param func
 *            方法
 * @param view
 *            显示页数
 */
function htmlPage(divId, currentPage, totalNumber, func, pageSize) {
    currentPage = parseInt(currentPage);
    totalNumber = parseInt(totalNumber);
    if(pageSize == undefined) pageSize = parseInt($('#pageSelect').val());
    pageSize = parseInt(pageSize);
    // 如果没有数据，显示共一页
    if (totalNumber == 0) {
        totalNumber = 0;
    }
    var totalPage = Math.floor((totalNumber + pageSize -1)/pageSize);
    // 分页数
    var showPage = '<select id="pageSelect" class="form-control input-sm" onchange="'+ func +'(1);" style="width: 55px; float: left; margin: 3px 0px; height: 25px; ">' +
        '<option value="5">5</option><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option>' +
        '</select>';
    // 总条数显示
    var stotal = (currentPage-1)*pageSize;
    var etotal = currentPage*pageSize;
    if(totalNumber < pageSize) etotal = totalNumber;
    stotal++;
    showPage += '<div style="float: left; margin: 5px 15px;">显示'+ stotal +'-'+ Math.min(etotal, totalNumber)+'条，共<span> '
        + Math.floor(totalNumber) + ' </span>条<input id="currentPage" value="'+ currentPage +'" type="hidden"/></div>';
    if(totalNumber <= pageSize) {
        // 如果总条数小于分页数
        $("#" + divId).html(showPage);
        $('#pageSelect').val(pageSize);
        if(pageSize != undefined) $('#pageSelect').val(pageSize);
        return;
    }
    /**
     * 分页详细
     */
    var view = 5;// 显示多少个页码
    if (!isNotBlank(view)) {
        view = 5;
    }
    showPage += '<ul class="pagination pull-right no-margin">';
    // 首页
    if (totalPage > view && currentPage > Math.floor(view/2)+1) {
        showPage += "<li class=\"paginate_button\"><a href=\"javascript:void(0)\" ";
        showPage += "onclick=\"" + func + "(1)\">首页</a></li>";
    }
    var s = 0; // 从第几个页码开始
    var e = 0; // 到第几个页码结束
    if (Math.floor(currentPage) <= DivMath(view, 2) + 1) {
        s = 1;
    } else {
        s = Math.floor(currentPage) - DivMath(view, 2);
    }
    if (s + view - 1 >= Math.floor(totalNumber)) {
        e = Math.floor(totalNumber);
    } else {
        e = s + view - 1;
    }
    if (e >= totalPage) {
        s = s-(e-totalPage);
        e = totalPage;
    }
    if (s <= 0) {
        s = 1;
    }
    // 中间页数
    for ( var i = s; i <= e; i++) {
        if (Math.floor(currentPage) == i) {
            showPage += ' <li class="paginate_button active"><a href="javascript:void(0);" onclick="'
                + func + '(' + i + ')">' + (i) + '</a></li>';
        } else {
            if(i <= totalPage){
                showPage += ' <li class="paginate_button"><a href="javascript:void(0);" onclick="' + func + '('
                    + i + ')">' + (i) + '</a></li>';
            }
        }
    }
    if (totalPage > view && e < totalPage) {
        showPage += "<li class=\"paginate_button\"><a href=\"javascript:void(0)\" ";
        showPage += 'onclick=\"' + func + '(' + totalPage + ')\">尾页</a></li>';
    }
    showPage += '</ul>';
    $("#" + divId).html(showPage);
    $('#pageSelect').val(pageSize);
    $('#currentPage').val(currentPage);
}

/**
 * 列表分页
 *
 * @param divId
 * @param currentPage
 *            当前页
 * @param pageSize
 *            每页数量
 * @param totalNumber
 *            总页数
 * @param func
 *            方法
 * @param view
 *            显示页数
 * @message 显示上一页，下一页
 */
function htmlPage2(divId, currentPage, pageSize, totalNumber, func) {
    currentPage = parseInt(currentPage);
    pageSize = parseInt(pageSize);
    totalNumber = parseInt(totalNumber);
    // 如果没有数据，显示共一页
    if (totalNumber == 0) {
        totalNumber = 1;
    }
    var totalPage = Math.floor((totalNumber + pageSize -1)/pageSize);
    // 分页数
    var showPage = '<select id="pageSelect" class="form-control input-sm" onchange="'+ func +'(1);" style="width: 55px; float: left; margin: 3px 0px; height: 25px; ">' +
        '<option value="5">5</option><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option>' +
        '</select>';
    // 总条数显示
    var stotal = (currentPage-1)*pageSize;
        stotal++;
    showPage += '<div style="float: left; margin: 5px 15px;">显示'+ stotal +'-'+ currentPage*pageSize +'条，共<span> '
        + Math.floor(totalNumber) + ' </span>条<input id="currentPage" value="'+ currentPage +'" type="hidden"/></div>';
    // 分页详细
    showPage += '<ul class="pagination pull-right no-margin">';
    // 向前翻页
    if (currentPage > 1) {
        showPage += "<li class=\"paginate_button\"><a href=\"javascript:void(0)\" ";
        showPage += "onclick=\"" + func + "(" + (currentPage - 1) + ")\">上一页</a></li>";
    }
    if (currentPage == 1) {
        showPage += "<li class=\"paginate_button\"><a href=\"javascript:void(0)\" >上一页</a></li>";
    }
    // var view =10; // 显示多少个页码
    var view = 5;
    if (!isNotBlank(view)) {
        view = 5;
    }
    var s = 0; // 从第几个页码开始
    var e = 0; // 到第几个页码结束
    if (Math.floor(currentPage) <= DivMath(view, 2) + 1) {
        s = 1;
    } else {
        s = Math.floor(currentPage) - DivMath(view, 2);
    }
    if (s + view - 1 >= Math.floor(totalNumber)) {
        e = Math.floor(totalNumber);
    } else {
        e = s + view - 1;
    }
    if (e >= Math.floor(totalNumber)) {
        s = e - view + 1;
    }
    if (s <= 0) {
        s = 1;
    }
    if(parseInt(s) > 2){
        // 最后一页
        showPage += '<li class="paginate_button"><a href="javascript:void(0);" onclick="' + func + '('
            + 1 + ')">' + (1) + '</a></li>';
        showPage += '<li class="paginate_button"><a href="javascript:void(0);">...</a></li>';
    }
    // 中间页数
    for ( var i = s; i <= e; i++) {
        if (Math.floor(currentPage) == i) {
            showPage += ' <li class="paginate_button active"><a href="javascript:void(0);" onclick="'
                + func + '(' + i + ')">' + (i) + '</a></li>';
        } else {
            if(i <= totalPage){
                showPage += ' <li class="paginate_button"><a href="javascript:void(0);" onclick="' + func + '('
                    + i + ')">' + (i) + '</a></li>';
            }
        }
    }

    if(parseInt(e) < totalPage){
        // 最后一页
        showPage += '<li class="paginate_button"><a href="javascript:void(0);">...</a></li>';
        showPage += '<li class="paginate_button"><a href="javascript:void(0);" onclick="' + func + '('
            + totalPage + ')">' + (totalPage) + '</a></li>';
    }
    // 向后翻页
    if (currentPage < totalPage) {
        showPage += "<li class=\"paginate_button\"><a href=\"javascript:void(0)\" ";
        showPage += 'onclick=\"' + func + '(' + (Math.floor(currentPage) + 1)
            + ')\">下一页</a></li>';
    }
    if (currentPage == totalPage) {
        showPage += '<li class=\"paginate_button\"><a href=\"javascript:void(0)\">下一页</a></li>';
    }
    showPage += '</ul>';
    $("#" + divId).html(showPage);
    $('#pageSelect').val(pageSize);
}
