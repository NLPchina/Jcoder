<%@page language="java" pageEncoding="UTF-8"%>
<%@include file="/common/taglibs.jsp"%>
<%@include file="common/common.jsp"%>

<!DOCTYPE html>
<html lang="cn">
<%@include file="header.jsp"%>
<!-- topbar ends -->
<div class="container">
	<div class="row ">
		<div class="col-sm-3 col-sm-offset-4 panel panel-default"
			style="margin-top: 10%; margin-bottom: 10%; padding-top: 10px; padding-bottom: 20px">
			<div class="col-sm-offset-4"> </div>
			<form class="form-horizontal" role="form" action="javascript:login();" >
				<div class="form-group" style="margin-top: 10px">
					<label for="name" class="col-sm-3 control-label">Account</label>
					<div class="col-sm-8">
						<input type="text" class="form-control" id="name" name="name" required
							placeholder="input account">
					</div>
				</div>
				<div class="form-group">
					<label for="password" class="col-sm-3 control-label">Password</label>
					<div class="col-sm-8">
						<input type="password" class="form-control" id="password" name="password" required
							placeholder="input password">
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<button type="reset" class="btn btn-default">Reset</button>
						&nbsp;&nbsp;&nbsp;&nbsp;
						<button type="submit" class="btn btn-default">Login</button>
					</div>
				</div>
			</form>
		</div>
	</div>
</div>
<%@include file="footer.jsp"%>
</body>
<script type="text/javascript">

function login() {
	var userName = "${userName}";
	var uName = $("#name").val();
	var dataUrl = "/login";
	$.ajax({
		'url' : dataUrl,
		'dataType' : 'json',
		'type' : 'POST',
		'data' : {
			name : uName,
			password: $("#password").val()
		},
		'beforeSend' : function(XMLHttpRequest) {
		},
		'error' : function(e) {
			alert("PassWord Or Acount Err！");
		},
		'success' : function(data) {
			if(data.ok){
				location.href ="/thread/list";	
			}else{
				alert("PassWord Or Acount Err！");				
			}
		}
	});
}
</script>
</html>