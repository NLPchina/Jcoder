
<!DOCTYPE html>
<html lang="en">
<head>

<meta charset="utf-8">
<title>Jcoder Dynamic Code Platform</title>
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="author" content="Jcoder">

<link id="bs-css" href="css/bootstrap-cerulean.min.css" rel="stylesheet">
<link href="css/charisma-app.css" rel="stylesheet">
<link href='bower_components/fullcalendar/dist/fullcalendar.css'
	rel='stylesheet'>
<link href='bower_components/fullcalendar/dist/fullcalendar.print.css'
	rel='stylesheet' media='print'>
<link href='bower_components/chosen/chosen.min.css' rel='stylesheet'>
<link href='bower_components/colorbox/example3/colorbox.css'
	rel='stylesheet'>
<link href='bower_components/responsive-tables/responsive-tables.css'
	rel='stylesheet'>
<link
	href='bower_components/bootstrap-tour/build/css/bootstrap-tour.min.css'
	rel='stylesheet'>
<link href='css/jquery.noty.css' rel='stylesheet'>
<link href='css/noty_theme_default.css' rel='stylesheet'>
<link href='css/elfinder.min.css' rel='stylesheet'>
<link href='css/elfinder.theme.css' rel='stylesheet'>
<link href='css/jquery.iphone.toggle.css' rel='stylesheet'>
<link href='css/uploadify.css' rel='stylesheet'>
<link href='css/animate.min.css' rel='stylesheet'>

<script src="bower_components/jquery/jquery.min.js"></script>

<!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

<link rel="shortcut icon" href="img/favicon.ico">
</head>
<body>
	<div class="ch-container">
		<div class="row">
			<div class="row">
				<div class="col-md-12 center login-header">
					<h2>Jcoder Dynamic Code Platform</h2>
				</div>

			</div>
			<div class="row">
				<div class="well col-md-5 center login-box">
					<div class="alert alert-info" id="message">Please login with your Username and Password.</div>
					
					<form class="form-horizontal" action="javascript:login();"  method="post">	
						<fieldset>
							<div class="input-group input-group-lg">
								<span class="input-group-addon"><i class="glyphicon glyphicon-user red"></i></span> 
								<input type="text" id="name" name="name"  class="form-control"  placeholder="Username">
							</div>
							<div class="clearfix"></div>
							<br>
							<div class="input-group input-group-lg">
								<span class="input-group-addon"><i class="glyphicon glyphicon-lock red"></i></span> 
								<input type="password" id="password" name="password" class="form-control" placeholder="Password">
							</div>
							<div class="clearfix"></div>
							<br>
							<div class="input-group input-group-lg">
								<span class="input-group-addon">
								<img alt="refulush" src="/verification_code.jpg" id="vcode" onclick="reflush()" /></span> 
								<input type="text" class="form-control" id="verification_code" name="verification_code" style="height: 79px;" placeholder="Verification Code">
							</div>
							<div class="clearfix"></div>
							<p class="center col-md-5">
								<button type="submit" class="btn btn-primary">Login</button>
							</p>
						</fieldset>
					</form>
				</div>

			</div>
		</div>
	</div>

	<script src="bower_components/bootstrap/dist/js/bootstrap.min.js"></script>

	<script src="js/jquery.cookie.js"></script>

	<script src='bower_components/moment/min/moment.min.js'></script>
	<script src='bower_components/fullcalendar/dist/fullcalendar.min.js'></script>

	<script src='js/jquery.dataTables.min.js'></script>

	<script src="bower_components/chosen/chosen.jquery.min.js"></script>

	<script src="bower_components/colorbox/jquery.colorbox-min.js"></script>

	<script src="js/jquery.noty.js"></script>

	<script src="bower_components/responsive-tables/responsive-tables.js"></script>

	<script
		src="bower_components/bootstrap-tour/build/js/bootstrap-tour.min.js"></script>

	<script src="js/jquery.raty.min.js"></script>

	<script src="js/jquery.iphone.toggle.js"></script>

	<script src="js/jquery.autogrow-textarea.js"></script>

	<script src="js/jquery.uploadify-3.1.min.js"></script>

	<script src="js/jquery.history.js"></script>

	<script src="js/charisma.js"></script>
	
	<script type="text/javascript">
		
		function login() {
			var uName = $("#name").val();
			var dataUrl = "/login";
			$.ajax({
				'url' : dataUrl,
				'dataType' : 'json',
				'type' : 'POST',
				'data' : {
					name : uName,
					password: $("#password").val(),
					verification_code: $("#verification_code").val()
				},
				'beforeSend' : function(XMLHttpRequest) {
				},
				'error' : function(e) {
					$('#message').addClass("alert alert-danger");
					$('#message').text($.parseJSON(e.responseText).message) 
					reflush();
				},
				'success' : function(data) {
					if(data.ok){
						location.href ="/thread/list";	
					}else{
						reflush();
						$('#message').addClass("alert alert-danger");
						$('#message').text(data.message) ;				
					}
				}
			});
		}
		
		
		function reflush(){
			document.getElementById('vcode').src='/verification_code.jpg' ;
		}
	</script>
</body>
</html>
