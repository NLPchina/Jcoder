vmApp.module = new Vue({
    el: '#vmTaskModule',

    components: {
        'task-component': {
            props: ['tasks'],
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
            '<td :title="item.describe">' +
            '<span style="width:380px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;display: block;" v-text="item.describe"></span>' +
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
            '        <button class="btn btn-xs btn-info" title="编辑">' +
            '            <i class="ace-icon fa fa-pencil bigger-120"></i>' +
            '        </button>' +
            '        <button class="btn btn-xs btn-danger" title="删除">' +
            '            <i class="ace-icon fa fa-trash-o bigger-120"></i>' +
            '        </button>' +
            '    </div>' +
            '</td>' +
            '</tr>' +
            '</tbody></table>'
        }
    },

    data: {
        apiTasks: [],
        cronTasks: [],
        recycleTasks: []
    },

    mounted: function () {
        $("#tabs").tabs();

        var me = this;

        // 加载API
        me.loadTasks(1).then(function (data) {
            me.apiTasks = data.obj;
        });

        // 加载CRON
        me.loadTasks(2).then(function (data) {
            me.cronTasks = data.obj;
        });

        // 加载RECYCLE
        me.loadTasks(0).then(function (data) {
            me.recycleTasks = data.obj;
        });
    },

    methods: {

        loadTasks: function (taskType, cb) {
            return Jcoder.ajax('/admin/task/list', 'GET', {
                groupName: Tools.parseQuery(location.hash).name,
                taskType: taskType
            }).catch(function (req) {
                JqdeBox.message(false, req.responseText);
            });
        },

        add: function () {
            location.hash = '/task/edit.html'
        }
    }
});
