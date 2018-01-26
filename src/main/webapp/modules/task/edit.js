vmApp.module = new Vue({
    el: '#vmTaskEditModule',

    components: {
        'host-component': {
            props: ['hosts'],
            data: function () {
                return {isLoading: true};
            },
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
            me.editor = CodeMirror.fromTextArea(document.getElementById("code"), {
                lineNumbers: true,
                mode: "text/x-java",
                matchBrackets: true,
                theme: "monokai",
                showCursorWhenSelecting: true
            });
            me.editor.setSize(null,document.documentElement.clientHeight-400);//设置高度
            var $this = $(me.$el);
            $this.find(".CodeMirror").resizable({
                resize: function () {
                    me.editor.setSize($this.find("form:first").width() - 37);
                }
            });
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
                Jcoder.ajax('/admin/task/save', 'POST', {
                    hosts: hosts,
                    task: JSON.stringify($.extend({}, task, {name: null})),
                    oldName: task.name
                }).then(function (data) {
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
        },

        diff: function () {
            var me = this, t = me.task, editor = me.editor;
            JqdeBox.dialog({
                title: "<span class='blue' style='font-size:80%;'><i class='ace-icon fa fa-retweet bigger-130'></i> " +
                me.sourceHost + " <i class='ace-icon fa fa-angle-right'></i> " +
                t.groupName + " <i class='ace-icon fa fa-angle-right'></i> " +
                t.name + "</span>",
                buttons: {cancel: {label: '<i class="fa fa-times"></i> 关闭', className: 'btn-sm'}},
                init: function (dlg) {
                    //$(".bootbox-body").css("height",(document.documentElement.clientHeight-400)+"px")
                    $.get('modules/task/task_diff.html?_=' + _.now(), function (html) {
                        dlg.on('hide.bs.modal', function (e) {
                            $('#mergely').mergely('destroy');
                        }).on('hidden.bs.modal', function (e) {
                            editor.refresh();
                        }).find('.modal-dialog').css('width', '80%').find('.bootbox-body').html(html);

                        //
                        var h = me.sourceHost;
                        new Vue({
                            el: '#vmTaskDiffModule',
                            data: {
                                hosts: me.sourceHosts,
                                lhs_host: h,
                                lhs_version: "Current",
                                rhs_host: h,
                                rhs_version: "Current",
                                tasks: _.object([[h, [{version: "Current", code: editor.getValue()}]]])
                            },
                            mounted: function () {
                                $('#mergely').mergely({
                                    width: "auto",
                                    height: 'auto',
                                    cmsettings: {mode: "text/x-java", theme: "monokai", readOnly: false}
                                });
                                $('#mergely').resize();

                                this.init();
                            },
                            methods: {
                                init: function () {
                                    $('#mergely').mergely("lhs", editor.getValue());
                                    if (h == "master") return $('#mergely').mergely("rhs", editor.getValue());

                                    var self = this, tasks = self.tasks;
                                    JqdeBox.loading();
                                    Jcoder.ajax('/admin/task/version', 'POST', {
                                        host: h,
                                        groupName: t.groupName,
                                        name: t.name
                                    }).then(function (data) {
                                        data = data.obj;
                                        tasks[h] = Array.prototype.concat(tasks[h], _.map(data, function (ele) {
                                            return {version: ele, code: ""};
                                        }));

                                        Jcoder.ajax('/admin/task/history', 'POST', {
                                            host: h,
                                            groupName: t.groupName,
                                            name: t.name,
                                            version: data[0]
                                        }).then(function (ret) {
                                            JqdeBox.unloading();
                                            self.rhs_version = data[0];
                                            $('#mergely').mergely("rhs", tasks[h][1].code = ret.obj);
                                        }).catch(function (req) {
                                            JqdeBox.unloading();
                                            JqdeBox.message(false, req.responseText);
                                        });
                                    }).catch(function (req) {
                                        JqdeBox.unloading();
                                        JqdeBox.message(false, req.responseText);
                                    })
                                },

                                changeHost: function (side) {
                                    var self = this, tasks = self.tasks, hKey = side + "_host",
                                        vKey = side + "_version";

                                    if (tasks[self[hKey]]) {
                                        self[vKey] = tasks[self[hKey]][0].version;
                                        return self.changeVersion(side);
                                    }

                                    // 如果主机是master, 无历史版本
                                    if (self[hKey] == "master") {
                                        tasks["master"] = [{version: self[vKey] = "Current", code: ""}];
                                        return self.changeVersion(side);
                                    }

                                    JqdeBox.loading();
                                    Jcoder.ajax('/admin/task/version', 'POST', {
                                        host: self[hKey],
                                        groupName: t.groupName,
                                        name: t.name
                                    }).then(function (data) {
                                        JqdeBox.unloading();
                                        tasks[self[hKey]] = Array.prototype.concat(tasks[me[hKey]] || [], _.map(data.obj, function (ele) {
                                            return {version: ele, code: ""};
                                        }));

                                        self[vKey] = data.obj[0];
                                        self.changeVersion(side);
                                    }).catch(function (req) {
                                        JqdeBox.unloading();
                                        JqdeBox.message(false, req.responseText);
                                    });
                                },

                                changeVersion: function (side) {
                                    var self = this, hKey = side + "_host", vKey = side + "_version",
                                        history = _.findWhere(self.tasks[self[hKey]], {version: self[vKey]});
                                    if (history.code) return $('#mergely').mergely(side, history.code);

                                    JqdeBox.loading();
                                    Jcoder.ajax('/admin/task/history', 'POST', {
                                        host: self[hKey],
                                        groupName: t.groupName,
                                        name: t.name,
                                        version: self[vKey]
                                    }).then(function (data) {
                                        JqdeBox.unloading();
                                        $('#mergely').mergely(side, history.code = data.obj);
                                    }).catch(function (req) {
                                        JqdeBox.unloading();
                                        JqdeBox.message(false, req.responseText);
                                    });
                                }
                            }
                        });
                    });
                }
            });
        }
    }
});
