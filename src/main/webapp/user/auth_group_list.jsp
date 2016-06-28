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
			<c:forEach items="${groups}" var="g">
				<div class="row">
					<div class="panel panel-default">
						<div class="box-header well" data-toggle="collapse"
							data-target="#collapseUser${g.id}">
							<h2 class="glyphicon glyphicon-tower">
								<span> ${g.name}</span>
							</h2>
						</div>
						<div id="collapseUser${g.id}" class="panel-collapse collapse in">
							<div class="panel-body">
								<div class="row">
									<button type="button" data-toggle="modal"
										onclick="addUser(${g.id});"
										class="btn btn-link  col-lg-offset-9 col-sm-offset-9"
										data-target="#myModal">添加</button>
								</div>
								<div class="row">
									<table class="table">
										<thead>
											<tr>
												<th>id</th>
												<th>帐号</th>
												<th>邮箱</th>
												<th>类型</th>
												<th>可创建task</th>
												<th>操作</th>
											</tr>
										</thead>
										<tbody id="tbody${g.id}">
											<c:forEach items="${userGroups[g.id]}" var="ug">
												<tr>
													<td>${users[ug.userId].id}</td>
													<td>${users[ug.userId].name}</td>
													<td hidden="hidden">${users[ug.userId].password}</td>
													<td>${users[ug.userId].mail}</td>
													<td>${ug.isLeader==1?"负责人":"成员"}</td>
													<td>${ug.canAdd==1?"是":"否"}</td>
													<td><button type="button" class="btn btn-link" 
														onclick="authUser(${ug.userId},'${users[ug.userId].name}',${ug.groupId});">授权</button>
														<button type="button" class="btn btn-link" 
															onclick="delUser(this,${ug.id});">删除</button></td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
								</div>
							</div>
						</div>
					</div>
				</div>
			</c:forEach>
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
					<button type="submit" class="btn btn-primary" onclick="commit();">提交</button>
				</div>
			</form>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>
<!-- /.modal -->
<div id="addUserGroup" hidden="hidden">
	<table class='table table-striped'>
		<tr>
			<td>组号：</td>
			<td><input type='text' class='form-control' name='groupId'
				id="addGroupId" readonly /></td>
		</tr>
		<tr>
			<td>用户：</td>
			<td><select class='form-control' id="addUserName" required
				name='userId'>
					<option>选择用户</option>
			</select></td>
		</tr>
		<tr>
			<td>负责人：</td>
			<td><input type="checkbox" id="addIsLeader" name='isLeader'
				value="1" /></td>
		</tr>
		<tr>
			<td>任务添加权限：</td>
			<td><input type="checkbox" id="addCanAdd" name='canAdd'
				value="1" /></td>
		</tr>
	</table>
</div>
<!-- /.modal -->
<script type="text/javascript">
//添加userGroup
	function addUser(m){
		$("#modalName").html("为组添加组员");
		$("#datas").html($("#addUserGroup").html());
		$("#addGroupId").val(m);
		var haveUser;
		var childs = $("#tbody"+m).children();
		for(var i=0;i<childs.length;i++){
			haveUser=haveUser+","+childs[i].cells[0].innerText;
		}
		haveUser = haveUser + ",";
		var users = "${users}".split("User");
		for(var j=1;j<users.length;j++){
			var userId = users[j].match(/id=(\d+)/g)[0].substr(3);
			var userName = users[j].match(/name=[^,]+/g)[0].substr(5);
			if(haveUser.indexOf(","+userId+",")==-1){
				$("#addUserName").append("<option value='"+userId+"'>"+userName+"<option>");		
			}
		}
	}
	function commit(){
		var form = document.getElementById("modalAction");
		form.action = "/auth/addUserGroup";
	}
	
	function delUser(m,n){
		url = "/auth/delUserGroup";
		$.get(url, {
			"id" : n
		}, function(data, status) {
			if (data == 'false') {
				alert("删除失败！");
			} else {
				alert("删除成功！");
				var tr = m.parentNode.parentNode;
				 tr.parentNode.deleteRow(tr.rowIndex-1);
			}
		});
	}
	
	function authUser(u,un,g){
		un = encodeURI(un);
		url = "/auth/authUser?userId="+u+"&userName="+un+"&groupId="+g;
		location.href = url;
	}
</script>
<%@include file="/footer.jsp"%>
</body>
</html>
