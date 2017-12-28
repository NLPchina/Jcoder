var diffMergely = new Vue({
    el: '#diffMergely',
    data: {
        groupName: param.groupName,
        hosts:[],
        lHost:param.leftHostPort,
        rHost:param.rightHostPort,
        lFiles:[],
        rFiles:[],
        lFilePath:param.relativePath,
        rFilePath:param.relativePath ,
        defaultFileInfo:{md5:"",length:0},
        lFileInfo:{},
        rFileInfo:{}
        //hostPort 如果为master则从一台同步主机中获取。


        //获取文件列表 /admin/fileInfo/listFiles {hostPort,groupName}
        //获取文件内容 /admin/fileInfo/fileContent {hostPort, groupName, relativePath, maxSize}
    },
    mounted: function() {

    	this.hostList() ;

		this.listFiles(true) ;
		this.listFiles(false) ;

        $('#compare').mergely({
            cmsettings: {
                mode: 'javascript',
                readOnly: false
            },
            width: 'auto',
            height: 600
        });
    },
    methods: {
    	hostList:function(){
			var $this = this;
			Jcoder.ajax('/admin/group/groupHostList', 'post',{"name":$this.groupName},null).then(function (data) {
				JqdeBox.unloading();
				if(data.ok){
				  $this.hosts = data.obj;
				  return false ;
			  }
			});
		},

		listFiles:function(flag){
			var $this = this ;
			var hostPort = flag?$this.lHost:$this.rHost ;
			Jcoder.ajax('/admin/fileInfo/listFiles', 'post',{"hostPort":hostPort,"groupName":$this.groupName},null).then(function (data) {
				var files = []
				if(!data.ok){
					JqdeBox.message(false, data.message);
				}else{
					files = data.obj ;
				}
				if(flag){
					$this.lFiles = files ;
					if(!files||files.length==0){
						$this.lFilePath = "" ;
					}
				}else{
					$this.rFiles = files ;
					if(!files||files.length==0){
                        $this.rFilePath = "" ;
                    }
				}
				$this.setContent(flag) ;
			});
		},

		setContent:function(flag){

			var $this = this ;
			var hostPort = flag?$this.lHost:$this.rHost ;
			var filePath = flag?$this.lFilePath:$this.rFilePath ;
			if(filePath!=""){
				Jcoder.ajax('/admin/fileInfo/fileContent', 'post',{"hostPort":hostPort,"groupName":$this.groupName,"relativePath":filePath},null).then(function (data) {
                    if(!data.ok){
                        JqdeBox.message(false, data.message);
                    }
                    if(flag){
                        $this.lFileInfo = data.obj ;
                        $('#compare').mergely('lhs', data.message);
                    }else{
                        $this.rFileInfo = data.obj ;
                        $('#compare').mergely('rhs', data.message);
                    }
                });
			}else{
				if(flag){
					$this.lFileInfo = $this.defaultFileInfo ;
                    $('#compare').mergely('lhs', "请选择对比文件");
                }else{
                    $this.rFileInfo = $this.defaultFileInfo ;
                    $('#compare').mergely('rhs', "请选择对比文件");
                }
			}
		}

	}
});