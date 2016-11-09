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
	border-top: 1px solid #eee;
	border-bottom: 1px solid #eee;
	overflow: none;
	font-size: 18px;
	height: 500px;
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
						<form action="" method="post" name="taskForm" id="taskForm">
							<input type="hidden" value="${task.taskId==null?task.id:task.taskId}" id="task.id" name="task.id" />
							<input type="hidden" value="${task.createUser }" id="task.createUser" name="task.createUser" />
							<input type="hidden"  id="task.createTime" name="task.createTime"  value='<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${task.createTime}" />'/>
							<div class="box-content">
								<div class="form-group has-success col-md-1">
									<div class="controls">
										<label class="control-label" for="inputWarning1">Type</label>
										<select id='task_type' name='task.type' class="form-control">
											<option value=1 ${task.type==1?"selected":""}>API</option>
											<option value=2 ${task.type==2?"selected":""}>Cron</option>
										</select>
									</div>
								</div>
								<div class="form-group has-success col-md-1">
									<div class="controls">
										<label class="control-label" for="inputWarning1">status</label> <select
											id="task.status" name="task.status" class="form-control">
											<option value="0" ${task.status==0?"selected":""}>Stop</option>
											<option value="1" ${task.status==1?"selected":""}>Active</option>
										</select>
									</div>
								</div>
								<div class="form-group has-warning col-md-4">
									<label class="control-label" for="inputWarning1">Description</label> <input
										type="text" class="form-control" name="task.description"
										value="${task.description}" />
								</div>

								<div class="form-group has-error col-md-2" id="zhixing"
									style="display: none;">
									<label class="control-label" for="inputError1">CronExpression</label> <input
										type="text" class="form-control" name="task.scheduleStr"
										value="${task.scheduleStr}" />
								</div>

								<!-- 选择组 -->
								<div class="form-group has-error col-md-2" id="change_group">
									<label class="control-label" >Group</label> <select
										class="form-control" id="change_group" name="task.groupId">
										<c:forEach items="${GROUP_LIST }" var="temp">
											<c:if test="${userType==1 ||AUTH_MAP[temp.id]==2 }">
												<option value="${temp.id }"
													${temp.id==groupId?"selected":""}>${temp.name }</option>
											</c:if>
										</c:forEach>
									</select>
								</div>
								
								<c:if test="${userType==1 ||AUTH_MAP[temp.id]==2 }">
								<div class="form-group has-error col-md-1" id="">
									<label class="control-label" >versions</label>
									 <select class="form-control" id="change_version" >
										<c:forEach items="${versions}" var="v">
											<option ${task.version==v?'selected="selected"':''} value="${v}">${v}</option>
										</c:forEach>
									</select>
								</div>
								</c:if>
								
								<c:if test="${userType==1 || AUTH_MAP[groupId]==2}">
									<div class="form-group has-error col-md-1">
										<label class="control-label" for="inputWarning1">Save</label>
										<a id="submit_task" class="btn btn-danger" href="#"><i class="glyphicon glyphicon-save icon-white"></i>Save</a>
									</div>
								</c:if>
								
								<br>
								<div id="codeDiv" class="input-group col-md-12"
									style="padding: 1em;">
									<textarea id="code" style="width:100%; " name="task.code">${task.code}</textarea>
								</div>

							</div>
						</form>
					</div>
				</div>
				<!--/span-->
			</div>
			<!--/row-->
			<c:if test="${userType==1 || AUTH_MAP[groupId]==2}">
				<c:set var="console_task" value="true" scope="request"></c:set>
				<c:set var="console_filter" value="" scope="request"></c:set>
				<c:set var="console_contains" value="${task.name}" scope="request"></c:set>
				<c:set var="console_height" value="200px;" scope="request"></c:set>
				<jsp:include page="/console.jsp"></jsp:include>
			</c:if>
			<!--/row-->
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
	<script src="${ctx }/editor/lib/codemirror.js"></script>
	<script src="${ctx }/editor/addon/dialog/dialog.js"></script>
	<script src="${ctx }/editor/addon/search/searchcursor.js"></script>
	<script src="${ctx }/editor/addon/edit/matchbrackets.js"></script>
	<script src="${ctx }/editor/keymap/vim.js"></script>
	<script src="${ctx }/editor/addon/display/fullscreen.js"></script>
	<script src="${ctx }/editor/mode/python/python.js"></script>
	<script>
		CodeMirror.commands.save = function() {
			alert("Saving");
		};
		var editor = CodeMirror.fromTextArea(document.getElementById("code"), {
			lineNumbers : true,
			mode : "python",
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
				"Ctrl-S" : function(cm) {

				},
				"Ctrl-G" : function(cm) {
					$("#script_start").click();
				}
			}
		});

		editor.setSize($("#console").width());

		var javaCode = "package org.nlpcn.jcoder.code;\n\nimport org.nlpcn.jcoder.run.java.Execute;\n\npublic class <ClassName> implements Execute{\n\n\tpublic void execute() throws InterruptedException{\n\t}\n}";

		function codeEditorFullCode() {

			if ($("#code_type").val() == "java") {

				var tempCode = editor.getValue();

				if (tempCode == null || tempCode == '' || javaCode == tempCode) {
					if ($("#code_type").val() == "java") {
						editor.setValue(javaCode);
						$("#code").val(javaCode);
					}
				}

				$("#task_name_span")
						.html(
								'<input type="text" class="form-control" name="task.name" value="${task.name }" />');

			}
		}

		$('#code_type').change(function() {
			codeEditorFullCode();
		});

		$('#code_type').change();

		$('#task_type').change(function() {
			if ($('#task_type').val() == 1) {
				$('#zhixing').css("display", "none");
			} else if ($('#task_type').val() == 2) {
				$('#zhixing').css("display", "block");
			} else {
				alert('???error type!');
			}
		});

		$('#task_type').change();
		
		
		$('#taskName').change(function(){
			$('#console_contains').val($('#taskName').val()) ;
		}) ;


		$('#submit_task').click(function() {
			$("#code").val(editor.getValue());
			var d = $("#taskForm").serialize();
			$.messager.confirm("Alert", "Are you sure to save it！", function() { 
				$.post("${ctx }/task/save/${groupId}", d, function(result) {
					if (result.ok) {
						if(!result.save){
							$.messager.alert("message:","The code have no change ,so not save it to history Task");	
						}else{
							document.getElementById("task.id").value = result.id;
							location.href="/task/editor/${groupId}/"+result.id;	
						}
					}else{
						$('#dialog_message').text(result.message);
						$('#myModal').modal('show');
					}
				}, "json").error(function() {
					$('#dialog_message').text('Save err , my be your session timeout！');
					$('#myModal').modal('show');
				});
			});
		});
		
		
		$('#change_version').change(function() {
			var childs = $("#change_version").children();
			var version = $("#change_version").val();
			if(childs[0].value!=version){
				location.href="/task/editor/${groupId}/${task.taskId==null?task.id:task.taskId}?version="+version;
			}else{
				location.href="/task/editor/${groupId}/${task.taskId==null?task.id:task.taskId}";
			}
		});
		
		
		function jumpTo(url,mess){
			$.messager.confirm("Alert", mess, function() { 
				window.location.href=url;
			});
		} 
	</script>
	</body>
</html>
