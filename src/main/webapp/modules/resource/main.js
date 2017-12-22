var resourceManager = new Vue({
  el: '#resourceManager',
  data: {
    resources:{},
    checkedHosts:[],
    hosts:[],
    groupName:param.name
  },
  mounted:function(){
	  var $this = this;
	  $this.hostList();
	  $this.resourceList();
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
	  resourceList:function(){
          var $this = this;
          Jcoder.ajax('/admin/resource/list', 'post',{groupName:$this.groupName},null).then(function (data) {
            JqdeBox.unloading();
            debugger;
            if(data.ok){
                $this.resources = data.obj;
            }else{
                JqdeBox.message(false, data.msg);
            }
          });
      }
  }
});