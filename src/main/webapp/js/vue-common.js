
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
    props: ['groupName', 'hosts'],
    data: function () {return {isLoading: true};},
    template: '<div class="alert alert-block alert-success host-component" style="padding:8px;">' +
    '<i v-if="isLoading" class="ace-icon fa fa-spinner fa-spin orange bigger-140"></i>' +
    '<div v-else v-on:click="select(item)" v-for="item in hosts"' +
    '       v-bind="{class:\'infobox infobox-small infobox-dark \'+(item.selected?\'infobox-blue\':(item.current?\'infobox-green\':\'\')),style:\'cursor:pointer;margin:2px;width:210px;\'+(!item.selected&&!item.current?\'background-color:#E08374;border-color:#E08374;\':\'\')}">' +
    '<div class="infobox-progress">' +
    '    <div class="easy-pie-chart percentage" :data-percent="item.weight" data-size="39">' +
    '        <span class="percent">{{item.weight}}</span>%' +
    '    </div>' +
    '</div>' +
    '<div class="infobox-data">' +
    '    <div class="infobox-content">' +
    '        {{item.host}}' +
    '    </div>' +
    '</div>' +
    '</div>' +
    '</div>',
    mounted: function () {
        var me = this;
        Jcoder.ajax('/admin/common/host', 'GET', {groupName: me.groupName}).then(function (data) {
            me.isLoading = false;

            data = data.obj;
            var hosts = (me.hosts = me.hosts || []), sum = _.chain(data).pluck("weight").reduce(function (memo, num) {return memo + num;}, 0).value(), host = param.host || "master";
            if (sum == 0) sum = 1;
            _.each(data, function (ele) {
                hosts.push({host: ele.hostPort, selected: host == "master" ? ele.current : host == ele.hostPort, current: ele.current, weight: ele.hostPort == "master" ? 100 : (ele.weight / sum * 100).toFixed(0)});
            });

            Vue.nextTick(function () {
                $(".host-component").find('.easy-pie-chart.percentage').each(function () {
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
            JqdeBox.message(false, req.responseText || req.message);
        });
    },
    methods: {
        select: function (ele) {
            if (ele.selected) {
                if (1 < _.filter(this.hosts, function (ele) {return ele.current && ele.selected;}).length) {
                    ele.selected = false;
                }
                return;
            }

            if (ele.current) {
                var changed = _.some(this.hosts, function (ele) {return !ele.current && ele.selected;});
                _.each(this.hosts, function (ele) {ele.selected = !!ele.current;});
                if (!changed) return;
            } else {
                var host = ele.host;
                _.each(this.hosts, function (ele) {ele.selected = ele.host == host;});
            }

            this.$emit('change', ele);
        },

        diff: function (hosts) {
            var me = this;
            Jcoder.ajax('/admin/common/host', 'GET', {groupName: me.groupName}).then(function (data) {
                hosts = hosts || [];
                data = _.reduce(data.obj, function (memo, ele) {
                    if (hosts.length < 1 || _.contains(hosts, ele.hostPort)) {
                        memo[ele.hostPort] = ele.current;
                    }
                    return memo;
                }, {});
                _.each(me.hosts, function (ele) {
                    if (_.isBoolean(data[ele.host])) {
                        ele.current = !!data[ele.host];
                    }
                });
            }).catch(function (req) {
                JqdeBox.message(false, req.responseText || req.message);
            });
        }
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
