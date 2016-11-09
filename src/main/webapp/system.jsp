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
		<table>
			<thead>
				<tr>
					<th>
						Backup or Restore :
					<th>
					<th>
						<a class="btn btn-success" href="${ctx }/system/export_task"><i class="glyphicon glyphicon-circle-arrow-up"></i>ExportTask</a>
					</th>
					<th>
						<a class="btn btn-danger" href="javascript:$('#import_file').click()"><i class="glyphicon glyphicon-circle-arrow-down"></i>ImportTask</a>
					</th>
				</tr>
			</thead>
		</table>
		<form action="${ctx }/system/import_task" method="post" enctype ="multipart/form-data" id="file_up">
			<input type="file" name="file" style="display: none;" id="import_file" onchange="$('#file_up').submit()" >
		</form>
		<c:if test="${obj!=null }">
			<textarea rows="8" style="width: 100%">${obj }</textarea>
		</c:if>
		<!--/row-->
		
	
		<div class="row">
			<div class="box col-md-12">
				<div class="box-inner">
					<div class="box-header well" data-original-title="">
						<h2>
							<i class="glyphicon glyphicon-user"></i>&nbsp;System Peroperties
						</h2>
						
						<div class="box-icon">
							<a href="#" class="btn btn-minimize btn-round btn-default"><i class="glyphicon glyphicon-chevron-up"></i></a>
							<a href="#" class="btn btn-close btn-round btn-default"><i class="glyphicon glyphicon-remove"></i></a>
						</div>
					</div>
					<div class="box-content">
						<table class="table table-striped table-bordered bootstrap-datatable datatable responsive">
							<thead>
								<tr>
									<th>Name</th>
									<th class="col-md-10">Value</th>
								</tr>
							</thead>
							<tbody>
									<tr>
										<td>jcoder_version</td>
										<td>${VERSION }</td>
									</tr>
									<c:forEach var="kv" items="${properties}">
									<tr>
										<td>${kv.key }</td>
										<td><input type="text" style="width: 100%" value="${kv.value }" ></td>
									</tr>
									</c:forEach>
							</tbody>
						</table>
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
