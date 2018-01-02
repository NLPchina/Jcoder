var zNodes = null;//全局ztree数据
var flag = false;
//zTree初始化数据
var setting = {
	view: {
		selectedMulti: false,//是否可以多选
		showIcon: true,//是否显示图片
		fontCss:{"font-family": "微软雅黑","font-size": "14px"}
	},
	edit: {
		drag:{
			isCopy:false,//是否可以复制
			isMove:false,//移动
			prev:false,//是否允许移动到目标节点前面
			inner:false,//是否允许成为目标节点的子节点
			next:false //是否允许移动到目标节点后面
		},
		enable: true,//设置 zTree 是否处于编辑状态
		showRemoveBtn: false,//展示删除按钮
		showRenameBtn: false//展示编辑按钮
	},
	data: {
		simpleData: {
			enable: true//是否采用简单数据模式 (Array)
		}
	},
	callback: {
		beforeClick:beforeNodeClick,//单击事件前
		onClick:treeNodeClick,//单机事件
	}
};



function beforeNodeClick(){
    resourceManager.resources = [];
}

//节点单机事件
function treeNodeClick(treeId, treeNodes) {
debugger;
	var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
	nodes = zTree.getSelectedNodes(),//获取被选中的节点
	treeNode = nodes[0];
	resourceManager.currentNode = treeNode;
	if(treeNode.file != null){
	    resourceManager.isFile = treeNode.file.directory?false:true;
	    if(treeNode.file.directory){
	        $("#folderContent").css("display","block");
	        $("#fileContent").css("display","none");
	    }else{
	        $("#folderContent").css("display","none");
            $("#fileContent").css("display","block");
	    }
	}
	if(treeNode.id == '0')resourceManager.isFile = false;
	for(var i = 0;i<zNodes.length;i++){
        if(zNodes[i].pId == treeNode.id){
            resourceManager.resources.push(zNodes[i]);
        }
    }
    if(!treeNode.file.directory){
        if(resourceManager.editor != null && resourceManager.editor.getValue() != null){
            resourceManager.editor.setValue(treeNode.file.md5);
            return false;
        }
    }
}
//初始化目录信息
function initResourceTree(resourceFiles){
	if(resourceFiles != null && resourceFiles != '' && resourceFiles != '[]'){
		$.fn.zTree.init($("#treeDemo"), setting, resourceFiles);
		zNodes = resourceFiles;
	}
	var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
	nodes = zTree.getNodes(),
	treeNode = nodes[0];
	if(treeNode.id == 0){
	    resourceManager.isFile = false;
	    $("#folderContent").css("display","block");
	    $("#fileContent").css("display","none");
	}
	zTree.selectNode(treeNode,false,false);
	resourceManager.currentNode = treeNode;
	for(var i = 0;i<resourceFiles.length;i++){
	    if(resourceFiles[i].pId == "0")resourceManager.resources.push(resourceFiles[i]);
	}
}

var resourceManager = new Vue({
  el: '#resourceManager',
  data: {
    resources:[],
    //checkedHosts:[],
    hosts:[],
    groupName:param.name,
    editor: '',
    isFile:false,
    currentNode:null
  },
  mounted:function(){
	  var $this = this;
	  //$this.hostList();
	  $this.resourceList('master',null);
	  $this.editor = CodeMirror.fromTextArea(document.getElementById('fileInfoDlg'), {
         lineNumbers : true,
         mode : "xml",
         matchBrackets : true,
         theme : "monokai",
         showCursorWhenSelecting:true
      });
      /*Vue.nextTick(function(){
        $this.change();
      });*/
  },
  /*watch:{
      'isFile':function(val){
          var $this = this;
          if(val){
            $this.editor = CodeMirror.fromTextArea(document.getElementById('etlWorkerDlg-add-code'), {
               lineNumbers : true,
               mode : "xml",
               matchBrackets : true,
               theme : "monokai",
               showCursorWhenSelecting : true
            });
          }
      }
  },*/
  methods:{
	  hostList:function(){
		  var $this = this;
		  Jcoder.ajax('/admin/ioc/hostList', 'post',{groupName:$this.groupName},null).then(function (data) {
				JqdeBox.unloading();
				if(data.ok){
				  for(var key in data.obj){
                    $this.hosts.push(key);
                    if(data.obj[key].current)$this.checkedHosts.push(key);
                  }
			    }else{
			    	JqdeBox.message(false, data.msg);
			    }
		  });
	  },
	  resourceList:function(host){
	      var $this = this;
          Jcoder.ajax('/admin/fileInfo/getFileTree', 'post',{
            hostPort: host,
            groupName:$this.groupName
          },null).then(function (data) {
            JqdeBox.unloading();
            if(data.ok){
                //Vue.nextTick(function(){
                    initResourceTree(data.obj);
                    console.log(data.obj);
                //});
            }else{
                JqdeBox.message(false, data.msg);
            }
          });
      },
      downFile:function(){
        var $this = this;
        console.log($this.currentNode);
        location.href = "/admin/resource/downFile?groupName="+$this.groupName+"&path="+$this.currentNode.file.relativePath ;
      },
      createFolder:function(){
          var $this = this;
          JqdeBox.dialog({
              title: 'CreateFolder',
              url: 'modules/resource/createFolder.html',
              confirm: function () {
                if(createFolder.valid()){
                    Jcoder.ajax('/admin/resource/createFolder', 'post',{hostPorts:$this.checkedHosts,groupName:$this.groupName,
                    path:filePath,folderName:createFolder.folderName},null).then(function (data) {
                        JqdeBox.unloading();
                        debugger;
                        if(data.ok){
                            //Vue.nextTick(function(){
                                initResourceTree(data.obj);
                                console.log(data.obj);
                            //});
                        }else{
                            JqdeBox.message(false, data.msg);
                        }
                    });
                }
              }
          });
      },
      uploadFile:function(){
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
      },
      change: function () {
          var $this = this;
          _.each($this.hosts, function (host) {
              if(host.selected){
                $this.resourceList(host.host);
              }
          });
      }
  }
});