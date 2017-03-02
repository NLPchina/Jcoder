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
						<div class="box-header well " data-original-title="">
							<h2>
								<i class="glyphicon glyphicon-edit"></i> Code Platform
							</h2>
							<i class="col-md-6"></i>
							<h2 class="progress" class="col-md-4">
							</h2>
						</div>
						<form action="" method="post" name="iocForm" id="iocForm">
							<div>
								
								<div id="codeDiv" class="input-group col-md-12" style="padding: 1em;">
									<textarea id="code" name="code">${obj}</textarea>
									<div class="form-group has-error col-md-11" >&nbsp;</div>
									<div class="form-group has-error col-md-1" style="float: right">
										<label class="control-label" for="inputError1">&nbsp;</label>
										<a id="submit_ioc" class="btn btn-danger" href="#"><i class="glyphicon glyphicon-save">Save&Reload</i></a>
									</div>
								</div>

							</div>
						</form>
					</div>
				</div>
				<!--/span-->
			</div>
		</div>
	</div>

	<div class="modal fade" id="myModal" tabindex="-1" >
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>保存</h3>
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

	<!--DEVEL-->
	<script src="${ctx }/editor/lib/codemirror.js"></script>
	<script src="${ctx }/editor/addon/dialog/dialog.js"></script>
	<script src="${ctx }/editor/addon/search/searchcursor.js"></script>
	<script src="${ctx }/editor/addon/edit/matchbrackets.js"></script>
	<script src="${ctx }/editor/keymap/vim.js"></script>
	<script src="${ctx }/editor/addon/display/fullscreen.js"></script>
	<script src="${ctx }/editor/mode/javascript/javascript.js"></script>
	<script>
		CodeMirror.commands.save = function() {
		};
		var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
			lineNumbers : true,
			mode : "javascript",
			matchBrackets : true,
			theme : "monokai",
			showCursorWhenSelecting : true
			
		});


		$('#code_type').change(function() {
			codeEditorFullCode();
		});


		

		$('#submit_ioc').click(function() {
			$("#code").val(editor.getValue());
			var d = $("#iocForm").serialize();
			$.messager.confirm("Alert", "Are you sure to save it ?", function() { 
				$.post("${ctx }/ioc/save", d, function(result) {
					$('#dialog_message').text(result.message);
					$('#myModal').modal('show');
				}, "json").error(function() {
					$('#dialog_message').text('Save err ！');
					$('#myModal').modal('show');
				});
			});
		});
		
	</script>
	</body>
</html>
