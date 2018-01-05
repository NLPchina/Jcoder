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
                        host: !hostGroup.host || hostGroup.host == "master" ? null : hostGroup.host,
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
                    var me = this, parent = me.$parent;
                    JqdeBox.dialog({
                        title: '确认删除任务 ' + name,
                        url: 'modules/task/remove_confirm.html',
                        init: function () {
                            (this.data = this.data || {}).vmTaskRemoveConfirm = new Vue({
                                el: '#vmTaskRemoveConfirmModule',
                                data: {
                                    hosts: _.map(parent.hosts, function (ele) {
                                        return {selected: ele.selected, current: ele.current, host: ele.host};
                                    }),
                                    all: false
                                },
                                mounted: function () {
                                    var $scrollable = $(this.$el).find('.scrollable');
                                    $scrollable.ace_scroll({size: $scrollable.attr('data-size')});
                                },
                                methods: {
                                    selectAll: function () {
                                        var all = this.all;
                                        _.each(this.hosts, function (ele) {
                                            ele.selected = all;
                                        });
                                    }
                                }
                            });
                        },
                        confirm: function () {
                            var hosts = _.chain(this.data.vmTaskRemoveConfirm.hosts).where({selected: true}).pluck("host").value();
                            if (!hosts || hosts.length < 1) {
                                JqdeBox.alert("请选择主机 ！");
                                return false;
                            }
                            if (hosts.length == 1 && hosts[0].trim().toLowerCase() == 'master') {
                                JqdeBox.alert("请至少选择一个非主版本的主机 ！");
                                return false;
                            }

                            JqdeBox.loading();
                            Jcoder.ajax('/admin/task/delete', 'POST', {
                                hosts: hosts,
                                groupName: parent.groupName,
                                name: name,
                                type: me.type
                            }).then(function () {
                                JqdeBox.unloading();
                                JqdeBox.hideAll();

                                // 刷新主机面板
                                parent.$children[0].diff(hosts);

                                // 刷新任务列表
                                me.loadTasks();
                                me.type != parent.recycleType && parent.$children[3].loadTasks();
                            }).catch(function (req) {
                                JqdeBox.unloading();
                                JqdeBox.message(false, req.responseText);
                            });

                            return false;
                        }
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
