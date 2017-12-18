var iocManager = new Vue({
  el: '#iocManager',
  data: {
    item:{},
    checkedHosts:[],
    hosts:[]
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
		  Jcoder.ajax('/admin/group/hostList', 'post',null,null).then(function (data) {
				JqdeBox.unloading();
				if(data.ok){
				  $this.hosts = data.obj;
				  return false;
			    }
		  });
	  }
  }
});