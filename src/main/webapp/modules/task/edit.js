vmApp.module = new Vue({
    el: '#vmTaskEditModule',

    data: {
        hosts: [],
        groups: [],

        sourceHost: "master",
        task: {
            type: 1,
            status: 0,
            groupName: param.group
        },

        editor: null
    },

    mounted: function () {
        var me = this;

        // 如果是编辑, 加载任务
        if (me.task.name = param.name) {
            me.loadTask();
        }

        Vue.nextTick(function () {
            (me.editor = CodeMirror.fromTextArea(document.getElementById("code"), {
                lineNumbers: true,
                mode: "text/x-java",
                matchBrackets: true,
                theme: "monokai",
                showCursorWhenSelecting: true
            })).setSize($(me.$el).find('form:first').width() - 23);
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
                var task = data.obj;
                if (_.isNumber(task.id)) {
                    me.editor.setValue((me.task = task).code);
                } else {
                    me.editor.setValue((me.task = {type: 1, status: 0, groupName: me.task.group}).code);
                    JqdeBox.message("warning", "主机 " + me.sourceHost + " 不存在任务 " + t.name + " ，请选择其他版本！");
                }
            }).catch(function (req) {
                JqdeBox.unloading();
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

            JqdeBox.confirm("确定保存修改 ？", function (confirm) {
                if (!confirm) return;

                JqdeBox.loading();
                Jcoder.ajax('/admin/task/save', 'POST', {hosts: hosts, task: task}).then(function (data) {
                    JqdeBox.unloading();
                    setTimeout(function () {
                        location.hash = '/task/list.html?name=' + task.groupName;
                    }, 200);
                }).catch(function (req) {
                    JqdeBox.unloading();
                    JqdeBox.message(false, req.responseText);
                });
            });
        }
    }
});
