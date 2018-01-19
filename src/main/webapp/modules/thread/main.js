function fetchThreadData(page){
	$('#currentPage').val(page);
	threadManager.ids=[];
	threadManager.threadList();
};
/*function fetchSchedulerData(page){
	$('#currentPage').val(page);
	userModule.ids=[];
	userModule.fetchData();
};*/
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
	  htmlPage("threadPageInfo",1 ,0,'fetchThreadData', 10);
	  htmlPage("schedulersPageInfo",1 ,0,'fetchSchedulerData', 10);
	  htmlPage("actionsPageInfo",1 ,0,'fetchSchedulerData', 10);
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
                console.log(data.obj.actions);
				return false;
			  }
          });
	  },
	  gotoTaskInfo:function(item){

	  }
  }
});
