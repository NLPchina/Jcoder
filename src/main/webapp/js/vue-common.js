
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

/** HOST组件 */
Vue.component('host-component', {
    props: ['group', 'hosts'],
    template: '<div class="alert alert-block alert-success" style="padding:8px;height:50px;">' +
    '<span v-for="item in hosts" v-bind="{class:item.isCurrent?\'label label-xlg label-info label-white\':\'label label-xlg label-danger label-white\'}" style="margin:2px;text-align:left;">' +
    '<label>' +
    '    <input name="form-field-checkbox" class="ace ace-checkbox-2" type="checkbox" v-model="item.checked">' +
    '    <span v-if="item.host==\'master\'" class="lbl" style="width:163px;" :title="item.host"> <i v-for="n in 5" style="margin-left:8px;font-size:18px;" class="star-on-png"></i></span>' +
    '    <span v-else class="lbl" style="width:163px;"> {{item.host}}</span>' +
    '</label>' +
    '</span></div>',
    mounted: function () {
        var me = this;
        Jcoder.ajax('/admin/common/host', 'GET', {groupName: me.group}).then(function (data) {
            _.each(data.obj, function (ele) {
                me.hosts.push({host: ele.hostPort, checked: ele.current, isCurrent: ele.current});
            });
        }).catch(function (req) {
            JqdeBox.message(false, req.responseText);
        });
    }
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
