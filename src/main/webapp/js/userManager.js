var userManager = new Vue({
  el: '#userManager',
  data: {
    users: []
  },
  mounted:function(){
	  this.userList();
  },
  methods:{
	  userList:function(){
		  var $this = this;
		  $.ajax({
			  'url' : '/admin/user/list',
			  'dataType' : 'json',
			  'type' : 'POST',
			  'success' : function(data) {
				if(data.ok){
					$this.users = data.obj.users;
					return false;
				}else{
					
				}
			  }
	      });
	  },
	  add:function(userInfo){
		  var $this = this;
		  var vUrl = '/admin/user/add';
		  $this.item = {};
		  var vT = '添加用户';
		  if(userInfo != undefined){
			  vT = '编辑用户';
			  $this.item = userInfo;
			  vUrl = '/admin/user/modify';
		  }
		  JqdeBox.dialog({
              title: vT,
              url: '/userAddOrEdit.html',
              init: function () {
            	  userAddOrEdit.item = userInfo;
            	  if(userAddOrEdit.item == undefined)userAddOrEdit.item = {};
            	  $('#userAddOrEdit').css('display','block');
	  	          $("#userType option").each(function (){
	  	        	  if(userAddOrEdit.item.type == $(this).attr('value'))$(this).attr("selected", "selected");
	  	          });  
              },
              confirm: function () { 
            	  userAddOrEdit.item.type = $("#userType").val();
            	  var param = userAddOrEdit.item;
            	  $.ajax({
    	  			  'url' : vUrl,
    	  			  'dataType' : 'json',
    	  			  'type' : 'POST',
    	  			  'data':param,
    	  			  'success' : function(data) {
    	  				if(data.ok){
    	  					$this.userList();
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
			  'url' : '/admin/user/del',
			  'dataType' : 'json',
			  'type' : 'POST',
			  'data':item,
			  'success' : function(data) {
				if(data.ok){
					$this.userList();
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
