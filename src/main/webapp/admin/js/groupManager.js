var $ = layui.jquery;
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
		  layer.open({
	        type: 1,
	        title: vT,
	        area: ['580px', groupInfo == undefined?'330px':'380px'],
	        shade: 0,
	        maxmin: false,
	        offset: [ //为了演示，随机坐标
	           '50px', '400px'
	        ],
	        content: $('#groupAddOrEdit'),
	        btn: ['确定', '取消'], //只是为了演示
	        yes: function(){
	  		  $.ajax({
	  			  'url' : vUrl,
	  			  'dataType' : 'json',
	  			  'type' : 'POST',
	  			  'data':$this.item,
	  			  'success' : function(data) {
	  				if(data.ok){
	  					layer.closeAll();
	  					$this.groupList();
	  					message('success',data.message);
	  				}else{
	  					message('false',data.message);
	  					return false;
	  				}
	  			  }
	  	      });
	        },
	        btn2: function(){
	          $('#groupAddOrEdit').css('display','none');
	          $this.item = {};
	          layer.closeAll();
	        },
	        success: function(layero){
	        	$('#groupAddOrEdit').css('display','block');
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
