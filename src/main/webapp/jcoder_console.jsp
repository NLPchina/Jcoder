<%@page language="java" pageEncoding="UTF-8"%>
<%@include file="/common/taglibs.jsp"%>
<%@include file="/common/common.jsp"%>

<!DOCTYPE html>
<html lang="cn">
<%@include file="/header.jsp"%>


<!-- topbar ends -->
<div class="ch-container">
	<div class="row">
		<!-- left menu starts -->
		<%@include file="/left.jsp"%>
		<!-- left menu ends -->
		<c:set var="console_task" value="false" scope="request"></c:set>
		<c:set var="console_filter" value="" scope="request"></c:set>
		<c:set var="console_contains" value="" scope="request"></c:set>
		<div id="content" class="col-lg-10 col-sm-10">
		<jsp:include page="/console.jsp"></jsp:include>
		</div>
</div>
	<%@include file="/footer.jsp"%>
	</body>
</html>
