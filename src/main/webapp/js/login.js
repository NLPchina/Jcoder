/*layui.use(['layer', 'form'], function() {
	var layer = layui.layer,
		$ = layui.jquery;
	$("#sub").click(function(){
		var param = {name:$("#userName").val(),password:$("#password").val()};
		var flag = false;
		$.ajax({
			'url' : '/admin/login',
			'dataType' : 'json',
			'type' : 'POST',
			'data' : param,
			'success' : function(data) {
				if(data.ok){
					flag = true;
					localStorage.setItem("userName",data.obj.user);
					localStorage.setItem("userId",data.obj.userId);
					localStorage.setItem("userType",data.obj.userType);
					localStorage.setItem("AUTH_MAP",data.obj.AUTH_MAP);
					localStorage.setItem("GROUP_LIST",data.obj.GROUP_LIST);
					location.href='index.html';
					return false;
				}else{
					$('#message').addClass("alert alert-danger");
					$('#message').text(data.message) ;
				}
			}
		});
		return false;
	});	
}); */
$(function(){
	$("#sub").on('click',function(){
		var param = {name:$("#userName").val(),password:$("#password").val()};
		var flag = false;
		$.ajax({
			'url' : '/admin/login',
			'dataType' : 'json',
			'type' : 'POST',
			'data' : param,
			'success' : function(data) {
				if(data.ok){
					console.log(data);
					flag = true;
					localStorage.setItem("userName",data.obj.user);
					localStorage.setItem("userId",data.obj.userId);
					localStorage.setItem("userType",data.obj.userType);
					localStorage.setItem("AUTH_MAP",data.obj.AUTH_MAP);
					localStorage.setItem("GROUP_LIST",data.obj.GROUP_LIST);
					location.href = '/.';
				}else{
					$('#message').addClass("alert alert-danger");
					$('#message').text(data.message) ;
				}
			}
		});
		return false;
	});
});

	


var c=document.getElementById("c");
var ctx=c.getContext("2d");
	c.width=window.innerWidth;
	c.height=window.innerHeight;
var string1 = "abcdefghjklmnopqrstuvwxyz1234567890";
	string1.split("");
var fontsize=20;
	columns=c.width/fontsize;
var drop = [];
for(var x=0;x<columns;x++){
	drop[x]=0;
}
function drap(){
	ctx.fillStyle="rgba(0,0,0,0.07)";
	ctx.fillRect(0,0,c.width,c.height);
	ctx.fillStyle="#0F0";
	ctx.font=fontsize+"px arial";
	for(var i=0;i<drop.length;i++){
		var text1=string1[Math.floor(Math.random()*string1.length)];
		ctx.fillText(text1,i*fontsize,drop[i]*fontsize);
		drop[i]++;
		if(drop[i]*fontsize>c.height&&Math.random()>0.9){//90%的几率掉落
			drop[i]=0;
		}
	}
}
setInterval(drap,50);