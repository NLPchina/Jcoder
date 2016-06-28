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
								data-target="#collapseUser">
						<h2 class="glyphicon glyphicon-user">
							<span> 用户管理 </span>
						</h2>
					</div>
					<div id="collapseUser" class="panel-collapse collapse in">
						<div class="panel-body">
							<div class="row">
								<button type="button" data-toggle="modal" onclick="add('用户');"
									class="btn btn-link  col-lg-offset-9 col-sm-offset-9"
									data-target="#myModal">添加</button>
							</div>
							<div class="row">
								<table class="table">
									<thead>
										<tr>
											<th>序号</th>
											<th>帐号</th>
											<th>邮箱</th>
											<th>类型</th>
											<th>创建时间</th>
											<th>操作</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${users }" var="m">
											<tr>
												<td>${m.id}</td>
												<td>${m.name}</td>
												<td hidden="hidden">${m.password}</td>
												<td>${m.mail}</td>
												<td>${m.type==1?"超级用户":m.type==2?"组长":"组员"}</td>
												<td><fmt:formatDate value="${m.createTime}" type="both" /></td>
												<td><button type="button" class="btn btn-link" onclick="editUser(this);" data-target="#myModal" data-toggle="modal">编辑</button>
													<button type="button" class="btn btn-link" onclick="editAuth(${m.id});" data-target="#myModal" data-toggle="modal">权限</button>
													<button type="button" class="btn btn-link" onclick="deleteUser(this);" data-target="#myModal" data-toggle="modal">删除</button></td>
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
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title col-lg-3 col-sm-3" id="modalName"></h4>
			</div>
			<form id="modalAction" action="#" method="post">
				<div class="modal-body" id="datas"></div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" >关闭</button>
					<button type="submit" id="submit_user" class="btn btn-primary" onclick="modalSubmit();" style="display: block">提交</button>
				</div>
			</form>
		</div>
		<!-- /.modal-content -->
	</div>
	<!-- /.modal-dialog -->
</div>
<!-- /.modal -->
<div id="user" hidden="hidden">
	<table class='table table-striped'>
		<tr>
			<td>序号：</td>
			<td><input type='text' class='form-control' name='id'
				id="userId" readonly /></td>
		</tr>
		<tr>
			<td>帐号：</td>
			<td><input type='text' class='form-control' name='name' required onblur="checkName('/user/nameDiff',this)" id='userName' /></td>
		</tr>
		<tr>
			<td>密码：</td>
			<td><input type="password" class='form-control' id="userPassword" required name='password' /></td>
		</tr>
		<tr>
			<td>邮箱：</td>
			<td><input type='email' class='form-control' id="userMail" required name='mail' /></td>
		</tr>
		<tr>
			<td>类型：</td>
			<td><select id="userType" name='type'>
					<option value="3">普通用户</option>
					<!-- <option value="2">组长</option> -->
					<option value="1">超级用户</option>
			</select></td>
		</tr>
	</table>
</div>

<div id="auth" hidden="hidden">
	<table class='table table-striped'>
		<tr>
			<td>组名称</td><td>查看</td><td>编辑</td>				
		</tr>
		<c:forEach items="${groups }" var="group">
		<tr>
			<td>${group.name}</td>
			<td>
				<input type='hidden' class='form-control' name='id' id="tempUserId" readonly />
				<select name="auth" id="auth_${group.id }" onchange="checkedchange(${group.id},this.value)" style="width: 98%">
					<option value="0">无权限</option>
					<option value="1">查看权限</option>
					<option value="2">编辑权限</option>
				</select>
			</td>
		</tr>
		</c:forEach>
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
	
	function checkedchange(groupId,auth){
		url = "/auth/updateUserGroup";
		var userId = $('#tempUserId').val() ;
		$.get(url, {
			"groupId" : groupId,
			"auth":auth,
			"userId":userId
		}, function(data, status) {
			if(status!='success'){
				alert("授权失败！请重新选择");
			}
		});
	}
	
	function editUser(m) {
		actionName = '用户';
		act = "修改";
		$("#submit_user").css('display', '');
		var tr = m.parentNode.parentNode;
		$("#modalName").html('用户管理');
		$("#datas").html($("#user").html());
		$("#userId").val(tr.cells[0].innerText);
		$("#userName").val(tr.cells[1].innerText);
		$("#userPassword").val(tr.cells[2].innerText);
		$("#userMail").val(tr.cells[3].innerText);
		var t = tr.cells[4].innerText;
		var d = (t == "超级用户" ? 1 : 3);
		$("#userType option[value='" + d + "'").attr('selected', 'selected');
	}
	
	function editAuth(userId,auth){
		$("#submit_user").css('display', 'none');
		url = "${ctx}/auth/authUser";
		$.getJSON(url, {
			"userId":userId
		}, function(data, status) {
			if(status!='success'){
				alert("授权失败！请重新选择");
			}else{
				$("#modalName").html('权限管理');
				$("#datas").html($("#auth").html());
				$('#tempUserId').val(userId) ;
				$("input[name='auth']").find("option[value='0']").attr("selected",true);
				for(var index in data){
					$("#auth_"+data[index].groupId).find("option[value='"+data[index].auth+"']").attr("selected",true);
				}
			}
		});
	}
	
	function deleteUser(m){
		actionName = '用户';
		act = "删除";
		$("#submit_user").css('display', '');
		var tr = m.parentNode.parentNode;
		$("#modalName").html('用户管理');
		$("#datas").html($("#user").html());
		$("#userId").val(tr.cells[0].innerText);
		$("#userName").val(tr.cells[1].innerText);
		$("#userPassword").val(tr.cells[2].innerText);
		$("#userMail").val(tr.cells[3].innerText);
		var t = tr.cells[4].innerText;
		var d = (t == "超级用户" ? 1 : 3);
		$("#userType option[value='" + d + "'").attr('selected', 'selected');
	}
	
	function add(n) {
		actionName = n;
		act = "添加";
		$("#submit_user").css('display', '');
		$("#userType option[value='3']").attr('selected', 'selected');
		$("#modalName").html(n + "添加");
		$("#datas").html($("#" + shift(n)).html());
	}
</script>
<%@include file="/footer.jsp"%>
</body>
</html>
