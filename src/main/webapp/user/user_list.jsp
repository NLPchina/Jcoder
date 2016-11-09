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
							<span> UserManager </span>
						</h2>
					</div>
					<div id="collapseUser" class="panel-collapse collapse in">
						<div class="panel-body">
							<div class="row">
								<button type="button" data-toggle="modal" onclick="add('User');" class="btn btn-link  col-lg-offset-9 col-sm-offset-9" data-target="#myModal">Add</button>
							</div>
							<div class="row">
								<table class="table">
									<thead>
										<tr>
											<th>No</th>
											<th>Account</th>
											<th>Mail</th>
											<th>Type</th>
											<th>CreateTime</th>
											<th>Edit</th>
										</tr>
									</thead>
									<tbody>
										<c:forEach items="${users }" var="m">
											<tr>
												<td>${m.id}</td>
												<td>${m.name}</td>
												<td hidden="hidden">${m.password}</td>
												<td>${m.mail}</td>
												<td>${m.type==1?"Admin":m.type==2?"Group Master":"Member"}</td>
												<td><fmt:formatDate value="${m.createTime}" type="both" /></td>
												<td><button type="button" class="btn btn-link" onclick="editUser(this);" data-target="#myModal" data-toggle="modal">Edit</button>
													<button type="button" class="btn btn-link" onclick="editAuth(${m.id});" data-target="#myModal" data-toggle="modal">Auth</button>
													<button type="button" class="btn btn-link" onclick="deleteUser(this);" data-target="#myModal" data-toggle="modal">Delete</button></td>
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
					<button type="button" class="btn btn-default" >Close</button>
					<button type="submit" id="submit_user" class="btn btn-primary" onclick="modalSubmit();" style="display: block">Submit</button>
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
			<td>No：</td>
			<td><input type='text' class='form-control' name='id'
				id="userId" readonly /></td>
		</tr>
		<tr>
			<td>Åccount：</td>
			<td><input type='text' class='form-control' name='name' required onblur="checkName('/user/nameDiff',this)" id='userName' /></td>
		</tr>
		<tr>
			<td>Password：</td>
			<td><input type="password" class='form-control' id="userPassword" required name='password' /></td>
		</tr>
		<tr>
			<td>Email：</td>
			<td><input type='email' class='form-control' id="userMail" required name='mail' /></td>
		</tr>
		<tr>
			<td>Type：</td>
			<td><select id="userType" name='type'>
					<option value="3">Member</option>
					<!-- <option value="2">组长</option> -->
					<option value="1">Admin</option>
			</select></td>
		</tr>
	</table>
</div>

<div id="auth" hidden="hidden">
	<table class='table table-striped'>
		<tr>
			<td>Group Name</td><td>View</td><td>Edit</td>				
		</tr>
		<c:forEach items="${groups }" var="group">
		<tr>
			<td>${group.name}</td>
			<td>
				<input type='hidden' class='form-control' name='id' id="tempUserId" readonly />
				<select name="auth" id="auth_${group.id }" onchange="checkedchange(${group.id},this.value)" style="width: 98%">
					<option value="0">None</option>
					<option value="1">View</option>
					<option value="2">Editor</option>
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
				node.setCustomValidity("the use name has been created！");
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
	
	function checkedchange(groupId,auth){
		url = "/auth/updateUserGroup";
		var userId = $('#tempUserId').val() ;
		$.get(url, {
			"groupId" : groupId,
			"auth":auth,
			"userId":userId
		}, function(data, status) {
			if(status!='success'){
				alert("Auth  ERR  ！Please try again");
			}
		});
	}
	
	function editUser(m) {
		actionName = 'User';
		act = "Edit";
		$("#submit_user").css('display', '');
		var tr = m.parentNode.parentNode;
		$("#modalName").html('User Manager');
		$("#datas").html($("#user").html());
		$("#userId").val(tr.cells[0].innerText);
		$("#userName").val(tr.cells[1].innerText);
		$("#userPassword").val(tr.cells[2].innerText);
		$("#userMail").val(tr.cells[3].innerText);
		var t = tr.cells[4].innerText;
		var d = (t == "Admin" ? 1 : 3);
		$("#userType option[value='" + d + "'").attr('selected', 'selected');
	}
	
	function editAuth(userId,auth){
		$("#submit_user").css('display', 'none');
		url = "${ctx}/auth/authUser";
		$.getJSON(url, {
			"userId":userId
		}, function(data, status) {
			if(status!='success'){
				alert("Auth  ERR  ！Please try again");
			}else{
				$("#modalName").html('AuthManager');
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
		actionName = 'User';
		act = "Delete";
		$("#submit_user").css('display', '');
		var tr = m.parentNode.parentNode;
		$("#modalName").html('User Manager');
		$("#datas").html($("#user").html());
		$("#userId").val(tr.cells[0].innerText);
		$("#userName").val(tr.cells[1].innerText);
		$("#userPassword").val(tr.cells[2].innerText);
		$("#userMail").val(tr.cells[3].innerText);
		var t = tr.cells[4].innerText;
		var d = (t == "Admin" ? 1 : 3);
		$("#userType option[value='" + d + "'").attr('selected', 'selected');
	}
	
	function add(n) {
		actionName = n;
		act = "Add";
		$("#submit_user").css('display', '');
		$("#userType option[value='3']").attr('selected', 'selected');
		$("#modalName").html(n + "Add");
		$("#datas").html($("#" + shift(n)).html());
	}
</script>
<%@include file="/footer.jsp"%>
</body>
</html>
