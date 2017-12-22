vmApp.module = new Vue({
    el: '#vmTaskModule',

    components: {
        'task-component': {
            props: ['type'],

            data: function () {
                return {tasks: null};
            },

            template: '<table class="table table-striped table-bordered table-hover dataTable">' +
            '<thead>' +
            '<tr>' +
            '<th width="20%">名称</th>' +
            '<th>描述</th>' +
            '<th width="6.8%">状态</th>' +
            '<th width="14%">创建时间</th>' +
            '<th width="14%">更新时间</th>' +
            '<th width="7%">操作</th>' +
            '</tr>' +
            '</thead>' +
            '<tbody>' +
            '<tr v-for="(item, index) in tasks">' +
            '<td>{{item.name}}</td>' +
            '<td :title="item.description">' +
            '<span style="width:380px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;display: block;" v-text="item.description"></span>' +
            '</td>' +
            '<td>' +
            '    <span v-if="item.status==1" class="label label-success label-white middle">' +
            '        <i class="ace-icon glyphicon glyphicon-ok bigger-120"></i>' +
            '        激活' +
            '    </span>' +
            '    <span v-else class="label label-warning label-white middle">' +
            '        <i class="ace-icon fa fa-exclamation-triangle bigger-120"></i>' +
            '        停用' +
            '    </span>' +
            '</td>' +
            '<td>{{item.createTime}}</td>' +
            '<td>{{item.updateTime}}</td>' +
            '<td>' +
            '    <div class="hidden-sm hidden-xs btn-group">' +
            '        <button class="btn btn-xs btn-info" type="button" title="编辑" @click="edit(item.name)">' +
            '            <i class="ace-icon fa fa-pencil bigger-120"></i>' +
            '        </button>' +
            '        <button class="btn btn-xs btn-danger" type="button" title="删除" @click="remove(item.name)">' +
            '            <i class="ace-icon fa fa-trash-o bigger-120"></i>' +
            '        </button>' +
            '    </div>' +
            '</td>' +
            '</tr>' +
            '</tbody></table>',

            mounted: function () {
                this.loadTasks();
            },

            methods: {
                loadTasks: function () {
                    var me = this;
                    return Jcoder.ajax('/admin/task/list', 'GET', {
                        groupName: me.$parent.groupName,
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
                    var me = this;
                    JqdeBox.confirm("确定删除任务 " + name + " ？", function (confirm) {
                        if (!confirm) return;

                        // hosts的处理
                        var hosts = _.chain(me.hosts).where({checked: true}).pluck('host').value();

                        JqdeBox.loading();
                        Jcoder.ajax('/admin/task/delete', 'POST', {
                            hosts: hosts,
                            groupName: me.$parent.groupName,
                            name: name,
                            type: me.type
                        }).then(function () {
                            JqdeBox.unloading();
                            me.loadTasks();
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
        $("#tabs").tabs();
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
