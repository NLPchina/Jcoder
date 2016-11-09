<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib  prefix="c" uri="http://java.sun.com/jstl/core_rt"%>

<div class="col-sm-2 col-lg-2">
    <div class="sidebar-nav">
        <div class="nav-canvas">
            <div class="nav-sm nav nav-stacked">
            </div>
            <ul class="nav nav-pills nav-stacked main-menu">
                <li class="nav-header">Task　Group</li>
                <c:forEach items="${GROUP_LIST}" var="m">
                <li><a class="ajax-link" href="${ctx }/task/group?groupId=${m.id}"><i class="glyphicon glyphicon-tasks"></i><span> ${m.name}</span></a></li>
                </c:forEach>
                
                <li class="nav-header hidden-md">Manger</li>
                <li><a class="ajax-link" href="${ctx }/thread/list/"><i class="glyphicon glyphicon-list-alt"></i><span>Thread Manager</span></a></li>
                <li><a class="ajax-link" href="${ctx }/jcoder_console.jsp"><i class="glyphicon glyphicon-eye-open"></i><span> Console </span></a></li>
                <c:if test="${userType==1}">
                <li><a class="ajax-link" href="${ctx }/ioc"><i class="glyphicon glyphicon-th"></i><span> Ioc Manager </span></a></li>
                <li><a class="ajax-link" href="${ctx }/jar/list"><i class="glyphicon glyphicon-upload"></i><span> Jar Manger </span></a></li>
                <li><a class="ajax-link" href="${ctx }/resource/list"><i class="glyphicon glyphicon-file"></i><span> Resource Manager </span></a></li>
                <li><a class="ajax-link" href="${ctx }/system"><i class="glyphicon glyphicon-file"></i><span> System Setting </span></a></li>
                <li><a class="ajax-link" href="${ctx }/user/list"><i class="glyphicon glyphicon-user"></i><span> User Manager </span></a></li>
                <li><a class="ajax-link" href="${ctx }/group/list"><i class="glyphicon glyphicon-tower"></i><span> Group Manager </span></a></li>
                </c:if>
                
            </ul>
        </div>
    </div>
</div>
<!--/span-->