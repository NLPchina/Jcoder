var threadManager = new Vue({
  el: '#threadManager',
  data: {
    threads: [],
    schedulers:[],
    actions:[],
    groupName:param.name,
    threadsTable:null,
    schedulersTable:null,
    actionsTable:null
  },
  mounted:function(){
	  this.threadList();
  },
  methods:{
	  threadList:function(){
		  var $this = this;
		  Jcoder.ajax('/admin/thread/list', 'post',{groupName:$this.groupName},null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
				$this.threads = data.obj.threads;
                $this.schedulers = data.obj.schedulers;
                $this.actions = data.obj.actions;
                Vue.nextTick(function(){
                    threadsTable = $('#threadsTable').DataTable({
                        /*"colReorder": true,*/
                        "destroy":true, //Cannot reinitialise DataTable,解决重新加载表格内容问题
                        "bDestroy":true,
                        "bProcessing" : true,
                        "bAutoWidth" : false, //是否自适应宽度
                        "bScrollCollapse" : false, //是否开启DataTables的高度自适应，当数据条数不够分页数据条数的时候，插件高度是否随数据条数而改变
                        "bPaginate" : true, //是否显示（应用）分页器
                        "bInfo" : true, //是否显示页脚信息，DataTables插件左下角显示记录数
                        "sPaginationType" : "full_numbers", //详细分页组，可以支持直接跳转到某页
                        "bSort" : false, //是否启动各个字段的排序功能
                        "bFilter" : true, //是否启动过滤、搜索功能
                        /*colReorder: {
                            fixedColumnsLeft: 1,
                            fixedColumnsRight: 1
                        }*/
                     });

                    schedulersTable = $('#schedulersTable').DataTable({
                         "destroy":true, //Cannot reinitialise DataTable,解决重新加载表格内容问题
                         "bDestroy":true,
                         "bProcessing" : true,
                         "bAutoWidth" : false, //是否自适应宽度
                         "bScrollCollapse" : false, //是否开启DataTables的高度自适应，当数据条数不够分页数据条数的时候，插件高度是否随数据条数而改变
                         "bPaginate" : true, //是否显示（应用）分页器
                         "bInfo" : true, //是否显示页脚信息，DataTables插件左下角显示记录数
                         "sPaginationType" : "full_numbers", //详细分页组，可以支持直接跳转到某页
                         "bSort" : false, //是否启动各个字段的排序功能
                         "bFilter" : true, //是否启动过滤、搜索功能
                    });

                    actionsTable = $('#actionsTable').DataTable({
                       "destroy":true, //Cannot reinitialise DataTable,解决重新加载表格内容问题
                       "bDestroy":true,
                       "bProcessing" : true,
                       "bAutoWidth" : false, //是否自适应宽度
                       "bScrollCollapse" : false, //是否开启DataTables的高度自适应，当数据条数不够分页数据条数的时候，插件高度是否随数据条数而改变
                       "bPaginate" : true, //是否显示（应用）分页器
                       "bInfo" : true, //是否显示页脚信息，DataTables插件左下角显示记录数
                       "sPaginationType" : "full_numbers", //详细分页组，可以支持直接跳转到某页
                       "bSort" : false, //是否启动各个字段的排序功能
                       "bFilter" : true, //是否启动过滤、搜索功能
                    });
                });
				return false;
			  }
          });
	  },
	  gotoTaskInfo:function(item,type){
        location.hash = "/task/edit.html?group="+this.groupName+"&host=master&name="+item.taskName;
	  },
      stopTask:function(item,type){
        debugger;
        var $this = this;
        Jcoder.ajax('/admin/thread/stop', 'post',{hostPort:item.hostPort,key:item.name,first:true},null).then(function (data) {
          JqdeBox.unloading();
          if(data.ok){
            JqdeBox.message(data.ok, "停止任务成功！");
            if(type == 'thread'){
                for(var i = 0;i < $this.threads.length;i++){
                    if($this.threads[i].name == item.name)$this.threads.splice($.inArray($this.threads[i],$this.threads),1);
                }
                var threadsTable =$('#threadsTable').dataTable();
                threadsTable.fnDestroy();
            }else if(type == 'action'){
                for(var i = 0;i < $this.actions.length;i++){
                    if($this.actions[i].name == item.name)$this.actions.splice($.inArray($this.actions[i],$this.actions),1);
                }
                var actionsTable =$('#actionsTable').dataTable();
                actionsTable.fnDestroy();
            }

            //threadsTable.fnClearTable();
            $this.threadList();
            /*var dttable = $('#datatable1').dataTable();
            dttable.fnClearTable(); //清空一下table
            dttable.fnDestroy(); //还原初始化了的datatable*/
            //window.location.reload();
          }
        });
      }
  }
});