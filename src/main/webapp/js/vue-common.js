
Vue.config.debug = Config.debug;
Vue.config.devtools = Config.devtools;
Vue.config.silent = Config.silent;

// 时间格式化
Vue.filter('datetime', function (value, format) {

    format = format || Tools.yyyyMMddHHmmss_;

    if(typeof(value) == 'number'){
        value = new Date(value);
    }
    return Tools.dateFormat(value, format);
});

// 时间格式化
Vue.filter('stemming', function (value, format) {

    if(value == '0'){
        return '不提取';
    }
    return '提取';
});

// 数字格式化
Vue.filter('formatNumber', function (value, decimals) {
    value = parseFloat(value);
    if (!isFinite(value) || (!value && value !== 0)) return '';

    decimals = decimals != null ? decimals : 0;
    var stringified = Math.abs(value).toFixed(decimals), _int = decimals ? stringified.slice(0, -1 - decimals) : stringified, i = _int.length % 3;
    return value < 0 ? '-' : '' + (i > 0 ? (_int.slice(0, i) + (_int.length > 3 ? ',' : '')) : '') +
        _int.slice(i).replace(/(\d{3})(?=\d)/g, '$1,') + (decimals ? stringified.slice(-1 - decimals) : '');
});

function qdeloading(divid){
    var msg = '.';
    $('#'+divid).html('<font size="6" color="#438EB9" style="margin-left: 70px;">'+msg+'</font>');
    var indx = 1;
    var loadInterval = setInterval(function(){
        if(indx%6 == 0) {
            msg = '.';
        } else msg += ' .';
        $('#'+divid).html('<font size="6" color="#438EB9" style="margin-left: 70px;">'+msg+'</font>');
        indx++;
    },1000);
    return loadInterval;
}
