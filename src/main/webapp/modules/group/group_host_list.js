var groupHostList = new Vue({
  el: '#groupHostList',
  data: {
    items: [],
    groupName: "",
    hostPorts:[],
    diffs:[],
    diffHostPort:"",
    currentHostPort:""
  },
  mounted:function(){
  	this.groupName = param.name ;
  	this.hostList();
  	// check all
	var $table = $('#dynamic-table');
	$table.on('click', 'th input[type=checkbox]', function () {
		var th_checked = this.checked;
		$table.find('td input[type=checkbox]').each(function () {
			this.checked = th_checked;
		});
	});

	$table = $('#diff-table');
	$table.on('click', 'th input[type=checkbox]', function () {
		var th_checked = this.checked;
		$table.find('td input[type=checkbox]').each(function () {
			this.checked = th_checked;
		});
	});
  },
  methods:{
	hostList:function(){
		var $this = this;
		Jcoder.ajax('/admin/group/groupHostList', 'post',{"name":$this.groupName},null).then(function (data) {
			JqdeBox.unloading();
			if(data.ok){
			  $this.items = data.obj;
			  return false ;
		  }
		});
	},

	changeWeight:function(hostPort,weight){
		var $this = this;
		JqdeBox.loading();
		Jcoder.ajax('/admin/group/changeWeight', 'post',{"groupName":$this.groupName,"weight":weight,"hostPort":hostPort},null).then(function (data) {
			JqdeBox.unloading();
			JqdeBox.message(data.ok, data.message);
		});
	},

	del: function(hostPort,groupName){
		var $this = this;
		JqdeBox.confirm("确定删除组在："+hostPort,function(status){
			if(status){
				JqdeBox.loading();
				Jcoder.ajax("/admin/group/delete", 'post',{"hostPorts":hostPort,"name":groupName},null).then(function (data) {
					JqdeBox.unloading();
					if(data.ok){
						$this.hostList();
						JqdeBox.message(true, data.message);
					}else{
						JqdeBox.message(false, data.message);
					}
				});
			}
		}) ;
	},

	hostArray: function () {
		var $this = this;
		var len = $(":checkbox").length ;
		for(var i =1 ;i<len ;i++){
		 if($(":checkbox")[i].checked==true){
			$this.hostPorts.push($this.items[i-1].hostPort);
		 }
		}
	},

	delAll: function(){
		var $this = this;
		$this.hostArray();
		JqdeBox.confirm("确定删除组："+$this.hostPorts.toString(),function(status){
			if(status){
				JqdeBox.loading();
				Jcoder.ajax("/admin/group/delete", 'post',{"hostPorts":$this.hostPorts.toString(),"name":$this.groupName},null).then(function (data) {
					JqdeBox.unloading();
					if(data.ok){
						$this.hostList();
						JqdeBox.message(true, data.message);
					}else{
						JqdeBox.message(false, data.message);
					}
				});
			}
		}) ;
	},

	share: function(hostPort){
		var $this = this;
		JqdeBox.dialog({
              title: "克隆",
              url: 'modules/group/groupShare.html',
              confirm: function () {
              	  JqdeBox.loading();
            	  var param = groupShare.item;
            	  groupShare.hostArray();
            	  Jcoder.ajax("/admin/group/share", 'post',{"hostPorts":groupShare.toHosts.toString(),"groupName":$this.groupName,"toGroupName":groupShare.toGroupName,"formHostPort":hostPort},null).then(function (data) {
                      JqdeBox.unloading();
                      if(data.ok){
                    	  $this.hostList();
  	  					  JqdeBox.message(true, data.message);
  	  				  }else{
  	  					JqdeBox.message(false, data.message);
  	  				  }
                  });
              }
          });
	},

	flush: function(hostPort,upMapping){
		var $this = this;
		JqdeBox.loading();
		Jcoder.ajax("/admin/group/flush", 'post',{"hostPort":hostPort,"groupName":$this.groupName,"upMapping":upMapping},null).then(function (data) {
			JqdeBox.unloading();
			if(data.ok){
				$this.diffs = data.obj ;
				$this.hostList();
				if($this.diffs.length==0){
					JqdeBox.message(data.ok, hostPort+" 已与主版本一致");
				}
				$this.diffHostPort = hostPort ;
				$this.items.forEach(function(v){
					if(v.current){
						$this.currentHostPort = v;
					}
				}) ;
			}else{
				JqdeBox.message(false, data.message);
			}
		}).catch(function(req){
			JqdeBox.unloading();
			JqdeBox.message(false,req.responseText) ;
		});
	},


	fixDiff: function(fromHostPort ,toHostPort, relativePath, isCheckout){
		var $this = this;
		JqdeBox.loading();
		if(isCheckout){ //如果是checkout ，则从checkbox中取得
			var list = [] ;
			$('#diff-table').find('td input[type=checkbox]').each(function(i,v) {
				if(v.checked){
					list.push($this.diffs[i].path)
				}
			});

			if(list.length==0){
				JqdeBox.message(false, "至少选择一项");
				JqdeBox.unloading();
				return ;
			}

			relativePath = list;
		}

		Jcoder.ajax("/admin/group/fixDiff", 'post',{"fromHostPort":fromHostPort,"toHostPort":toHostPort,"groupName":$this.groupName,"relativePath[]":relativePath},null).then(function (data) {
			JqdeBox.unloading();
			JqdeBox.message(data.ok, data.message);
			if(fromHostPort!='master'){
				$this.flush(fromHostPort,true);
			}
			if(toHostPort!='master'){
				$this.flush(toHostPort,true);
			}
		}).catch(function(req){
			JqdeBox.unloading();
			JqdeBox.message(false,req.responseText) ;
		});
	},
  }

});