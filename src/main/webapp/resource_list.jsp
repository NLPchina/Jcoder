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
		<div class="row">
			<div class="box col-md-12">
				<div class="box-inner">
					<div class="box-header well" data-original-title="">
						<h2>
							<i class="glyphicon glyphicon-user"></i>&nbsp;Resource
						</h2>
						
						<div class="box-icon">
							<a href="#"  id="create_begin" class="btn btn-round btn-default btn-success"><i class="glyphicon glyphicon-folder-open"></i></a>
							<a href="#"  id="update_begin" class="btn btn-round btn-default btn-success"><i class="glyphicon glyphicon-circle-arrow-up"></i></a>
		                </div>
					</div>
					<div class="box-content">
						<table class="table table-striped table-bordered">
							<thead>
								<tr>
									<th>Name</th>
									<th>Size</th>
									<th>Modified time</th>
									<th>Path</th>
									<th>Operation</th>
								</tr>
							</thead>
							<tbody>
									<c:forEach var="info" items="${obj}">
									<tr>
										<td>
											<c:if test="${info.file.directory }">
												<i class="glyphicon blue glyphicon-inbox"></i> <a href="${ctx }/resource/list?path=${info.encodingPath}">${info.name }</a>
											</c:if>
											<c:if test="${!info.file.directory }">
												<i class="glyphicon blue glyphicon-file"></i> <a href="${ctx }/resource/down?path=${info.encodingPath}">${info.name }</a>
											</c:if>
										</td>
										<td><fmt:formatNumber value="${info.file.length()/1024 }" type="currency" pattern="#0.00k"/></td>
										<td class="center">
											<fmt:formatDate value="${info.date }" type="both"/>
										</td>
										<td class="center"><input type="text" value="${info.file.path}" style="width: 100%; height: 100%"/></td>
										<td class="center">
											<a class="btn btn-danger" href="${ctx }/resource/remove?path=${info.encodingPath}"><i class="glyphicon glyphicon-trash icon-white"></i> Delete </a>
										</td>
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
	
	<div class="modal fade" id="myModal" tabindex="-1" >
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>Upload</h3>
				</div>
				<div class="modal-body">
					<div id="update_file">Upload File</div>
					<div id="update_file_info"></div>
				</div>
				<div class="modal-footer">
					<a href="javascript:location.reload(true)" class="btn btn-primary" >确定</a>
				</div>
			</div>
		</div>
	</div>
	
	<div class="modal fade" id="createFile" tabindex="-1" >
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">×</button>
					<h3>Create Dir</h3>
				</div>
				<div class="modal-body">
					<div id="update_file">Input Dir Name: <input type="text" class="form-control" placeholder="input folder name" id="createFileName"/></div>
				</div>
				<div class="modal-footer">
					<a id="create_folder_btn" href="#" class="btn btn-primary" >确定</a>
				</div>
			</div>
		</div>
	</div>

	<%@include file="footer.jsp"%>
	
	
	<script type="text/javascript" src="${ctx }/webuploader/myupload.js"></script>
	<script type="text/javascript">
	

		function getUrlParam(name) {
			var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
			var r = window.location.search.substr(1).match(reg);  //匹配目标参数
			if (r != null) return unescape(r[2]); return null; //返回参数值
       }
		
		var flag = true;
		$('#update_begin').click(function() {
			$('#myModal').modal('show');
			if(flag){
				flag = false ;
				for(var start = Date.now(); Date.now() - start <= 1000; ) { }
				
				var path = getUrlParam('path') ;
				
				if(path !=null){
					createUploader("update_file","${ctx}/resource/upload?path="+getUrlParam('path'));
				}else{
					createUploader("update_file","${ctx}/resource/upload");
				}
			}
		});
		
		$('#create_begin').click(function() {
			$('#createFile').modal('show');
		});
		
		
		$('#create_folder_btn').click(function(){
			
			var path = getUrlParam('path') ;
			
			if(path !=null){
				location.href = "${ctx}/resource/crate_folder?path="+getUrlParam('path')+"&floder="+$('#createFileName').val() ;
			}else{
				location.href = "${ctx}/resource/crate_folder?floder="+$('#createFileName').val() ;
			}
			
			
			
		})
		
	</script>
	
	</body>
</html>
