
var threadManager = new Vue({
  el: '#threadManager',
  data: {
    threads: [],
    schedulers:[],
    actions:[],
    groupName:param.name
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
                    var threadsTable = $('#threadsTable').DataTable({
                        /*"colReorder": true,*/
                        /*"destroy":true, //Cannot reinitialise DataTable,解决重新加载表格内容问题
                        "bDestroy":true,*/
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

                    var schedulersTable = $('#schedulersTable').DataTable({
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

                    var actionsTable = $('#actionsTable').DataTable({
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
	    var name = '';
	    if(type == 'scheduler'){
	        name = item.name.split('@')[1];
	    }else if(type == 'thread'){
            name = item.name.split('@')[0];
	    }
        location.hash = "/task/edit.html?group="+this.groupName+"&host=master&name="+name;
	  }
  }
});