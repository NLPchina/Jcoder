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
							<span> 组管理 </span>
						</h2>
					</div>
					<div id="collapseGroup" class="panel-collapse collapse in">
						<div class="panel-body">
							<div class="row">
								<button type="button" data-toggle="modal" onclick="add('组');"
									class="btn btn-link  col-lg-offset-9 col-sm-offset-9"
									data-target="#myModal">添加</button>
							</div>
							<div>
								<table style="width: 100%" border="1" class="table-striped">
									<thead>
										<tr>
											<th>组号</th>
											<th>组名</th>
											<th>描述</th>
											<th>创建时间</th>
											<th>操作</th>
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
														onclick="edit(this,'组','修改');" class="btn btn-link"
														data-target="#myModal">编辑</button>
													<button type="button" data-toggle="modal"
														onclick="edit(this,'组','删除');" class="btn btn-link"
														data-target="#myModal">删除</button></td>
											</tr>
											<tr>
												<td colspan="6">
														<table class="table " >
															<thead>
																<tr>
																	<th>用户ID</th>
																	<th>用户名</th>
																	<th>创建时间</th>
																	<th>权限</th>
																</tr>
															</thead>
															<c:forEach items="${m.users}" var="u">
															<tr>
																<tr>
																	<td>${u.id }</td>
																	<td>${u.name }</td>
																	<td><fmt:formatDate value="${u.createTime}" type="both" /></td>
																	<td>${u.auth==1?'查看':'编辑' }</td>
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
					<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
					<button type="submit" class="btn btn-primary"
						onclick="modalSubmit();">提交</button>
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
			<td>组号：</td>
			<td><input type='text' class='form-control' name='id'
				id="groupId" readonly /></td>
		</tr>
		<tr>
			<td>组名：</td>
			<td><input type='text' class='form-control' id="groupName"
				required name='name' onblur="checkName('/group/nameDiff',this)" /></td>
		</tr>
		<tr>
			<td>描述：</td>
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
				node.setCustomValidity("名字已被占用！");
			} else {
				node.setCustomValidity("");
			}
		});
	}
	function shift(m) {
		if (m == "删除") {
			return "del";
		}
		if (m == "添加") {
			return "add";
		}
		if (m == "修改") {
			return "modify";
		}
		if (m == "用户") {
			return "user";
		}
		if (m == "组") {
			return "group";
		}
	}
	function edit(m, n, l) {
		var tr = m.parentNode.parentNode;
		var modalName, modalContent;
		actionName = n;
		act = l;
		if (n == "用户") {
			$("#modalName").html(n + l);
			$("#datas").html($("#user").html());
			$("#userId").val(tr.cells[0].innerText);
			$("#userName").val(tr.cells[1].innerText);
			$("#userPassword").val(tr.cells[2].innerText);
			$("#userMail").val(tr.cells[3].innerText);
			var t = tr.cells[4].innerText;
			var d = (t == "超级用户" ? 1 : 3);
			$("#userType option[value='" + d + "'")
					.attr('selected', 'selected');
		} else if (n == "组") {
			$("#modalName").html(n + l);
			$("#datas").html($("#group").html());
			$("#groupId").val(tr.cells[0].innerText);
			$("#groupName").val(tr.cells[1].innerText);
			$("#groupDescription").val(tr.cells[2].innerText);
		}
	}
	function add(n) {
		actionName = n;
		act = "添加";
		$("#userType option[value='3'").attr('selected', 'selected');
		$("#modalName").html(n + "添加");
		$("#datas").html($("#" + shift(n)).html());
	}
</script>
<%@include file="/footer.jsp"%>
</body>
</html>
