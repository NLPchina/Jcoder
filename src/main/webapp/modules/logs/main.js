vmApp.module = new Vue({
  el: '#logsManager',
  data: {
    fileInfo:null,
    hosts:[],
    editor: '',
    groups:[],
    webSockets:{},
    checkedHosts:[],
    ok:true
  },
  mounted:function(){
	  var $this = this;
	  $this.getHosts();
	  $this.getGroups();
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
             $this.checkedHosts = $this.hosts;
             Vue.nextTick(function(){
              for(var i = 0;i < $this.checkedHosts.length;i++){
                  $(":checkbox").each(function(index){
                      if($(this).val() == $this.checkedHosts[i])$(this).attr('checked',true);
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
        for(var i = 0; i < me.checkedHosts.length; i++){
            // 首先,需要创建一个WebSocket连接
            var ws = new WebSocket("ws://"+me.checkedHosts[i]+"/log");
            var key = me.checkedHosts[i]+"";
            me.webSockets[key] = ws;
            console.log(me.webSockets);
            // 连接成功后,会触发onopen回调
            ws.onopen = function(event) {
                // 加入home房间
                console.log('open....');
                ws.send(JSON.stringify({'groupName':group,'methodName':"join"}));
            };
            // 收到服务器发来的信息时触发的回调
            ws.onmessage = function(event) {
                console.log(event.data);
                $("#logsInfoConsole").append("<p>"+JSON.parse(event.data).message+"</p>");
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
        for(var i = 0; i < me.checkedHosts.length; i++){
            //获取连接
            var key = me.checkedHosts[i]+"";
            var ws = me.webSockets[key];
            //把连接中指定group开启或移除
            ws.send(JSON.stringify({'groupName':group,'methodName':type}));
        }
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
      }
  },
  beforeDestroy: function () {
    var me = this;
    for(var i = 0;i < me.webSockets.length;i++){
        me.webSockets[i].close();
    }
  },
});

