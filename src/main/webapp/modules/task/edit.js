vmApp.module = new Vue({
    el: '#vmTaskEditModule',

    components: {
        'host-component': {
            props: ['hosts'],
            data: function () {return {isLoading: true};},
            template: '#host-component',
            mounted: function () {
                var me = this, parent = me.$parent, task = parent.task;
                Jcoder.ajax('/admin/task/statistics', 'GET', {
                    groupName: task.groupName,
                    name: task.name
                }).then(function (data) {
                    me.isLoading = false;

                    data = data.obj;
                    var hosts = me.hosts, selectedHosts = parent.selectedHosts;
                    _.each(data, function (ele) {
                        var current = ele.hostGroup ? ele.hostGroup.current : true;
                        hosts.push({
                            host: ele.hostPort,
                            checked: selectedHosts.length == 1 && selectedHosts[0] == "master" ? current : _.contains(selectedHosts, ele.hostPort),
                            current: current,
                            weight: (ele.weight / ele.sumWeight * 100).toFixed(0),
                            success: ele.success,
                            error: ele.error
                        });
                    });

                    Vue.nextTick(function () {
                        $(parent.$el).find('.easy-pie-chart.percentage').each(function () {
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
        }
    },

    data: function () {
        var hosts = param.host.split(",");
        return {
            hosts: [],

            sourceHost: hosts[0],
            sourceHosts: [],
            selectedHosts: hosts,

            task: {
                type: 1,
                status: 0,
                groupName: param.group,
                name: param.name
            },

            editor: null
        };
    },

    mounted: function () {
        var me = this;

        // 如果是编辑
        if (me.task.name) {
            // 加载任务
            me.loadTask();

            // 加载任务对应的主机列表
            me.loadSourceHosts();
        }

        Vue.nextTick(function () {
            (me.editor = CodeMirror.fromTextArea(document.getElementById("code"), {
                lineNumbers: true,
                mode: "text/x-java",
                matchBrackets: true,
                theme: "monokai",
                showCursorWhenSelecting: true
            })).setSize($(me.$el).find('form:first').width() - 37);
        });
    },

    methods: {

        loadTask: function () {
            var me = this, t = me.task;
            JqdeBox.loading();
            Jcoder.ajax('/admin/task/task', 'POST', {
                groupName: t.groupName,
                name: t.name,
                sourceHost: me.sourceHost
            }).then(function (data) {
                JqdeBox.unloading();

                //
                var task = data.obj || {};
                if (_.isNumber(task.id)) {
                    task.type = $.trim(task.scheduleStr) ? 2 : 1;
                    me.editor.setValue((me.task = task).code);
                } else {
                    me.task = {type: 1, status: 0, groupName: t.groupName, name: t.name};
                    me.editor.setValue("");
                    JqdeBox.message("warning", me.sourceHost + " 不存在任务 " + t.name + " ，请选择其他版本！");
                }
            }).catch(function (req) {
                JqdeBox.unloading();
                JqdeBox.message(false, req.responseText);
            });
        },

        loadSourceHosts: function () {
            var me = this, t = me.task;
            Jcoder.ajax('/admin/task/host/list', 'GET', {groupName: t.groupName, name: t.name})
                .then(function (data) {
                    me.sourceHosts = data.obj;
                })
                .catch(function (req) {
                    JqdeBox.message(false, req.responseText);
                });
        },

        /**
         * 保存表单
         */
        save: function () {
            var me = this, code = me.editor.getValue(), task = me.task;

            if (task.type == 2) {
                if (!task.scheduleStr) {
                    return JqdeBox.alert("请填写Cron表达式 ！");
                }
            } else {
                // 如果不是例行性任务
                task.scheduleStr = "";
            }

            if (!code.trim()) {
                return JqdeBox.alert("请填写代码 ！");
            } else {
                task.code = code;
            }

            // hosts的处理
            var hosts = _.chain(me.hosts).where({checked: true}).pluck('host').value();
            if (!hosts || hosts.length < 1) {
                return JqdeBox.alert("请选择主机 ！");
            }
            if (hosts.length == 1 && hosts[0].trim().toLowerCase() == 'master') {
                return JqdeBox.alert("请至少选择一个非主版本的主机 ！");
            }

            JqdeBox.confirm("确定保存修改 ？", function (confirm) {
                if (!confirm) return;

                JqdeBox.loading();
                Jcoder.ajax('/admin/task/save', 'POST', {hosts: hosts, task: JSON.stringify($.extend({}, task, {name: null})), oldName: task.name}).then(function (data) {
                    JqdeBox.unloading();
                    JqdeBox.message(true, "修改任务 " + task.name + " 成功！");
                    setTimeout(function () {
                        location.hash = "/task/edit.html?group=" + task.groupName + "&host=" + hosts.join(",") + "&name=" + data.obj;
                    }, 200);
                }).catch(function (req) {
                    JqdeBox.unloading();
                    JqdeBox.message(false, req.responseText);
                });
            });
        }
    }
});
