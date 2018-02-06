var logsManager = new Vue({
  el: '#logsManager',
  data: {
    fileInfo:null,
    editor: ''
  },
  mounted:function(){
	  var $this = this;
	  $this.editor = CodeMirror.fromTextArea(document.getElementById('fileInfoDlg'), {
         lineNumbers : true,
         mode : "xml",
         matchBrackets : true,
         theme : "monokai",
         showCursorWhenSelecting:true
      });
  },
  methods:{
	  getLogsInfo:function(host,groupName){
	      JqdeBox.loading();
	      var $this = this;
          Jcoder.ajax('/admin/fileInfo/getFileTree', 'post',{
            hostPort: host,
            groupName:groupName
          },null).then(function (data) {
            JqdeBox.unloading();
            if(data.ok){

            }else{
                JqdeBox.message(false, data.msg);
            }
          }).catch(function (data) {
            JqdeBox.unloading();
            JqdeBox.message(false, data.responseText);
          });
      }
  }
});