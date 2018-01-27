var groupManager = new Vue({
  el: '#groupManager',
  data: {
    groups: []
  },
  mounted:function(){
	  this.list();
  },
  methods:{
	  list:function(){
		  var $this = this;
		  Jcoder.ajax('/admin/groupGit/list', 'post',null,null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
            	  $this.groups = data.obj;
					return false;
			  }
          });
	  }
  }
});
