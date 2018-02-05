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
            var hostPort = null;
            _.each(resourceManager.hosts, function (host) {
                  if(host.selected){
                    hostPort = host.host;
                  }
            });
            if(hostPort == null)hostPort = "master";
            Jcoder.ajax('/admin/fileInfo/fileContent', 'post',{
                hostPort: hostPort,
                groupName:resourceManager.groupName,
                relativePath:treeNode.file.relativePath
              },null).then(function (data) {
                JqdeBox.unloading();
                if(data.ok){
                    resourceManager.editor.setValue(data.message);
                    if(resourceManager.currentNode.file.length.toFixed(2)/1024/1024 < 1){
                        if(resourceManager.currentNode.file.name.indexOf('.jar') > -1 ){
                            $("#saveBtn").css('display','none');
                        }else{
                            $("#saveBtn").css('display','block');
                        }
                    }else{
                        $("#saveBtn").css('display','none');
                    }
                }else{
                    JqdeBox.message(false, data.msg);
                }
            });
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
	resourceManager.resources = [];
	for(var i = 0;i<resourceFiles.length;i++){
	    if(resourceFiles[i].pId == "0")resourceManager.resources.push(resourceFiles[i]);
	}
}

function selectNode(nodeName){
    var zTree = $.fn.zTree.getZTreeObj("treeDemo"),
    treeNode = zTree.getNodeByParam("name",nodeName);
    zTree.selectNode(treeNode,false,false);
    resourceManager.currentNode = treeNode;
    resourceManager.resources = [];
    if(treeNode.isParent){
        $("#folderContent").css("display","block");
        $("#fileContent").css("display","none");
        for(var i = 0;i<zNodes.length;i++){
            if(zNodes[i].pId == treeNode.id){
                resourceManager.resources.push(zNodes[i]);
            }
        }
    }else{
        $("#folderContent").css("display","none");
        $("#fileContent").css("display","block");
        changeFileInfo(treeNode);
    }
}

function changeFileInfo(treeNode){
    if(resourceManager.editor != null && resourceManager.editor.getValue() != null){
        var hostPort = null;
        _.each(resourceManager.hosts, function (host) {
              if(host.selected){
                hostPort = host.host;
              }
        });
        if(hostPort == null)hostPort = "master";
        Jcoder.ajax('/admin/fileInfo/fileContent', 'post',{
            hostPort: hostPort,
            groupName:resourceManager.groupName,
            relativePath:treeNode.file.relativePath
          },null).then(function (data) {
            JqdeBox.unloading();
            if(data.ok){
                if(treeNode.file.length.toFixed(2)/1024/1024 < 1){
                    if(resourceManager.currentNode.file.name.indexOf('.jar') > -1 ){
                        $("#saveBtn").css('display','none');
                    }else{
                        $("#saveBtn").css('display','block');
                    }
                }else{
                    $("#saveBtn").css('display','none');
                }
                resourceManager.editor.setValue(data.message);
            }else{
                JqdeBox.message(false, data.msg);
            }
        });
        return false;
    }
}

var resourceManager = new Vue({
  el: '#resourceManager',
  data: {
    resources:[],
    hosts:[],
    groupName:param.name,
    editor: '',
    isFile:false,
    currentNode:null,
    filePath:[]
  },
  mounted:function(){
	  var $this = this;
	  $this.resourceList('master',null);
	  $this.editor = CodeMirror.fromTextArea(document.getElementById('fileInfoDlg'), {
         lineNumbers : true,
         mode : "xml",
         matchBrackets : true,
         theme : "monokai",
         showCursorWhenSelecting:true
      });
      // check all
      var $table = $('#file-table');
      $table.on('click', 'th input[type=checkbox]', function () {
        var th_checked = this.checked;
        $table.find('td input[type=checkbox]').each(function () {
            this.checked = th_checked;
        });
        $this.checkChanged();
      });
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
	  resourceList:function(host,nodeName){
	      JqdeBox.loading();
	      var $this = this;
          Jcoder.ajax('/admin/fileInfo/getFileTree', 'post',{
            hostPort: host,
            groupName:$this.groupName
          },null).then(function (data) {
            JqdeBox.unloading();
            if(data.ok){
                //Vue.nextTick(function(){
                    initResourceTree(data.obj);
                    if(nodeName != null && nodeName != undefined)selectNode(nodeName);
                    console.log(data.obj);
                //});
            }else{
                JqdeBox.message(false, data.msg);
            }
          });
      },
      downFile:function(path){
        var $this = this;
        console.log($this.currentNode);
        var hostPort = null;
        _.each($this.hosts, function (host) {
              if(host.selected){
                hostPort = host.host;
              }
        });
        if(hostPort == null)hostPort = "master";
        var filePath = $this.currentNode.file.relativePath ;
        if(path)filePath = path;
        location.href = "/admin/fileInfo/downFile?hostPort="+hostPort+"&groupName="+$this.groupName+"&relativePath="+filePath;
      },
      del:function(path){
        var $this = this;
        JqdeBox.dialog({
          title: '上传文件',
          url: 'modules/resource/deleteFile.html',
          init: function () {
             importFile.filePath = path;
             _.each($this.hosts, function (host) {
                importFile.hosts.push(host.host);
               if(host.selected){
                 importFile.checkedHosts.push(host.host);
               }
             });
          },
          confirm: function () {
            if(importFile.checkedHosts.length == 1 && importFile.checkedHosts[0] == "master"){
                JqdeBox.message(false, "无法只删除Master主机的文件！");
                return false;
            }
            if($this.filePath != null && $this.filePath != undefined && $this.filePath.length > 0){
                path = $this.filePath;
            }else{
                var arr = new Array();
                arr.push(path);
                path = arr;
            }
            Jcoder.ajax('/admin/fileInfo/deleteFile', 'post',
                {hostPort:importFile.checkedHosts,groupName:$this.groupName,"relativePaths[]":path},null).then(function (data) {
                JqdeBox.unloading();
                JqdeBox.message(data.ok, data.message);
                JqdeBox.unloading();
                if(data.ok){
                    var msg = data.message.substring(data.message.indexOf(":") , data.message.length).split(',');
                    debugger;
                    for(var i = 0;i < path.length;i++){
                        if(msg[i].indexOf("成功") > -1)$this.resources.splice($.inArray(path[i],$this.resources),1);
                    }
                    $('#file-table').find('td input[type=checkbox]').each(function () {
                         this.checked = false;
                     });
                    _.each($this.hosts, function (host) {
                       if(host.selected){
                         var fileName = $this.currentNode.file.name;
                         $this.resourceList(host.host,fileName);
                         return true;
                       }
                    });
                }
            });
          }
        });
      },
      createFolder:function(){
            var $this = this;
            JqdeBox.dialog({
                title: 'CreateFolder',
                url: 'modules/resource/createFolder.html',
                confirm: function () {
                  if(createFolder.valid()){
                      var folderNode = {name:$("#fn").val(),id:"'"+new Date().getTime()+"'",
                        pId:$this.currentNode.id,open:true,isParent:true,file:{name:$("#fn").val(),length:0,directory:true,
                        relativePath:$this.currentNode.file.relativePath+$("#fn").val(),date:new Date().getTime()}};
                      zNodes.push(folderNode);
                      initResourceTree(zNodes);
                  }else{
                    return false;
                  }
                }
            });
        },
      uploadFile:function(){
        var $this = this;
        JqdeBox.dialog({
          title: '上传文件',
          url: 'modules/resource/fileUpload.html',
          init: function () {
             importFile.filePath = $this.currentNode.file.relativePath;
             _.each($this.hosts, function (host) {
                importFile.hosts.push(host.host);
               if(host.selected){
                 importFile.checkedHosts.push(host.host);
               }
             });
          },
          confirm: function () {
            if(importFile.checkedHosts.length == 1 && importFile.checkedHosts[0] == "master"){
                JqdeBox.message(false, "无法把文件只上传到Master主机！");
                return false;
            }
            var formData = new FormData();
            var files = $('#id-input-file-3').prop("files");
            for(var i = 0;i < files.length;i++){
                /*if((files[i].size/10240) > 1){
                    JqdeBox.message(false, "无法上传大于2M的文件！");
                    return false;
                }*/
                formData.append('file', files[i]);
            }
          　$.ajax({
              url:"/admin/fileInfo/uploadFile?group_name="+$this.groupName+"&hostPorts="+importFile.checkedHosts
                                                              +"&filePath="+importFile.filePath,
              type:"post",
              data:formData,
              processData:false,
              contentType:false,
              cache: false,
              success:function(data){
                 JqdeBox.unloading();
                 $this.resourceList('master',null);
                 JqdeBox.message(data.ok, data.message);
              }
            });
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
      },
      nodeInfo:function(fileName){
            selectNode(fileName);
      },
      //保存文件内容并刷新
      saveAndFlush:function(){
        var $this = this;
        JqdeBox.dialog({
          title: '保存',
          url: 'modules/resource/deleteFile.html',
          init: function () {
             importFile.filePath = $this.currentNode.file.relativePath;
             _.each($this.hosts, function (host) {
               importFile.hosts.push(host.host);
               if(host.selected){
                 importFile.checkedHosts.push(host.host);
               }
             });
          },
          confirm: function () {
            Jcoder.ajax('/admin/fileInfo/saveAndFlush', 'post',
                {hostPorts:importFile.checkedHosts,groupName:$this.groupName,
                content:$this.editor.getValue(),relativePath:$this.currentNode.file.relativePath},null).then(function (data) {
                JqdeBox.unloading();
                JqdeBox.message(data.ok, "成功！");
            });
          }
        });
      },
      // 选中改变
      checkChanged: function () {
        var idsTemp = [];
        $('#file-table').find('td input[type=checkbox]').each(function () {
            if (this.checked) {
                idsTemp.push(this.value);
            }
        });
        this.filePath = idsTemp;
      }
  }
});