var jarManager = new Vue({
  el: '#jarManager',
  data: {
    mavenJars:[],
    userJars:[],
    sysJars:[],
    groupName:param.name
  },
  mounted:function(){
	  this.jarList();
  },
  methods:{
	  jarList:function(){
		  var $this = this;
		  Jcoder.ajax('/admin/jar/list', 'post',null,null).then(function (data) {
              JqdeBox.unloading();
              if(data.ok){
            	  $this.mavenJars = data.obj.Maven;
            	  $this.userJars = data.obj.File;
            	  $this.sysJars = data.obj.System;
			  }
          });
	  },
	  gotoMavenFileEdit:function(){
	    location.hash = 'jar/mavenFileEdit.html?name='+this.groupName;
	  },
	  UploadJar:function(){
		  var $this = this;
		  JqdeBox.dialog({
              title: 'UploadJar',
              url: 'modules/jar/importJar.html',
              init:function(){
                /*$('#importDlg').form({
                  url: "/admin/jar/uploadJar?group_name="+$this.groupName,
                  onSubmit: function(){
                      return true;
                  },
                  success:function(data){
                      JqdeBox.message(data.ok, data.message);
                  }
                });*/
              },
              confirm: function () {
              	JqdeBox.loading();
                var formData = new FormData();
                formData.append('file', $('#id-input-file-3').prop("files"));
              　$.ajax({
                  url:"/admin/jar/uploadJar?group_name="+$this.groupName,
                  type:"post",
                  data:formData,
                  processData:false,
                  contentType:false,
                  cache: false,
                  success:function(data){
                     $this.groupList();
                     JqdeBox.message(data.ok, data.message);
                  }
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
