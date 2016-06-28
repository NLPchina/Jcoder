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
				<div class="box col-md-12">
					<div class="box-inner">
						<div class="box-header well  " data-original-title="">
							<h2>
								<i class="glyphicon glyphicon-user"></i> ${userName}
							</h2>
							<div class="box-icon">
								<a href="#" class="btn btn-minimize btn-round btn-default"><i
									class="glyphicon glyphicon-chevron-up"></i></a> <a href="#"
									class="btn btn-close btn-round btn-default"><i
									class="glyphicon glyphicon-remove"></i></a>
							</div>
						</div>
						<div class="box-content">
							<table
								class="table table-striped  bootstrap-datatable datatable responsive">
								<thead>
									<tr>
										<th>id</th>
										<th>task名</th>
										<th>可删除</th>
										<th>可修改</th>
										<th>可运行</th>
										<th><!-- <input type="checkbox" onclick="linesCheck(this);" /> --></th>
									</tr>
								</thead>
								<tbody id="tbody">
									<c:forEach items="${tasks}" var="t">
										<tr id="task${t.id}" >
											<td>${t.id}</td>
											<td>${t.name}</td>
											<td><input type="checkbox"
												${userTasks[t.id].canDel==1?"checked":""} value="1"  onchange="checkedchange(this);" /></td>
											<td><input type="checkbox"
												${userTasks[t.id].canUpdate==1?"checked":""} value="1"   onchange="checkedchange(this);"/></td>
											<td><input type="checkbox"
												${userTasks[t.id].canExecute==1?"checked":""} value="1"  onchange="checkedchange(this);" /></td>
											<td><input onclick="lineCheck(${t.id},this);"
												type="checkbox" 
												${userTasks[t.id].canExecute==1&&userTasks[t.id].canUpdate==1&&userTasks[t.id].canExecute==1?"checked":""} /></td>
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
<script type="text/javascript">
	function lineCheck(m,n){
		var oTast=document.getElementById('task'+m);
		var aInput=oTast.getElementsByTagName('input');
		for(var i=0;i<aInput.length;i++)
			{
				if(aInput[i]==n)continue;
		    	aInput[i].checked=n.checked;
			};
		checkedchange(n);
	}
	
	function linesCheck(n){
		var oTast=document.getElementById("tbody");
		var aInput=oTast.getElementsByTagName('input');
		for(var i=0;i<aInput.length;i++)
			{
		    	aInput[i].checked=n.checked;
			};
		
	}
	
	function checkedchange(m){
		var tr = m.parentNode.parentNode;
		var taskId = tr.cells[0].innerText;
		var name = tr.cells[1].innerText;
		var canDel = tr.cells[2].firstChild.checked==true?1:0;
		var canUpdate = tr.cells[3].firstChild.checked==true?1:0;
		var canExecute = tr.cells[4].firstChild.checked==true?1:0;
		url = "/auth/updateUserTask";
		$.get(url, {
			"taskId" : taskId,
			"userId":"${userId}",
			"canDel":canDel,
			"canUpdate":canUpdate,
			"canExecute":canExecute
		}, function(data, status) {
			if(status!='success'){
				alert("授权失败！请重新选择");
			}
		});
	}
</script>
<%@include file="/footer.jsp"%>
</body>
</html>
