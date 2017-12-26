vmApp.module = new Vue({
    el: '#vmTaskModule',

    components: {
        'task-component': {
            props: ['type'],

            data: function () {
                return {tasks: null};
            },

            template: '#task-template',

            mounted: function () {
                $('.task-table').on('click', '.show-details-btn', function (e) {
                    e.preventDefault();
                    $(this).closest('tr').next().toggleClass('open');
                    $(this).find(ace.vars['.icon']).toggleClass('fa-angle-double-down').toggleClass('fa-angle-double-up');
                });

                this.loadTasks();
            },

            methods: {
                loadTasks: function () {
                    var me = this;
                    return Jcoder.ajax('/admin/task/list', 'GET', {
                        groupName: me.$parent.groupName,
                        taskType: me.type
                    }).then(function (data) {
                        me.tasks = _.each(data.obj, function (ele) {
                            ele.hosts = [];
                        });

                        // 更新主机列
                        me.loadHostList();
                    }).catch(function (req) {
                        JqdeBox.message(false, req.responseText);
                    });
                },

                loadHostList: function () {
                    var me = this, tasks = me.tasks;
                    if (!tasks || tasks.length < 1) return;
                    Jcoder.ajax('/admin/task/host/list', 'POST', {
                        groupName: me.$parent.groupName,
                        names: _.pluck(tasks, 'name'),
                        taskType: me.type
                    }).then(function (data) {
                        data = data.obj || {};
                        _.each(tasks, function (ele) {
                            ele.hosts = data[ele.name];
                        });
                    }).catch(function (req) {
                        JqdeBox.message(false, req.responseText);
                    });
                },

                edit: function (name) {
                    this.$parent.add(name);
                },

                remove: function (name, host) {
                    var me = this, msg = "确定删除" + (host ? "主机 " + host + " " : "所有主机") + "任务 " + name + " ？";
                    JqdeBox.confirm(msg, function (confirm) {
                        if (!confirm) return;

                        JqdeBox.loading();
                        Jcoder.ajax('/admin/task/delete', 'POST', {
                            host: host,
                            groupName: me.$parent.groupName,
                            name: name,
                            type: me.type
                        }).then(function () {
                            JqdeBox.unloading();
                            me.loadTasks();

                            //
                            var parent = me.$parent;
                            me.type != parent.recycleType && parent.$children[2].loadTasks();
                        }).catch(function (req) {
                            JqdeBox.unloading();
                            JqdeBox.message(false, req.responseText);
                        });
                    });
                }
            }
        }
    },

    data: {
        apiType: 1,
        cronType: 2,
        recycleType: 0,
        groupName: param.name
    },

    mounted: function () {
    },

    methods: {
        add: function (name) {
            var h = '/task/edit.html?group=' + this.groupName;
            if (name) {
                h += '&name=' + name;
            }
            location.hash = h;
        }
    }
});
