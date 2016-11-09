<%@page language="java" pageEncoding="UTF-8"%>
<%@include file="/common/taglibs.jsp"%>
<%@include file="/common/common.jsp"%>
<!DOCTYPE html>
<html lang="cn">
<%@include file="/header.jsp"%>
<div class="ch-container">
	<div class="row">
		<!-- left menu starts -->
		<%@include file="/left.jsp"%>
		<!-- left menu ends -->
		<div id="content" class="col-lg-10 col-sm-10">
			<div class="row">
				<ul id="myTab" class="nav nav-tabs">
					<li class="active"><a href="#duli" data-toggle="tab" onclick="tabDatas(1);"> API</a></li>
					<li><a href="#jihua" data-toggle="tab" onclick="tabDatas(2);">Cron</a></li>
					<li><a href="#laji" data-toggle="tab" onclick="tabDatas(0);">Recycle </a></li>
					<c:if test="${userType==1 || AUTH_MAP[groupId]==2}">
						<li><a href="#xinjian" data-toggle="tab" onclick="location.href='${ctx}/task/_new/${groupId }'">Create Task</a></li>
					</c:if>
				</ul>
			</div>
			<div class="tab-content">
				<div class="tab-pane fade in active" id="duli">
					<div class="row">
						<div class="box col-md-12">
							<div class="box-inner">
								<div class="box-content">
									<table class="table table-striped table-bordered ">
										<thead>
											<tr>
												<th>Name</th>
												<th>Description</th>
												<th>Status</th>
												<th>Success</th>
												<th>Err</th>
												<th>Editor</th>
												<th>Modified Time</th>
												<th>Info</th>
												<th>Edit</th>
												<c:if test="${userType==1 || AUTH_MAP[groupId]==2}">
												<th>Delete</th>
												</c:if>
											</tr>
										</thead>
										<tbody id="1body">
										</tbody>
									</table>
								</div>
							</div>
						</div>
						<!--/span-->
					</div>
				</div>
				<div class="tab-pane fade" id="jihua">
					<div class="row">
						<div class="box col-md-12">
							<div class="box-inner">
								<div class="box-content">
									<table class="table table-striped table-bordered ">
										<thead>
											<tr>
												<th>Name</th>
												<th>Description</th>
												<th>Status</th>
												<th>Success</th>
												<th>Err</th>
												<th>Editor</th>
												<th>Modified Time</th>
												<th>Info</th>
												<th>Edit</th>
												<c:if test="${userType==1 || AUTH_MAP[groupId]==2}">
												<th>Delete</th>
												</c:if>
											</tr>
										</thead>
										<tbody id="2body">
										</tbody>
									</table>
								</div>
							</div>
						</div>
						<!--/span-->
					</div>
				</div>

				<div class="tab-pane fade" id="laji">
					<div class="row">
						<div class="box col-md-12">
							<div class="box-inner">
								<div class="box-content">
									<table class="table table-striped table-bordered ">
										<thead>
											<tr>
												<th>Name</th>
												<th>Description</th>
												<th>Status</th>
												<th>Success</th>
												<th>Err</th>
												<th>Editor</th>
												<th>Modified Time</th>
												<th>Info</th>
												<th>Edit</th>
												<c:if test="${userType==1 || AUTH_MAP[groupId]==2}">
												<th>Delete</th>
												</c:if>
											</tr>
										</thead>
										<tbody id="0body">
										</tbody>
									</table>
								</div>
							</div>
						</div>
						<!--/span-->
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<table hidden="hidden">
	<tr id="clone">
		<td id="taskName"></td>
		<td><textarea id="taskDescription" style="width: 98%; height: 35px;"></textarea></td>
		<td id="taskStatus" class="center"></td>
		<td class="center"><span id="taskSuccess" class="label-success label label-default"></span></td>
		<td class="center"><span id="taskError"	class="label-success label label-danger"> </span></td>
		<td class="center" id="taskUpdateUser"></td>
		<td class="center" id="taskUpdateTime"></td>
		<td><textarea id="taskMessage" style="width: 98%; height: 35px;"></textarea></td>
		<td class="center">	<a class="btn btn-info" id="taskEdir"> <i class="glyphicon glyphicon-edit icon-white"></i> Edit</a></td>
		<c:if test="${userType==1 || AUTH_MAP[groupId]==2}">
		<td class="center"><a class="btn btn-danger" href="#" id="taskDelete"><i class="glyphicon glyphicon-trash icon-white"></i> Del</a></td>
		</c:if>
</table>
<script type="text/javascript">
	tabDatas(1);
	function tabDatas(m) {
		url = "/task/type/${groupId}";
		$.ajax({
			'url' : url,
			'dataType' : 'json',
			'type' : 'POST',
			'data' : {
				taskType : m
			},
			'beforeSend' : function(XMLHttpRequest) {
				//alert('远程调用开始...');
				// $("#loading").html("<img src='/img/loading.gif' />");
			},
			'error' : function(e) {
				//alert(e.repsonseText);
			},
			'success' : function(data) {
				$("#" + m + "body").html("");
				var d;
				if (m == 0) {
					d = "del"
				} else {
					d = "delete"
				}
				$.each(data.tasks, function(i, n) {
					var row = $("#clone").clone();
					row.find("#taskName").text(n.name);
					row.find("#taskDescription").html(n.description);
					row.find("#taskStatus").html(n.status!=0?'<span	class="label-success label label-default">Active</span>':'<span class="label-success label label-danger">Stop</span>');
					row.find("#taskSuccess").text(n.success+' times');
					row.find("#taskError").text(n.error+' times');
					row.find("#taskMessage").text(n.message);
					row.find("#taskUpdateUser").text(n.updateUser);
					var updateTime = new Date(n.updateTime) ;
					var timeStamp = (updateTime.getYear()+1900)+'-'+(updateTime.getMonth()+1)+'-'+updateTime.getDate()+' '+updateTime.getHours()+":"+updateTime.getMinutes()+':'+updateTime.getSeconds() ;
					row.find("#taskUpdateTime").text(timeStamp);
					row.find("#taskEdir").attr("href","${ctx }/task/editor/"+n.groupId+"/" + n.id);
					row.find("#taskDelete").click(function() {delTask(d, n.name, this);
					});
					row.appendTo("#" + m + "body");
				});
			}
		});
	}
	function delTask(u, m, n) {
		if (!confirm("Are you to delete it : " + m)) {
			return;
		}
		url = "/task/" + u + "/" + m;
		$.get(url, function(data, status) {
			if (data == 'false') {
			} else {
				var tr = n.parentNode.parentNode;
				tr.parentNode.deleteRow(tr.rowIndex - 1);
			}
		});
	}
</script>
<%@include file="/footer.jsp"%>
</body>
</html>
