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
				<div class="panel panel-default">
					<div class="box-header well" data-toggle="collapse" 
								data-target="#collapseGroup">
						<h2 class="glyphicon glyphicon-user">
							<span> GroupManager </span>
						</h2>
					</div>
					<div id="collapseGroup" class="panel-collapse collapse in">
						<div class="panel-body">
							<div class="row">
								<button type="button" data-toggle="modal" onclick="add('Group');"
									class="btn btn-link  col-lg-offset-9 col-sm-offset-9"
									data-target="#myModal">Add</button>
							</div>
							<div>
								<table style="width: 100%" border="1" class="table-striped">
									<thead>
										<tr>
											<th>No</th>
											<th>GroupName</th>
											<th>Description</th>
											<th>Create Time</th>
											<th>Edit</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${groups}" var="m">
											<tr>
												<td>${m.id}</td>
												<td>${m.name}</td>
												<td>${m.description}</td>
												<td><fmt:formatDate value="${m.createTime}" type="both" /></td>
												<td><button type="button" data-toggle="modal"
														onclick="edit(this,'Group','Edit');" class="btn btn-link"
														data-target="#myModal">Edit</button>
													<button type="button" data-toggle="modal"
														onclick="edit(this,'Group','Delete');" class="btn btn-link"
														data-target="#myModal">Delete</button></td>
											</tr>
											<tr>
												<td colspan="6">
														<table class="table " >
															<thead>
																<tr>
																	<th>User ID</th>
																	<th>User Name</th>
																	<th>Create Time</th>
																	<th>Auth</th>
																</tr>
															</thead>
															<c:forEach items="${m.users}" var="u">
															<tr>
																<tr>
																	<td>${u.id }</td>
																	<td>${u.name }</td>
																	<td><fmt:formatDate value="${u.createTime}" type="both" /></td>
																	<td>${u.auth==1?'View':'Edit' }</td>
																<tr>
															</tr>
															</c:forEach>
														</table>
												</td>
											</tr>
											
										</c:forEach>
									</tbody>
								</table>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<!-- modal -->
<div class="modal fade" id="myModal" tabindex="-1" role="dialog"
	aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title col-lg-3 col-sm-3" id="modalName"></h4>
			</div>
			<form id="modalAction" action="#" method="post">
				<div class="modal-body" id="datas"></div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					<button type="submit" class="btn btn-primary" onclick="modalSubmit();">Submit</button>
				</div>
			</form>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>
<!-- /.modal -->
<div id="group" hidden="hidden">
	<table class='table table-striped'>
		<tr>
			<td>GroupNo：</td>
			<td><input type='text' class='form-control' name='id'
				id="groupId" readonly /></td>
		</tr>
		<tr>
			<td>GroupName：</td>
			<td><input type='text' class='form-control' id="groupName"
				required name='name' onblur="checkName('/group/nameDiff',this)" /></td>
		</tr>
		<tr>
			<td>Description：</td>
			<td><textarea class='form-control' id="groupDescription"
					name='description' /></textarea></td>
		</tr>
	</table>
</div>
<script type="text/javascript">
	var actionName;
	var act;
	function modalSubmit() {
		var form = document.getElementById("modalAction");
		form.action = "/" + shift(actionName) + "/" + shift(act);
	}

	function checkName(url, node) {
		var na = node.value;
		$.get(url, {
			"name" : na
		}, function(data, status) {
			if (data == 'false') {
				node.setCustomValidity("name has been in db ！");
			} else {
				node.setCustomValidity("");
			}
		});
	}
	function shift(m) {
		if (m == "Delete") {
			return "del";
		}
		if (m == "Add") {
			return "add";
		}
		if (m == "Edit") {
			return "modify";
		}
		if (m == "User") {
			return "user";
		}
		if (m == "Group") {
			return "group";
		}
	}
	function edit(m, n, l) {
		var tr = m.parentNode.parentNode;
		var modalName, modalContent;
		actionName = n;
		act = l;
		if (n == "User") {
			$("#modalName").html(n + l);
			$("#datas").html($("#user").html());
			$("#userId").val(tr.cells[0].innerText);
			$("#userName").val(tr.cells[1].innerText);
			$("#userPassword").val(tr.cells[2].innerText);
			$("#userMail").val(tr.cells[3].innerText);
			var t = tr.cells[4].innerText;
			var d = (t == "Admin" ? 1 : 3);
			$("#userType option[value='" + d + "'")
					.attr('selected', 'selected');
		} else if (n == "Group") {
			$("#modalName").html(n + l);
			$("#datas").html($("#group").html());
			$("#groupId").val(tr.cells[0].innerText);
			$("#groupName").val(tr.cells[1].innerText);
			$("#groupDescription").val(tr.cells[2].innerText);
		}
	}
	function add(n) {
		actionName = n;
		act = "Add";
		$("#userType option[value='3'").attr('selected', 'selected');
		$("#modalName").html(n + "Add");
		$("#datas").html($("#" + shift(n)).html());
	}
</script>
<%@include file="/footer.jsp"%>
</body>
</html>
