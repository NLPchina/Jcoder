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
        //hostPort 如果为master则从一台同步主机中获取。


        //获取文件列表 /admin/fileInfo/listFiles {hostPort,groupName}
        //获取文件内容 /admin/fileInfo/fileContent {hostPort, groupName, relativePath, maxSize}
    },
    mounted: function() {

    	this.hostList() ;

		this.listFiles(true) ;
		this.listFiles(false) ;

		this.setContent(true) ;
		this.setContent(false) ;

        $('#compare').mergely({
            cmsettings: {
                mode: 'javascript',
                readOnly: false
            },
            width: 'auto',
            height: 600,
            lhs: function(setValue) {
                //设置值
                setValue('the quick red fox\njumped over the hairy dog');
            },
            rhs: function(setValue) {
                //设置值
                setValue('the quick brown fox\njumped over the lazy dog');
            }
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
				if(data.ok){
					if(flag){
						$this.lFiles = data.obj ;
					}else{
						$this.rFiles = data.obj ;
					}
				}else{
					JqdeBox.message(false, data.message);
				}
			});
		},

		setContent:function(flag){
			var $this = this ;
			var hostPort = flag?$this.lHost:$this.rHost ;
			Jcoder.ajax('/admin/fileInfo/fileContent', 'post',{"hostPort":hostPort,"groupName":$this.groupName,"relativePath":$this.lFilePath},null).then(function (data) {
                if(!data.ok){
                    JqdeBox.message(false, data.message);
                }
                if(flag){
                    $('#compare').mergely('lhs', data.message);
                }else{
					$('#compare').mergely('rhs', data.message);
                }
            });
		}

	}
});