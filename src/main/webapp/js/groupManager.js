var groupManager = new Vue({
  el: '#groupManager',
  data: {
    groups: [],
    item:{}
  },
  mounted:function(){
	  this.groupList();
  },
  methods:{
	  groupList:function(){
		  var $this = this;
		  $.ajax({
			  'url' : '/admin/group/list',
			  'dataType' : 'json',
			  'type' : 'POST',
			  'success' : function(data) {
				if(data.ok){
					$this.groups = data.obj.groups;
					return false;
				}
			  }
	      });
	  },
	  add:function(groupInfo){
		  var $this = this;
		  var vUrl = '/admin/group/add';
		  var vT = 'GroupAdd';
		  $this.item = {};
		  if(groupInfo != undefined){
			  vT = 'GroupEdit';
			  $this.item = groupInfo;
			  vUrl = '/admin/group/modify';
		  }
		  JqdeBox.dialog({
              title: vT,
              url: '/groupAddOrEdit.html',
              init: function () {
            	  groupAddOrEdit.item = groupInfo;
            	  if(groupAddOrEdit.item == undefined)groupAddOrEdit.item = {};
              },
              confirm: function () { 
            	  var param = groupAddOrEdit.item;
            	  $.ajax({
    	  			  'url' : vUrl,
    	  			  'dataType' : 'json',
    	  			  'type' : 'POST',
    	  			  'data':groupAddOrEdit.item,
    	  			  'success' : function(data) {
    	  				if(data.ok){
    	  					$this.groupList();
    	  					JqdeBox.message(true, '添加成功！');
    	  				}else{
    	  					JqdeBox.message(false, data.message);
    	  				}
    	  			  }
    	  	      });
              }
          });
	  },
	  del:function(item){
		  var $this = this;
		  $.ajax({
			  'url' : '/admin/group/del',
			  'dataType' : 'json',
			  'type' : 'POST',
			  'data':item,
			  'success' : function(data) {
				if(data.ok){
					$this.groupList();
					message('success',data.message);
				}else{
					message('false',data.message);
  					return false;
				}
			  }
	      });
	  }
  }
});
