<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jstl/core_rt"%>
<head>
    <meta charset="utf-8">
    <title>Jcoder Dynamic Code Platform</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="author" content="Jcoder">

    <!-- The styles -->
    <link id="bs-css" href="${ctx }/css/bootstrap-cerulean.min.css" rel="stylesheet">

    <link href="${ctx }/css/charisma-app.css" rel="stylesheet">
    <link href='${ctx }/bower_components/fullcalendar/dist/fullcalendar.css' rel='stylesheet'>
    <link href='${ctx }/bower_components/fullcalendar/dist/fullcalendar.print.css' rel='stylesheet' media='print'>
    <link href='${ctx }/bower_components/chosen/chosen.min.css' rel='stylesheet'>
    <link href='${ctx }/bower_components/colorbox/example3/colorbox.css' rel='stylesheet'>
    <link href='${ctx }/bower_components/responsive-tables/responsive-tables.css' rel='stylesheet'>
    <link href='${ctx }/bower_components/bootstrap-tour/build/css/bootstrap-tour.min.css' rel='stylesheet'>
    <link href='${ctx }/css/jquery.noty.css' rel='stylesheet'>
    <link href='${ctx }/css/noty_theme_default.css' rel='stylesheet'>
    <link href='${ctx }/css/elfinder.min.css' rel='stylesheet'>
    <link href='${ctx }/css/elfinder.theme.css' rel='stylesheet'>
    <link href='${ctx }/css/jquery.iphone.toggle.css' rel='stylesheet'>
    <link href='${ctx }/css/uploadify.css' rel='stylesheet'>
    <link href='${ctx }/css/animate.min.css' rel='stylesheet'>

    <!-- jQuery -->
    <script src="${ctx }/bower_components/jquery/jquery.min.js"></script>

    <!-- The HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- The fav icon -->
    <link rel="shortcut icon" href="${ctx }/img/favicon.ico">

</head>



<body>
	<!-- topbar starts -->
	<div class="navbar navbar-default" role="navigation">

		<div class="navbar-inner">
			<button type="button" class="navbar-toggle pull-left animated flip">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="/Home.jsp"> <img alt="Charisma Logo" src="${ctx }/img/logo.png" class="hidden-xs" /> <span>JCoder</span></a>

			<!-- user dropdown starts -->
			<div class="btn-group pull-right">
				<c:if test="${user!=null }">
				<button class="btn btn-default dropdown-toggle"
					data-toggle="dropdown">
					<i class="glyphicon glyphicon-user"></i><span
						class="hidden-sm hidden-xs">&nbsp;${empty user?'admin':user }&nbsp;</span> <span class="caret"></span>
				</button>
				<ul class="dropdown-menu">
					<li><a href="${ctx }/down/sdk">DevSDK</a></li>
					<li class="divider"></li>
					<li><a href="${ctx }/loginOut">Logout</a></li>
				</ul>
				</c:if>
			</div>
			<!-- user dropdown ends -->

			<!-- theme selector starts -->
			<div class="btn-group pull-right theme-container animated tada">
				<button class="btn btn-default dropdown-toggle"
					data-toggle="dropdown">
					<i class="glyphicon glyphicon-tint"></i><span
						class="hidden-sm hidden-xs"> Change Theme / Skin</span> <span
						class="caret"></span>
				</button>
				<ul class="dropdown-menu" id="themes">
					<li><a data-value="classic" href="#"><i class="whitespace"></i>
							Classic</a></li>
					<li><a data-value="cerulean" href="#"><i
							class="whitespace"></i> Cerulean</a></li>
					<li><a data-value="cyborg" href="#"><i class="whitespace"></i>
							Cyborg</a></li>
					<li><a data-value="simplex" href="#"><i class="whitespace"></i>
							Simplex</a></li>
					<li><a data-value="darkly" href="#"><i class="whitespace"></i>
							Darkly</a></li>
					<li><a data-value="lumen" href="#"><i class="whitespace"></i>
							Lumen</a></li>
					<li><a data-value="slate" href="#"><i class="whitespace"></i>
							Slate</a></li>
					<li><a data-value="spacelab" href="#"><i
							class="whitespace"></i> Spacelab</a></li>
					<li><a data-value="united" href="#"><i class="whitespace"></i>
							United</a></li>
				</ul>
			</div>
			<!-- theme selector ends -->
		</div>
	</div>
