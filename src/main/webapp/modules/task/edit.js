vmApp.module = new Vue({
    el: '#vmTaskEditModule',

    data: {
        hosts: [],
        groups: [],
        task: {
            type: 1,
            status: 0,
            groupName: param.group
        },

        editor: null
    },

    mounted: function () {
        var me = this;

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

            JqdeBox.confirm("确定保存修改 ？", function (confirm) {
                if (!confirm) return;

                // hosts的处理
                var hosts = _.chain(me.hosts).where({checked: true}).pluck('host').value();

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
