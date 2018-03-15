vmApp.module = new Vue({
  el: '#logsManager',
  data: {
    fileInfo:null,
    hosts:[],
    editor: '',
    groups:[],
    webSockets:{},
    ok:true
  },
  mounted:function(){
	  var $this = this;
	  $this.getHosts();
	  $this.getGroups();
	  setInterval($this.ws_ping(), 25000); // 25秒一次就可以了
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
             Vue.nextTick(function(){
              for(var i = 0;i < $this.hosts.length;i++){
                  $(":checkbox").each(function(index){
                      if($(this).val() == $this.hosts[i])$(this).attr('checked',true);
                  });
               }
               $this.initLogs('_jcoder_log');
             });
          }else{
             JqdeBox.message(false, "主机列表获取失败！");
          }
        });
      },
      getGroups:function(){
          var $this = this;
          Jcoder.ajax('/admin/logs/getAllGroups', 'post',null,null).then(function (data) {
            if(data.ok){
               for(var i = 0;i < data.obj.length; i++){
                  $this.groups.push(data.obj[i]);
               }
            }else{
               JqdeBox.message(false, "group列表获取失败！");
            }
          });
      },
      //初始化的时候执行一次
      initLogs:function(group){
        $('#groups ul li').eq(0).children().eq(0).addClass('infobox-green');
        var me = this;
        for(var i = 0; i < me.hosts.length; i++){
            // 首先,需要创建一个WebSocket连接
            var ws = new WebSocket("ws://"+me.hosts[i]+"/log");
            var key = me.hosts[i]+"";
            me.webSockets[key] = ws;

            ws.onopen = function(event) {
                event.target.send(JSON.stringify({'groupName':group,'methodName':"join"}));
            };
            ws.onerror = function(event){
                console.log(event);
            };
            // 收到服务器发来的信息时触发的回调
            ws.onmessage = function(event) {
                var message = JSON.parse(event.data).message;

                var temp = $("#console_contains").val();

                if (temp != undefined && temp != "" && message.indexOf(temp) == -1) {
                    return;
                }

                temp = $("#console_filter").val();
                if (temp != "" && message.indexOf(temp) >= 0) {
                    return;
                }
                $("#logsInfoConsole").append("<p>"+event.target.url.split('/')[2]+'-->'+JSON.parse(event.data).threadName+'-->'+JSON.parse(event.data).message+"</p>");
            };
        }
      },
      ws_ping:function(){
        var me = this;
        for(var i = 0; i < me.webSockets.length; i++){
           if (me.webSockets[i]) {
           		me.webSockets[i].send("{}"); // TODO 断线重连.
           	}
        }
      },
      //获取指定group日志信息
      getGroupLogInfo:function(groupName){
        var $this = this;
        $("#groups ul li").each(function(index){
            var currentGroup = $(this).children().eq(0);
            if(groupName == currentGroup.prop('id').replace("\n","")){
                if(currentGroup.hasClass('infobox-green')){
                    $(this).children().eq(0).removeClass('infobox-green');
                    $this.openOrCloseGroupLog(groupName,'left');
                }else{
                    $(this).children().eq(0).addClass('infobox-green');
                    $this.openOrCloseGroupLog(groupName,'join');
                }
                return false;
            }
        });
      },
      //开启指定group日志监控
      openOrCloseGroupLog:function(group,type){
        var me = this;
        $(":checkbox").each(function(index){
          if($(this).is(":checked")){
              //获取连接
              var key = $(this).val()+"";
              var ws = me.webSockets[key];
              //把连接中指定group开启或移除
              ws.send(JSON.stringify({'groupName':group,'methodName':type}));
          }
        });
      },
      openOrCloseHost:function(host){
        var me = this;
        var key = host+'';
        var ws = me.webSockets[key];
        var type = '';
        $(":checkbox").each(function(index){
          if($(this).val() == host){
            if($(this).is(":checked")){
                type = 'join';
            }else{
                type = 'left';
            }
          }
        });
        $("#groups ul li").each(function(index){
            var currentGroup = $(this).children().eq(0);
            if(currentGroup.hasClass('infobox-green')){
                ws.send(JSON.stringify({'groupName':currentGroup.prop('id'),'methodName':type}));
            }
            return false;
        });
      },
      //清空控制台信息
      cleanConsole:function(){
        $("#logsInfoConsole").empty();
      }
  },
  beforeDestroy: function () {
    var me = this;
    for(var i = 0;i < me.webSockets.length;i++){
        me.webSockets[i].close();
    }
  },
});

function sleep(n) { //n表示的毫秒数
    var start = new Date().getTime();
    while (true) if (new Date().getTime() - start > n) break;
}

