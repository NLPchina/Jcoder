var jarManager = new Vue({
  el: '#jarManager',
  data: {
    mavenJars:[],
    userJars:[],
    sysJars:[],
    groupName:param.name
  },
  mounted:function(){
	  this.jarList();
  },
  methods:{
	  jarList:function(){
		  var $this = this;
		  Jcoder.ajax('/admin/jar/list', 'post',null,null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
            	  $this.mavenJars = data.obj.Maven;
            	  $this.userJars = data.obj.File;
            	  $this.sysJars = data.obj.System;
			  }
          });
	  },
	  gotoMavenFileEdit:function(){
	    location.hash = 'jar/mavenFileEdit.html?name='+this.groupName;
	  },
	  add:function(){
		  var $this = this;
		  var vUrl = '/admin/group/add';
		  var vT = 'GroupAdd';

		  JqdeBox.dialog({
              title: vT,
              url: 'modules/group/groupAddOrEdit.html',
              confirm: function () {
              	  JqdeBox.loading();
            	  var param = groupAddOrEdit.item;
            	  groupAddOrEdit.hostArray();
            	  Jcoder.ajax(vUrl, 'post',{"hostPorts":groupAddOrEdit.hostPorts.toString(),"name":groupAddOrEdit.item.name},null).then(function (data) {
                      JqdeBox.unloading();
                      if(data.ok){
                    	  $this.groupList();
  	  					  JqdeBox.message(true, data.message);
  	  				  }else{
  	  					JqdeBox.message(false, data.message);
  	  				  }
                  });
              }
          });
	  },
	  deleteByCluster: function(groupName){
	  	var $this = this;
	  	JqdeBox.confirm("确定删除组："+groupName,function(status){
			if(status){
				JqdeBox.loading();
				Jcoder.ajax("/admin/group/deleteByCluster", 'post',{"name":groupName},null).then(function (data) {
				  JqdeBox.unloading();
				  if(data.ok){
					  $this.groupList();
					  JqdeBox.message(true, data.message);
				  }else{
					JqdeBox.message(false, data.message);
				  }
			  });
			}
	 	 }) ;
	  }
  }
});
