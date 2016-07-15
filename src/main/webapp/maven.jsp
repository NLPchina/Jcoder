<%@page language="java" pageEncoding="UTF-8"%>
<%@include file="/common/taglibs.jsp"%>
<%@include file="/common/common.jsp"%>

<!DOCTYPE html>
<html lang="cn">
<%@include file="/header.jsp"%>
<link rel="stylesheet" href="${ctx }/editor/lib/codemirror.css">
<link rel="stylesheet" href="${ctx }/editor/addon/dialog/dialog.css">
<link rel="stylesheet" href="${ctx }/editor/theme/monokai.css">

<style type="text/css">
.CodeMirror {
	border-top: 5px solid #eee;
	border-bottom: 5px solid #eee;
	overflow: none;
	font-size: 18px;
	height: 800px;
}
</style>
<!-- topbar ends -->
<div class="ch-container">
	<div class="row">
		<!-- left menu starts -->
		<%@include file="/left.jsp"%>
		<!-- left menu ends -->
		<div id="content" class="col-lg-10 col-sm-10">
		<div class="row">
			<div class="box col-md-12">
				<div class="box-inner">
					<div class="box-header well" data-original-title="">
						<h2><i class="glyphicon glyphicon-edit"></i> Form Elements</h2>
					</div>
					<div class="box-content">
						<form action="" method="post" name="mavenForm" id="mavenForm">
						<div class="form-group">
							<label for="exampleInputEmail1">MAVEN_PATH:</label>
							<input type="text" id="mavenPath" name="mavenPath" class="form-control" placeholder="Enter MavenPath" value="${obj.result.mavenPath }">
						</div>
						
						<div id="codeDiv" class="form-group ">
								<textarea id="code" name="content">${obj.result.content}</textarea>
						</div>
						<div class="form-group has-error col-md-1" style="float: right">
									<label class="control-label" for="inputError1">&nbsp;</label>
									<a id="submit_maven" class="btn btn-danger" href="#"><i class="glyphicon glyphicon-save">Save&Reload</i></a>
								</div>
						</form>
					</div>
				</div>
			</div>
		</div>
		
		
		<!-- left menu ends -->
		<c:set var="console_task" value="false" scope="request"></c:set>
		<c:set var="console_filter" value="" scope="request"></c:set>
		<c:set var="console_contains" value="JarService" scope="request"></c:set>
		<c:set var="console_height" value="300px;" scope="request"></c:set>
		<jsp:include page="/console.jsp"></jsp:include>
		</div>
	</div>

	<div class="modal fade" id="myModal" tabindex="-1" >
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>Save</h3>
				</div>
				<div class="modal-body">
					<p id="dialog_message"></p>
				</div>
				<div class="modal-footer">
					<a href="#" class="btn btn-primary" data-dismiss="modal">Confirm</a>
				</div>
			</div>
		</div>
	</div>
</div>
	<%@include file="/footer.jsp"%>

	<!-- websocket -->
	<script src="${ctx }/js/jquery.gracefulWebSocket.js"></script>
	<!--DEVEL-->
	<script src="${ctx }/editor/lib/codemirror.js"></script>
	<script src="${ctx }/editor/addon/dialog/dialog.js"></script>
	<script src="${ctx }/editor/addon/search/searchcursor.js"></script>
	<script src="${ctx }/editor/mode/clike/clike.js"></script>
	<script src="${ctx }/editor/addon/edit/matchbrackets.js"></script>
	<script src="${ctx }/editor/keymap/vim.js"></script>
	<script src="${ctx }/editor/addon/display/fullscreen.js"></script>
	<script src="${ctx }/editor/mode/xml/xml.js"></script>
	<script>
		CodeMirror.commands.save = function() {
			alert("Saving");
		};
		var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
			lineNumbers : true,
			mode : "xml",
			matchBrackets : true,
			theme : "monokai",
			showCursorWhenSelecting : true,
			extraKeys : {
				"F11" : function(cm) {
					cm.setOption("fullScreen", !cm.getOption("fullScreen"));
				},
				"Esc" : function(cm) {
					if (cm.getOption("fullScreen"))
						cm.setOption("fullScreen", false);
				},
				
				"Ctrl-G" : function(cm) {
					$("#script_start").click();
				}
			}
		});


		$('#code_type').change(function() {
			codeEditorFullCode();
		});


		

		$('#submit_maven').click(function() {
			$("#code").val(editor.getValue());
			var d = $("#mavenForm").serialize();
			$.messager.confirm("Alert", "are you sure save it！", function() { 
				$.post("${ctx }/maven/save", d, function(result) {
					$('#dialog_message').html(result.message);
					$('#myModal').modal('show');
				}, "json").error(function() {
					$('#dialog_message').text('save errr may be your session has lost！');
					$('#myModal').modal('show');
				});
			});
		});
		
	</script>
	</body>
</html>
