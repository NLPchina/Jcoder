<%@page language="java" pageEncoding="UTF-8"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

			<div class="row">
				<div class="box col-md-12">
					<div class="box-inner">
						<div class="box-header well" data-original-title="">
							<h2>
								<i class="glyphicon glyphicon-eye-open "></i> Console
							</h2>
							

							<div class="box-icon">
								<a class="btn btn-round btn-default btn-success" id="socket_connected"><i class="glyphicon glyphicon-plane"></i> </a> 
								<a class="btn btn-round btn-default" id="console_clear"><i class="glyphicon glyphicon-trash"></i> </a> 
								<c:if test="${console_task }">
								<a class="btn btn-round btn-default btn-danger" id="script_stop"><i class="glyphicon glyphicon-stop"></i> </a> 
								<a class="btn btn-round btn-default btn-success" id="script_start"><i class="glyphicon glyphicon-play"></i> </a>
								</c:if> 
								<a class="btn btn-minimize btn-round btn-default"><i class="glyphicon glyphicon-chevron-up"></i></a>
							</div>
						</div>
						<div class="box-content">
							<div id="console" style="line-height:120% ; width:100%; height:${console_height==null?'800px':console_height}; white-space:nowrap; overflow:auto; border:1px solid #000000; background:#000000; color: #FFF; padding: 5px;" ></div>
						</div>
						
						<div class="box-content form-inline">
							<div class="input-group col-md-6" style="width: 49%">
								<span class="input-group-addon"><i class="glyphicon glyphicon-search red"></i></span>
								<input type="text" id="console_contains" class="form-control" placeholder="包含" value="${console_contains }">
							</div>
							<div class="input-group col-md-6" style="width: 50%">
								<span class="input-group-addon"><i class="glyphicon glyphicon-glass red"></i></span>
								<input type="text" id="console_filter" class="form-control" placeholder="不包含" value="${console_filter }">
							</div>
						</div>
				</div>
				<!--/span-->
			</div>
		</div>
	<!-- websocket -->
	<script src="${ctx }/js/atmosphere-min.js"></script>
	<script src="${ctx }/js/console_socket.js"></script>
	
