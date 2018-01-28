var groupGitManager = new Vue({
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
	  },
	  save:function(index){
          var $this = this;
          var group = $this.groups[index];
          JqdeBox.loading();
		  Jcoder.ajax('/admin/groupGit/save', 'post',group,null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
                JqdeBox.message(true, data.message);
			  }
			  $this.list();
          }).catch(function (req) {
	          JqdeBox.unloading();
	          JqdeBox.message(false, req.responseText);
	      });
      },
      flush:function(index){
	      var $this = this;
	      var group = $this.groups[index];
	      JqdeBox.loading();
	      Jcoder.ajax('/admin/groupGit/flush', 'post',{"groupName":group.groupName},null).then(function (data) {
		          JqdeBox.unloading();
		          if(data.ok){
		          JqdeBox.message(true, data.message);
	         }
             $this.list();
		  }).catch(function (req) {
			    JqdeBox.unloading();
			    JqdeBox.message(false, req.responseText);
		  });
	  }
  }
});
