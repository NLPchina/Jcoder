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
                this.loadTasks();
            },

            methods: {
                loadTasks: function () {
                    var me = this, parent = me.$parent,
                        hostGroup = _.findWhere(parent.hosts, {selected: true}) || {current: true};
                    Jcoder.ajax('/admin/task/list', 'GET', {
                        host: hostGroup.current ? null : hostGroup.host,
                        groupName: parent.groupName,
                        taskType: me.type
                    }).then(function (data) {
                        me.tasks = data.obj;
                    }).catch(function (req) {
                        JqdeBox.message(false, req.responseText);
                    });
                },

                edit: function (name) {
                    this.$parent.add(name);
                },

                remove: function (name) {
                    var me = this, parent = me.$parent,
                        hosts = _.chain(parent.hosts).where({selected: true}).pluck("host").value();
                    JqdeBox.confirm("确定删除主机 " + hosts + " 任务 " + name + " ？", function (confirm) {
                        if (!confirm) return;

                        JqdeBox.loading();
                        Jcoder.ajax('/admin/task/delete', 'POST', {
                            hosts: hosts,
                            groupName: parent.groupName,
                            name: name,
                            type: me.type
                        }).then(function () {
                            JqdeBox.unloading();
                            me.loadTasks();

                            //
                            me.type != parent.recycleType && parent.$children[3].loadTasks();
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
        groupName: param.name,
        hosts: []
    },

    mounted: function () {
    },

    methods: {
        add: function (name) {
            var h = '/task/edit.html?group=' + this.groupName;
            if (name) {
                // 如果是编辑
                h += '&name=' + name;
                var hostGroup = _.findWhere(this.hosts, {selected: true});
                if (!hostGroup.current) {
                    h += "&host=" + hostGroup.host;
                }
            }
            location.hash = h;
        },

        change: function () {
            _.each(this.$children.slice(1), function (child) {
                child.loadTasks();
            });
        }
    }
});
