var mavenFileEdit = new Vue({
  el: '#mavenFileEdit',
  data: {
    editor: '',
    checkedHosts:[],
    hosts:[],
    groupName:param.name
  },
  mounted:function(){
	  var $this = this;
      $this.editor = CodeMirror.fromTextArea(document.getElementById('etlWorkerDlg-add-code'), {
           lineNumbers : true,
           mode : "xml",
           matchBrackets : true,
           theme : "monokai",
           showCursorWhenSelecting : true
        });
      $this.hostList();
      $this.findMavenInfoByGroupName();
  },
  methods:{
      hostList:function(){
  		  var $this = this;
  		  Jcoder.ajax('/admin/ioc/hostList', 'post',{groupName:$this.groupName},null).then(function (data) {
  				JqdeBox.unloading();
  				if(data.ok){
  				  for(var key in data.obj){
                      $this.hosts.push(key);
                      if(data.obj[key].current)$this.checkedHosts.push(key);
                    }
  			    }else{
  			    	JqdeBox.message(false, data.msg);
  			    }
  		  });
  	  },
      save:function(){
        var $this = this;
        Jcoder.ajax('/admin/jar/save', 'post',{hostPorts:$this.checkedHosts.toString(),
            groupName:$this.groupName,content:$this.editor.getValue(),first:true},null).then(function (data) {
            JqdeBox.unloading();
            JqdeBox.message(data.ok, data.message);
            setTimeout(function(){
                location.hash = 'jar/list.html?name='+$this.groupName;
            },800)
        });
      },
      findMavenInfoByGroupName:function(){
        var $this = this;
        Jcoder.ajax('/admin/jar/findMavenInfoByGroupName', 'post',{group_name:$this.groupName},null).then(function (data) {
          JqdeBox.unloading();
          $this.editor.setValue(data.obj);
        });
      }
  }
});
