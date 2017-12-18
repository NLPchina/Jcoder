var iocManager = new Vue({
  el: '#iocManager',
  data: {
    item:{},
    checkedHosts:[],
    hosts:[],
    groupName:param.name
  },
  mounted:function(){
	  var $this = this;
	  $this.editor = CodeMirror.fromTextArea(document.getElementById('etlWorkerDlg-add-code'), {
      	lineNumbers : true,
			mode : "python",
			matchBrackets : true,
			theme : "monokai",
			showCursorWhenSelecting : true    			
      });
	  $this.hostList();
  },
  methods:{
	  hostList:function(){
		  var $this = this;
		  Jcoder.ajax('/admin/ioc/hostList', 'post',{groupName:$this.groupName},null).then(function (data) {
				JqdeBox.unloading();
				if(data.ok){
				  $this.hosts = data.obj;
				  JqdeBox.message(true, msg);
			    }else{
			    	JqdeBox.message(false, data.msg);
			    }
		  });
	  },
	  save:function(){
		  
	  }
  }
});