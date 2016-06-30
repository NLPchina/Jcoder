<%@page language="java" pageEncoding="UTF-8"%>
<%@include file="/common/taglibs.jsp"%>
<%@include file="common/common.jsp"%>

<!DOCTYPE html>
<html lang="cn">
<%@include file="header.jsp"%>
<!-- topbar ends -->
<div class="row">
	<!-- left menu starts -->
	<%@include file="left.jsp"%>
	<!-- left menu ends -->
	<div id="content" class="col-lg-10 col-sm-10">
		<div class="row">
			<div class="box col-md-12">
				<div class="box-inner">
					<div class="box-header well" data-original-title="">
						<h2>
							<i class="glyphicon glyphicon-user"></i> Thread Manager
						</h2>
					</div>
					<div class="box-content">
						<table class="table table-striped table-bordered bootstrap-datatable datatable responsive">
							<thead>
								<tr>
									<th>Name</th>
									<th>Group</th>
									<th>Description</th>
									<th>Status</th>
									<th>Success</th>
									<th>Err</th>
									<th>Info</th>
									<th>Edit</th>
								</tr>
							</thead>
							<tbody>
									<c:forEach var="task" items="${obj.threads }">
									<c:if test="${userType==1 || AUTH_MAP[task.groupId]==2}">
									<tr>
										<td>${task.name }</td>
										<td>${task.groupId }</td>
										<td class="center">${task.description }</td>
										<td class="center">
											${task.runStatus}
										</td>
										<td class="center">
											<span class="label-success label label-default">${task.success}　times</span>
										</td>
										<td class="center">
											<span class="label-success label label-danger">${task.error}　times</span>
										</td>
										<td class="center">${task.message}</td>
										<td class="center">
											<a class="btn btn-info" href="${ctx }/task/editor/${task.groupId }/${task.id}"> <i class="glyphicon glyphicon-edit icon-white"></i> Edit </a>
											<a class="btn btn-danger" href="${ctx }/thread/stop/${task.name}"> <i class="glyphicon glyphicon-trash icon-white"></i> Stop </a>
										</td>
									</tr>
									</c:if>
									</c:forEach>
							</tbody>
						</table>
						<c:if test="${obj.actions.size()>0 }">
						<table class="table table-striped table-bordered bootstrap-datatable datatable responsive">
							<thead>
								<tr>
									<th>Name</th>
									<th>Group</th>
									<th>Description</th>
									<th>Status</th>
									<th>Success</th>
									<th>Err</th>
									<th>Info</th>
									<th>Edit</th>
								</tr>
							</thead>
							<tbody>
									<c:forEach var="task" items="${obj.actions }">
									<c:if test="${userType==1 || AUTH_MAP[task.groupId]==2}">
									<tr>
										<td>${task.name }</td>
										<td>${task.groupId }</td>
										<td class="center">${task.description }</td>
										<td class="center">
											${task.runStatus}
										</td>
										<td class="center">
											<span class="label-success label label-default">${task.success}　times</span>
										</td>
										<td class="center">
											<span class="label-success label label-danger">${task.error}　times</span>
										</td>
										<td class="center">${task.message}</td>
										<td class="center">
											<a class="btn btn-info" href="${ctx }/task/editor/${task.groupId }/${task.id}"> <i class="glyphicon glyphicon-edit icon-white"></i> Edit </a> 
											<a class="btn btn-danger" href="${ctx }/thread/stop/${task.name}"> <i class="glyphicon glyphicon-trash icon-white"></i> Stop </a>
										</td>
									</tr>
									</c:if>
									</c:forEach>
							</tbody>
						</table>
						</c:if>
						<c:if test="${obj.schedulers.size()>0 }">
						<table class="table table-striped table-bordered bootstrap-datatable datatable responsive">
							<thead>
								<tr>
									<th>名称</th>
									<th>分组</th>
									<th>简介</th>
									<th>激活</th>
									<th>状态</th>
									<th>信息</th>
									<th>操作</th>
								</tr>
							</thead>
							<tbody>
									<c:forEach var="task" items="${obj.schedulers }">
									<c:if test="${userType==1 || AUTH_MAP[task.groupId]==2}">
									<tr>
										<td>${task.name }</td>
										<td>${task.groupId }</td>
										<td class="center">${task.description }</td>
										<td class="center">${task.status}</td>
										<td class="center">${task.runStatus}</td>
										<td class="center">${task.message}</td>
										<td class="center">
											<a class="btn btn-info" href="${ctx }/task/editor/${task.groupId }/${task.name}"> <i class="glyphicon glyphicon-edit icon-white"></i> Edit </a> 
										</td>
									</tr>
									</c:if>
									</c:forEach>
							</tbody>
						</table>
						</c:if>
					</div>
				</div>
			</div>
			<!--/span-->
		</div>
		<!--/row-->
	</div>

	<%@include file="footer.jsp"%>
	</body>
</html>
