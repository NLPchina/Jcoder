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
			<div class="panel panel-default">
				<div class="panel-heading">用户修改</div>
				<div class="panel-body">
					${user.name }
				</div>
			</div>
		</div>
	</div>
</div>
<%@include file="/footer.jsp"%>
</body>
</html>
