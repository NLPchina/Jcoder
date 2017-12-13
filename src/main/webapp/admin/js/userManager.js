var $ = layui.jquery;
var userManager = new Vue({
  el: '#userManager',
  data: {
    users: [],
    item:{}
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
		  layer.open({
	        type: 1,
	        title: vT,
	        area: ['600px', userInfo == undefined?'430px':'480px'],
	        shade: 0,
	        maxmin: false,
	        offset: [ //为了演示，随机坐标
	           '50px', '400px'
	        ],
	        content: $('#userAddOrEdit'),
	        btn: ['确定', '取消'], //只是为了演示
	        yes: function(){
	          $this.item.type = $("#userType").val();
	  		  $.ajax({
	  			  'url' : vUrl,
	  			  'dataType' : 'json',
	  			  'type' : 'POST',
	  			  'data':$this.item,
	  			  'success' : function(data) {
	  				if(data.ok){
	  					layer.closeAll();
	  					$this.userList();
	  					message('success',data.message);
	  				}else{
	  					message('false',data.message);
	  					return false;
	  				}
	  			  }
	  	      });
	        },
	        btn2: function(){
	          $('#userAddOrEdit').css('display','none');
	          $this.item = {};
	          layer.closeAll();
	        },
	        success: function(layero){
	        	$('#userAddOrEdit').css('display','block');
	        	//if($this.item != undefined && $this.item != null)$("#userId").css('display','block');
	        	$("#userType option").each(function (){
	        		if($this.item.type == $(this).attr('value'))$(this).attr("selected", "selected");
	             });  
	        	layui.form.render('select');
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
					layer.closeAll();
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

function message(type, message) {
	layer.open({
	    type: 1,
	    offset: 'rt',
	    content: '<div style="padding: 20px 100px;">'+ message +'</div>',
	    shade: 0, //不显示遮罩
	    time: 1000,
	    anim: 1
    });
}
