var logsManager = new Vue({
  el: '#logsManager',
  data: {
    fileInfo:null,
    hosts:[],
    editor: '',
    groups:[],
    webSockets:[],
    checkedHosts:[],
    activeGroup:'',
    ok:true
  },
  mounted:function(){
	  var $this = this;
	  $this.getHosts();
	  $this.getGroups();
	  /*$this.editor = CodeMirror.fromTextArea(document.getElementById('logsInfoConsole'), {
         lineNumbers : true,
         mode : "xml",
         matchBrackets : true,
         theme : "monokai",
         showCursorWhenSelecting:true
      });*/
      $this.checkedHosts = $this.hosts;
  },
  watch:{
    'checkedHosts':function(val){
        debugger;
        for(var i = 0; i < this.hosts.length; i++){
            var flag = true;
            for(var a = 0; a < this.checkedHosts.length; a++){
                if(this.hosts[i] == this.checkedHosts[a]){
                    flag = false;
                    break;
                }
            }
            if(flag){
                // 首先,需要创建一个WebSocket连接
                var ws = new WebSocket("ws://"+this.hosts[i]+"/log");
                console.log('close');
                ws.onclose = function (event) {
                    console.log("websocket onmessage", event.data);
                };
                continue;
            }
        }
        this.initLogs(this.activeGroup);
    }
  },
  methods:{
      getHosts:function(){
        JqdeBox.loading();
        var $this = this;
        Jcoder.ajax('/admin/logs/getAllHosts', 'post',null,null).then(function (data) {
          JqdeBox.unloading();
          if(data.ok){
             for(var i = 0;i < data.obj.length; i++){
                $this.hosts.push(data.obj[i]);
             }
          }else{
             JqdeBox.message(false, "主机列表获取失败！");
          }
        });
      },
      getGroups:function(){
          //JqdeBox.loading();
          var $this = this;
          Jcoder.ajax('/admin/logs/getAllGroups', 'post',null,null).then(function (data) {
            //JqdeBox.unloading();
            if(data.ok){
               for(var i = 0;i < data.obj.length; i++){
                  $this.groups.push(data.obj[i]);
               }
               Vue.nextTick(function(){
                    $this.getGroupLogInfo($this.groups[0]);
               });

            }else{
               JqdeBox.message(false, "group列表获取失败！");
            }
          });
      },
      initLogs:function(group){
        var me = this;
        for(var i = 0; i < me.checkedHosts.length; i++){
            // 首先,需要创建一个WebSocket连接
            var ws = new WebSocket("ws://"+me.checkedHosts[i]+"/log");
            me.webSockets.push(ws);
            // 连接成功后,会触发onopen回调
            ws.onopen = function(event) {
                console.log("websocket onopen ...");
                // 加入home房间
                ws.send(JSON.stringify({groupName:group,methodName:"join"}));
            };
            // 收到服务器发来的信息时触发的回调
            ws.onmessage = function(event) {
                console.log("websocket onmessage", event.data);
                $("#logsInfoConsole").append("<p>aaaaa</p>");
                $("#logsInfoConsole").append("<p>"+event.data+"</p>");
            };
        }
        //setInterval(me.ws_ping(), 25000); // 25秒一次就可以了
      },
      ws_ping:function(){
        var me = this;
        for(var i = 0; i < me.webSockets.length; i++){
           if (me.webSockets[i]) {
           		me.webSockets[i].send("{}"); // TODO 断线重连.
           	}
        }
      },
      getGroupLogInfo:function(groupName){
         var $this = this;
        $("#groups ul li").each(function(index){
            debugger;
            if(groupName == $(this).prop('innerText').replace("\n","")){
                $('.infobox-green').removeClass('infobox-green');
                $(this).children().eq(0).addClass('infobox-green');
                return false;
            }
        });
        $this.activeGroup = groupName;
        $this.initLogs($this.activeGroup);
      }
  }
});

