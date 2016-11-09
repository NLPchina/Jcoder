<%@page language="java" pageEncoding="UTF-8"%>
<%@include file="/common/taglibs.jsp"%>
<%@include file="common/common.jsp"%>

<!DOCTYPE html>
<html lang="cn">
<%@include file="header.jsp"%>
<link rel="stylesheet" type="text/css" href="${ctx }/webuploader/webuploader.css">
<script type="text/javascript" src="${ctx }/webuploader/webuploader.js"></script>

<!-- topbar ends -->
<div class="row">
	<!-- left menu starts -->
	<%@include file="left.jsp"%>
	<!-- left menu ends -->
	<div id="content" class="col-lg-10 col-sm-10">
		<c:forEach var="entry" items="${obj }">
		<div class="row">
			<div class="box col-md-12">
				<div class="box-inner">
					<div class="box-header well" data-original-title="">
						<h2>
							<i class="glyphicon glyphicon-user"></i>&nbsp;${entry.key }
						</h2>
						
						<div class="box-icon">
							<c:if test="${entry.key=='File' }">
								<a href="#"  id="update_begin" class="btn btn-round btn-default btn-success"><i class="glyphicon glyphicon-circle-arrow-up"></i></a>
							</c:if>
							<c:if test="${entry.key=='Maven' }">
								<a href="${ctx }/jar/maven" class="btn btn-round btn-default btn-success"><i class="glyphicon glyphicon-pencil"></i></a>
							</c:if>
		                </div>
					</div>
					<div class="box-content">
						<table class="table table-striped table-bordered">
							<thead>
								<tr>
									<th>Status</th>
									<th>Name</th>
									<th>Size</th>
									<th>Time</th>
									<th>Path</th>
									<c:if test="${entry.key=='File' }">
									<th>Edit</th>
									</c:if>
								</tr>
							</thead>
							<tbody>
									<c:forEach var="info" items="${entry.value}">
									<c:if test="${userType==1 || AUTH_MAP[task.groupId]==2}">
									<tr>
										<td class="center ">
											<c:if test="${info.status==0 }">
												<span class="label-success label label-default">Ok</span>
											</c:if>
											<c:if test="${info.status==1 }">
												<span class="label-default label">Unload</span>
											</c:if>
											<c:if test="${info.status==2 }">
												<span class="label-warning label label-default">Unrelease</span>
											</c:if>
											
										</td>
										<td>${info.file.name }</td>
										<td><fmt:formatNumber value="${info.file.length()/1024 }" type="currency" pattern="#0.00k"/></td>
										<td class="center">
											<fmt:formatDate value="${info.date }" type="both"/>
										</td>
										<td class="center"><input type="text" value="${info.file.path}" style="width: 100%; height: 100%"/></td>
										<c:if test="${entry.key=='File' }">
										<td class="center">
											<a class="btn btn-danger" href="${ctx }/jar/remove?path=${info.encodingPath}"><i class="glyphicon glyphicon-trash icon-white"></i> Delete </a>
										</td>
										</c:if>
									</tr>
									</c:if>
									</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<!--/span-->
		</div>
		<!--/row-->
		</c:forEach>
	</div>
	
	<div class="modal fade" id="myModal" tabindex="-1" >
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>Upload</h3>
				</div>
				<div class="modal-body">
					<div id="update_jar">upload Jars</div>
					<div id="update_jar_info"></div>
				</div>
				<div class="modal-footer">
					<a href="${ctx }/jar/list" class="btn btn-primary" >Ok</a>
				</div>
			</div>
		</div>
	</div>

	<%@include file="footer.jsp"%>
	
	
	<script type="text/javascript" src="${ctx }/webuploader/myupload.js"></script>
	<script type="text/javascript">
		var flag = true;
		$('#update_begin').click(function() {
			$('#dialog_message').text("shang");
			$('#myModal').modal('show');
			if(flag){
				flag = false ;
				for(var start = Date.now(); Date.now() - start <= 1000; ) { }
				createUploader("update_jar","${ctx}/jar/upload");
			}
		});
		
	</script>
	
	</body>
</html>
