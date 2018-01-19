var diffMergely = new Vue({
    el: '#diffMergely',
    data: {
        groupName: param.groupName,
        hosts:[],
        lHost:param.leftHostPort,
        rHost:param.rightHostPort,
        lFiles:[],
        rFiles:[],
        lTasks:[],
        rTasks:[],
        lFilePath:param.relativePath,
        rFilePath:param.relativePath ,
        defaultFileInfo:{md5:"",length:0},
        defaultTask:{md5:"",code:""},
        lFileInfo:{},
        rFileInfo:{},
        lTask:{md5:"",code:""},
        rTask:{md5:"",code:""}
        //hostPort 如果为master则从一台同步主机中获取。


        //获取文件列表 /admin/fileInfo/listFiles {hostPort,groupName}
        //获取文件内容 /admin/fileInfo/fileContent {hostPort, groupName, relativePath, maxSize}
    },
    mounted: function() {
    	this.hostList() ;

		this.listFiles(true) ;
		this.listFiles(false) ;

		this.listTasks(true) ;
		this.listTasks(false) ;

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
					//TODO:去掉了没发现大问题先这么招 $this.lFilePath = "" ;
					}
				}else{
					$this.rFiles = files ;
					if(!files||files.length==0){
                     //TODO:去掉了没发现大问题先这么招   $this.rFilePath = "" ;
                    }
				}
				$this.setContent(flag) ;
			});
		},

		listTasks:function(flag){
			var $this = this ;
			var hostPort = flag?$this.lHost:$this.rHost ;
			Jcoder.ajax('/admin/task/list', 'post',{"host":hostPort,"groupName":$this.groupName,"taskType":-1},null).then(function (data) {
				var tasks = []
				if(!data.ok){
					JqdeBox.message(false, data.message);
				}else{
					tasks = data.obj ;
				}
				if(flag){
					$this.lTasks = tasks ;
				}else{
					$this.rTasks = tasks ;
				}
				$this.setContent(flag) ;
			});
		},

		setContent:function(flag){

			var $this = this ;
			var hostPort = flag?$this.lHost:$this.rHost ;
			var filePath = flag?$this.lFilePath:$this.rFilePath ;
			if(filePath.startsWith("/")){
				Jcoder.ajax('/admin/fileInfo/fileContent', 'post',{"hostPort":hostPort,"groupName":$this.groupName,"relativePath":filePath},null).then(function (data) {
                    if(!data.ok){
                        JqdeBox.message(false, data.message);
                    }
                    if(flag){
                    	$this.lTask = $this.defaultTask ;
                        $this.lFileInfo = data.obj ;
                        $('#compare').mergely('lhs', data.message);
                    }else{
                    	$this.rTask = $this.defaultTask ;
                        $this.rFileInfo = data.obj ;
                        $('#compare').mergely('rhs', data.message);
                    }
                });
			}else if(filePath!=""){
				Jcoder.ajax('/admin/task/task', 'post',{"sourceHost":hostPort,"groupName":$this.groupName,"name":filePath},null).then(function (data) {
					if(!data.ok){
						JqdeBox.message(false, data.message);
					}
					if(flag){
						$this.lFileInfo = $this.defaultFileInfo ;
						$this.lTask = data.obj ;
						$('#compare').mergely('lhs', data.obj.code);
					}else{
						$this.rFileInfo = $this.defaultFileInfo ;
						$this.rTask = data.obj ;
						$('#compare').mergely('rhs', data.obj.code);
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