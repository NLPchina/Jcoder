var diffMergely = new Vue({
    el: '#diffMergely',
    data: {
        groupName: param.groupName,
        hosts:[],
        lFiles:[],
        rFiles:[],
        lFilePath:"",
        rFilePath:"" ,
        //hostPort 如果为master则从一台同步主机中获取。


        //获取文件列表 /admin/fileInfo/listFiles {hostPort,groupName}
        //获取文件内容 /admin/fileInfo/fileContent {hostPort, groupName, relativePath, maxSize}
    },
    mounted: function() {

    	this.hostList() ;

		this.listFiles('127.0.0.1:9095',this.groupName,this.lFiles) ;

		this.listFiles('127.0.0.1:9095',this.groupName,this.rFiles) ;

		this.lFilePath = _.chain(this.lFiles).where({relativePath: this.relativePath}).value()[0].relativePath ;
		this.rFilePath = _.chain(this.rFiles).where({relativePath: this.relativePath}).value()[0].relativePath ;


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

		listFiles:function(hostPort,groupName,files){
			var $this = this ;
			Jcoder.ajax('/admin/fileInfo/listFiles', 'post',{"hostPort":hostPort,"groupName":groupName},null).then(function (data) {
				if(data.ok){
					files.splice(0,files.length);
					data.obj.forEach(function(v){
						files.push(v) ;
					}) ;
				}else{
					JqdeBox.message(false, data.message);
				}
			});
		}

	}
});