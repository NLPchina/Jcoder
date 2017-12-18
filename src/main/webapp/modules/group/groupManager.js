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
	  }
  }
});
