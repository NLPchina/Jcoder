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
		  Jcoder.ajax('/admin/user/list', 'post',null,null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
				$this.users = data.obj.users;
				return false;
			  }
          });
	  },
	  add:function(userInfo){
		  var $this = this;
		  var vUrl = '/admin/user/add';
		  $this.item = {};
		  var vT = '添加用户';
		  var msg = '添加成功！';
		  if(userInfo != undefined){
			  vT = '编辑用户';
			  $this.item = userInfo;
			  vUrl = '/admin/user/modify';
			  msg = '修改成功！';
		  }
		  JqdeBox.dialog({
              title: vT,
              url: 'modules/user/userAddOrEdit.html',
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
            	  Jcoder.ajax(vUrl, 'post',param,null).then(function (data) {
                      JqdeBox.unloading();
                      if(data.ok){
  	  					$this.userList();
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
		  Jcoder.ajax('/admin/user/del', 'post',item,null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
				$this.userList();
				JqdeBox.message(true, '删除成功！');
			  }else{
				JqdeBox.message(false, data.message);
			  }
          });
	  }
  }
});
