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
                importJar.groupName = $this.groupName;
              },
              confirm: function () {
                debugger;
              	JqdeBox.loading();
                var formData = new FormData();
                var files = $('#id-input-file-3').prop("files");
                /*var files = [];
                */
                /*for(var k in product_img_files){ //文件数组
                     formData.append('product[]',product_img_files[k]);
                }*/
                for(var i = 0;i < files.length;i++){
                    formData.append('file', files[i]);
                }
              　$.ajax({
                  url:"/admin/jar/uploadJar?group_name="+$this.groupName+"&hostPorts="+importJar.checkedHosts,
                  type:"post",
                  data:formData,
                  processData:false,
                  contentType:false,
                  cache: false,
                  success:function(data){
                     JqdeBox.unloading();
                     console.log(data);
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
