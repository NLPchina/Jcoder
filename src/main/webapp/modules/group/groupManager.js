var groupManager = new Vue({
  el: '#groupManager',
  data: {
    groups: []
  },
  mounted:function(){
	  this.groupList();
  },
  methods:{
	  groupList:function(){
		  var $this = this;
		  Jcoder.ajax('/admin/group/list', 'post',null,null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
            	  $this.groups = data.obj;
					return false;
			  }
          });
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
            	  groupAddOrEdit.getHostArray();
            	  Jcoder.ajax(vUrl, 'post',{"hostPorts":groupAddOrEdit.hostPorts.toString(),"name":groupAddOrEdit.item.name},null).then(function (data) {
                      JqdeBox.unloading();
                      if(data.ok){
                    	  $this.groupList();
  	  					  JqdeBox.message(true, data.message);
  	  				  }else{
  	  					JqdeBox.message(false, data.message);
  	  				  }
                  }).catch(function (req) {
                      JqdeBox.unloading();
	                  JqdeBox.message(false, eval("("+req.responseText+")").message);
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
