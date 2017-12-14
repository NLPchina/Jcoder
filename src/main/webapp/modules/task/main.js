vmApp.module = new Vue({
    el: '#vmTaskModule',

    data: {
        tasks: []
    },

    mounted: function () {
        $("#tabs").tabs();

        // 默认加载API
        this.loadTasks(1);
    },

    methods: {

        loadTasks: function (taskType) {
            var me = this;
            Jcoder.ajax('/admin/task/list', 'GET', {
                groupName: Tools.parseQuery(location.hash).name,
                taskType: taskType
            })
                .then(function (data) {
                    me.tasks = data.obj;
                })
                .catch(function (req) {
                    // TODO:
                });
        }
    }
});
