var iocManager = new Vue({
  el: '#iocManager',
  data: {
    item:{},
    checkedHosts:[],
    hosts:[],
    groupName:param.name,
    editor: ''
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
				  for(var key in data.obj){
                    $this.hosts.push(key);
                    //console.log(data.obj[key]);
                    if(data.obj[key].current)$this.checkedHosts.push(key);
                  }
			    }else{
			    	JqdeBox.message(false, data.msg);
			    }
		  });
	  },
	  save:function(){
		  var $this = this;
          Jcoder.ajax('/admin/ioc/save', 'post',{hostPorts:$this.checkedHosts,groupName:$this.groupName,code:$this.editor.getValue()},null).then(function (data) {
                JqdeBox.unloading();
                if(data.ok){
                  JqdeBox.message(true, data.msg);
                }else{
                    JqdeBox.message(false, data.msg);
                }
          });
	  }
  }
});