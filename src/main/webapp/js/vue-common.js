
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
    template: '<div class="alert alert-block alert-success" style="padding:8px;">' +
    '<label v-for="item in hosts"' +
    '       v-bind="{class:\'infobox infobox-small infobox-dark \'+(item.current?\'infobox-green\':\'\'),style:\'cursor:pointer;margin:2px;width:210px;\'+(!item.current?\'background-color:#E08374;border-color:#E08374;\':\'\')}">' +
    '<div class="infobox-progress">' +
    '    <div class="easy-pie-chart percentage" :data-percent="item.weight" data-size="39">' +
    '        <span class="percent">{{item.weight}}</span>%' +
    '    </div>' +
    '</div>' +
    '<div class="infobox-data">' +
    '    <div class="infobox-content" style="width:153px;max-width:153px;">' +
    '        {{item.host}}' +
    '        <label class="pull-right">' +
    '            <input class="ace ace-checkbox-2" type="checkbox" v-model="item.checked">' +
    '            <span class="lbl"></span>' +
    '        </label>' +
    '    </div>' +
    '    <div class="infobox-content" style="width:153px;max-width:153px;">' +
    '        <span class="green"><i class="ace-icon fa fa-check"></i> 0000</span>' +
    '        &nbsp;&nbsp;&nbsp;&nbsp;' +
    '        <span class="red"><i class="ace-icon fa fa-bolt"></i> 0000</span>' +
    '    </div>' +
    '</div>' +
    '</label>' +
    '</div>',
    mounted: function () {
        var me = this;
        Jcoder.ajax('/admin/common/host', 'GET', {groupName: me.group}).then(function (data) {
            var hosts = me.hosts;
            _.each(data.obj, function (ele) {
                hosts.push({host: ele.hostPort, checked: ele.current, current: ele.current, weight: ele.hostPort == 'master' ? 100 : ele.weight});
            });

            Vue.nextTick(function () {
                $('.easy-pie-chart.percentage').each(function () {
                    var $box = $(this).closest('.infobox'),
                        barColor = $(this).data('color') || (!$box.hasClass('infobox-dark') ? $box.css('color') : 'rgba(255,255,255,0.95)'),
                        trackColor = barColor == 'rgba(255,255,255,0.95)' ? 'rgba(255,255,255,0.25)' : '#E2E2E2',
                        size = parseInt($(this).data('size')) || 50;
                    $(this).easyPieChart({
                        barColor: barColor,
                        trackColor: trackColor,
                        scaleColor: false,
                        lineCap: 'butt',
                        lineWidth: parseInt(size / 10),
                        animate: ace.vars['old_ie'] ? false : 1000,
                        size: size
                    });
                });
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
