var groupManager = new Vue({
  el: '#groupManager',
  data: {
    groups: [],
    hosts: [],
    item:{}
  },
  mounted:function(){
	  this.groupList();
	  this.hostList();

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
		hostList:function(){
			  var $this = this;
			  Jcoder.ajax('/admin/group/hostList', 'post',null,null).then(function (data) {
					JqdeBox.unloading();
					if(data.ok){
					  $this.hosts = data.obj;
						return false;
				  }
				});
		},
	  add:function(groupInfo){
		  var $this = this;
		  var vUrl = '/admin/group/add';
		  var vT = 'GroupAdd';
		  var msg = '添加成功！';
		  $this.item = {};
		  if(groupInfo != undefined){
			  vT = 'GroupEdit';
			  $this.item = groupInfo;
			  vUrl = '/admin/group/modify';
			  msg = '修改成功！';
		  }
		  JqdeBox.dialog({
              title: vT,
              url: 'modules/group/groupAddOrEdit.html',
              init: function () {
            	  groupAddOrEdit.item = groupInfo;
            	  if(groupAddOrEdit.item == undefined)groupAddOrEdit.item = {};
              },
              confirm: function () { 
            	  var param = groupAddOrEdit.item;
            	  Jcoder.ajax(vUrl, 'post',groupAddOrEdit.item,null).then(function (data) {
                      JqdeBox.unloading();
                      if(data.ok){
                    	  $this.groupList();
  	  					JqdeBox.message(true, msg);
  	  				  }else{
  	  					JqdeBox.message(false, data.message);
  	  				  }
                  });
              }
          });
	  },
	  del:function(item){
		  var $this = this;
		  Jcoder.ajax('/admin/group/del', 'post',item,null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
				$this.groupList();
				JqdeBox.message(true, '删除成功！');
			  }else{
				JqdeBox.message(false, data.message);
			  }
          });
	  }
  }
});
