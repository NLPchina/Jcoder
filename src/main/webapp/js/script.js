/**
 * Created by Administrator on 2017/2/7.
 */

var navs;
var pages;

var top_r="<div class='ks-top col-md-topcolspan column ui-sortable'><span class='area_name'>top</span></div>";
var left='<div class="ks-left col-md-leftcolspan column ui-sortable"><span class="area_name">left</span></div>';
var center='<div class="ks-center col-md-centercolspan column ui-sortable"><span class="area_name">center</span></div>';
var right='<div class="ks-right col-md-rightcolspan column ui-sortable"><span class="area_name">right</span></div>';
var bottom="<div class='ks-bottom col-md-bottomcolspan column ui-sortable'><span class='area_name'>bottom</span></div>";

function removeElm() {
    $(".demo").delegate(".remove", "click",
        function(e) {
            e.preventDefault();
            $(this).parent().remove();
            if (!$(".demo .lyrow").length > 0) {
                clearDemo();
            }
        });
}
function clearDemo() {
    $(".demo").empty();
}


function cleanHtml(e) {
    $(e).parent().append($(e).children().html());
}

function drag(ev)
{
	var target_e = ev.target || ev.srcElement;
	//获取元数据字段以及对应的元数据库，格式:元数据字段【元数据库】
	var transfer_val = '';
	
	if($(target_e).attr("title")){
		transfer_val = $(target_e).attr("title");
	}
	else{
		transfer_val = $(target_e).text();
	}
	
	if(transfer_val.indexOf("【")>0 && transfer_val.indexOf("】")>0){
		var index_left = transfer_val.indexOf("【");
		transfer_val = transfer_val.substr(0,index_left);
	}
	
    ev.dataTransfer.setData("Text",transfer_val);
}

function dragover(ev)
{
	if(ev && ev.preventDefault ){ 
	    ev.preventDefault();
    } else { 
    	window.event.returnValue = false;
    } 
}

function drop(ev)
{
	if(ev && ev.preventDefault ){ 
	    ev.preventDefault();
	    
    } else { 
    	window.event.returnValue = false;
    	ev = window.event;
    } 
	
    var datavalue = ev.dataTransfer.getData("Text");
    
    //获取开始存在的值，获取拖拽添加的值，如果已经存在重复，则不需要修改添加
    var exit_val = $(ev.target).val();
    
    if($(ev.target).attr("multiple")){
    	if(exit_val.indexOf(",")>-1){
        	
        	if(exit_val.split(",").contains(datavalue)){
        		return false;
        	}
        	else{
        		 ev.target.value = exit_val+","+datavalue;
        	}
        }
        else{
        	if(exit_val==datavalue){
        		return false;
        	}
        	else if(exit_val==undefined || exit_val==''){
        		 ev.target.value = datavalue;
        	}
        	else{
        		ev.target.value = exit_val+","+datavalue;
        	}
        }
    }
    else{
    	 ev.target.value = datavalue;
    }
    
    return false;
}



function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");  
    var r = location.hash.substr(1).match(reg);  
    if (r != null) return decodeURI(r[2]);  
    return null;  
}  


//从数据库获取JSON并初始化页面布局
function initLayout(jsonarr,navs,arrs){
	var configId = getQueryString('configId');
	JqdeMods.ajax('ampConfigAction', 'getConfigInfoByConfigId',{'configId':configId}).then(function (result) {
		var jsonarr = result.obj.fields;
		var json_layout = eval("("+jsonarr+")");
		//获取布局
		for(var laykey in json_layout){
			if(eval(json_layout[laykey]).length>0){
				
				for(var i=0;i<eval(json_layout[laykey]).length;i++){
					
					var layobj = eval(json_layout[laykey])[i];
					
					//获取ID
					var moduleid = layobj.id;
					
					for(var n=0;n<navs.length;n++){
						if(navs[n].id==moduleid){
							var layout = navs[n].code;
							var code_rep = "<div class='box zj-"+n+" ui-draggable' style='position: relative; opacity: 1; width: 100%;'>\
	        			    <a href='#close' class='remove label label-danger'>\
	        			        <i class='glyphicon glyphicon-remove'></i>\
	        			        删除\
	        			        </a>\
	        			        <div class='preview'>"+navs[n].id+"</div>\
	        			        <div class='view'>\
	        			        <div class='row1 clearfix'>\
	        			        <div class='col-md-12 column-content'>"+layout+"\
	        			        </div>\
	        			        </div>\
	        			        </div>\
	        			        </div>";
						    
						        //初始化参数值
							    $(".area_name").each(function(){
								if($(this).text()==laykey){
									
									$(this).parent().append(code_rep);
									
									for(var okey in layobj){
										if(okey!='id'){
											$(this).parent().find(".column-content").last().find("[name='"+okey+"']").val(layobj[okey]);
										}
									}
								}
							});
						}
					}
				}
			}
		}
	});
}

//切换模版
function chagesection(obj){
    var arr=[];
    if(pages[$(obj).val()]){
    	 for(var key in pages[$(obj).val()].layout){
    	        arr.push(key);
    	    }
    }
    var vrr=[];
    var col={};
    if(pages[$(".select-mb").val()]){
    	 for(var key in pages[$(".select-mb").val()].layout){
    		 vrr.push(pages[$(".select-mb").val()].layout[key]);
             col[key] = pages[$(".select-mb").val()].colspan[key];
         };
    }
    //获取各区域中的文本信息
    layoutElement(vrr);
    
    pageLayout(arr,col);
    
    var jsonarr = '';
    //初始化拖拽的内容信息
    initLayout(jsonarr,navs,arr);
    
    alltuo();
}

function alltuo(){
    $(".sidebar-nav .box").draggable({
        connectToSortable: ".column",
        helper: "clone",
        handle: ".drag",
        opacity: 0.35,
        cursor: 'crosshair',
        drag: function(e, t) {
            t.helper.width("100%");
            t.helper.height("100%");
        },
        stop: function() {
        	event = event? event: window.event;
        	var targetelement =  event.srcElement ? event.srcElement:event.target; 
        	var arr = [];
        	for(var key in pages[$(".select-mb").val()].layout){
                arr.push(key);
            };
        	if(!$(targetelement).parent().parent().find("span.area_name").text() && $(targetelement).parent().parent().find("span.area_name").text()!='' && arr.contains($(targetelement).parent().parent().find("span.area_name").text())){
        		
        	}
        	else{
        		var moduleid = $(targetelement).parent().find(".preview").text();
            	var area = $(targetelement).parent().parent().find("span.area_name").text();
            	
            	//屏蔽"拖动"按钮
            	$(targetelement).parent().find("a.drag").hide();
            	
            	if(eval(pages[$(".select-mb").val()].layout)[area]){
            		 //如果当前位置不为当前拖动元素的可用区域，则允许拖入
                	if(eval(pages[$(".select-mb").val()].layout)[area].indexOf(moduleid)==-1){
                		//删除当前的拖拽条目,触发删除方法
                		$(targetelement).parent().find("a.remove").click();
                	}
            	}
            	else{
            		alltuo();
            	}
        	}
        	
        }
    });
    $(".demo .column").sortable({
        connectWith: ".column",
        opacity: 1,
        handle: ".drag"
    });
}

function pageLayout(arr,col){
	
	$('.shang').html('');
    if(arr.indexOf('top')>-1){
        $('.shang').append(top_r.replace("topcolspan",col['top']));
    }
	 
    $('.zhong').html('');
    if(arr.indexOf('left')>-1){
        $('.zhong').append(left.replace("leftcolspan",col['left']));
    }
    if(arr.indexOf('center')>-1){
        $('.zhong').append(center.replace("centercolspan",col['center']));
    }
    if(arr.indexOf('right')>-1){
        $('.zhong').append(right.replace("rightcolspan",col['right']));
    }
    
    $('.xia').html('');
    if(arr.indexOf('bottom')>-1){
        $('.xia').append(bottom.replace("bottomcolspan",col['bottom']));
    }
}

Array.prototype.contains = function (element) { 
	for (var i = 0; i < this.length; i++) { 
		if (this[i] == element) { 
		return true; 
		} 
	} 
	return false; 
} 


function layoutElement(arr){
	
	var zj="";
    var zimianarr=[];
    for(var i=0;i<arr.length;i++){
    	for(var j=0;j<arr[i].length;j++){
    		for(var k=0;k<navs.length;k++){
    			if(arr[i][j]==navs[k].id){
    				//去重
    				if(!zimianarr.contains(navs[k].id)){
    					zj +="<div class='box zj-"+k+"'>\
        			    <a href='#close' class='remove label label-danger'>\
        			        <i class='glyphicon glyphicon-remove'></i>\
        			        删除\
        			        </a>\
        			        <a class='drag label label-default'>\
        			        <i class='glyphicon glyphicon-move'></i>\
        			        拖动\
        			        </a>\
        			        <div class='preview'>"+navs[k].id+"</div>\
        			        <div class='view'>\
        			        <div class='row1 clearfix'>\
        			        <div class='col-md-12 column-content'>"+navs[k].code+"\
        			        </div>\
        			        </div>\
        			        </div>\
        			        </div>";
        			        zimianarr.push(navs[k].id);
    				}
    			}
    		}
    	}
    }
    $("#elmComponents").html(zj);
}

function getConfigXml(sitename){
	 
	JqdeMods.ajax('pmpSiteAction', 'getConfigXml',{'sitename':decodeURI(sitename)}).then(function (result) {
		
		navs=eval(result).models;
        pages=eval(result).pages;
        
        var arr=[];
        var col={};
        if(pages[$(".select-mb").val()]){
        	 for(var key in pages[$(".select-mb").val()].layout){
                 arr.push(key);
                 col[key] = pages[$(".select-mb").val()].colspan[key];
             };
        }
        //获取位置区域
        pageLayout(arr,col);
        var vrr=[];
        if(pages[$(".select-mb").val()]){
        	 for(var key in pages[$(".select-mb").val()].layout){
        		 vrr.push(pages[$(".select-mb").val()].layout[key]);
             };
        }
        //获取各区域中的文本信息
        layoutElement(vrr);
        
        var jsonarr='';
        //初始化拖拽的内容信息
        initLayout(jsonarr,navs,arr);
        
        alltuo();
	}); 
	
	/*  $.ajax({
          //请求方式为get
          type: "GET",
          //xml文件位置
          url: "template/"+decodeURI(sitename)+"/config.xml",
          //返回数据格式为xml
          dataType: "xml",
          //请求成功完成后要执行的方法
          success: function (xml) {alert(1111);
        	 $(xml).find("models").each(
        			 function(){
        				 var id=$(this).children("id");
        		          //获取节点文本
        		          var  id_value=id.text();
        				 alert(id_value);
        			 });
          }
	  });
	$.get("template/"+decodeURI(sitename)+"/config.xml",function(data){console.log(data);
		navs=xmlToJson(data).models;
		console.log('models');
		console.log(navs);
        pages=xmlToJson(data).pages;
        console.log('pages');
        console.log(pages);
        var arr=[];
        var col={};
        if(pages[$(".select-mb").val()]){
        	 for(var key in pages[$(".select-mb").val()].layout){
                 arr.push(key);
                 col[key] = pages[$(".select-mb").val()].colspan[key];
             };
        }
        //获取位置区域
        pageLayout(arr,col);
        var vrr=[];
        if(pages[$(".select-mb").val()]){
        	 for(var key in pages[$(".select-mb").val()].layout){
        		 vrr.push(pages[$(".select-mb").val()].layout[key]);
             };
        }
        //获取各区域中的文本信息
        layoutElement(vrr);
        
        var jsonarr='';
        //初始化拖拽的内容信息
        initLayout(jsonarr,navs,arr);
        
        alltuo();
    });*/
}

function filterC(){
	var filter_query = $("input[name='filter_column']").val();
	
	var reg = new RegExp(filter_query,'ig');
	$(".boxes").each(function(){
		if(this.style.display!='none'){
			if($.trim(filter_query)==''){
				$(this).find(".box1").each(function(){
						$(this).find(".bo").show();
				});
			}
			else{
				$(this).find(".box1").each(function(){
					var index_left = $(this).find(".bo").text().indexOf("【");
					if(!reg.test($(this).find(".bo").text().toString().substr(0,index_left))){
						$(this).find(".bo").hide();
					}
					else{
						$(this).find(".bo").show();
					}
				});
			}
		}
	});
}

$(window).resize(function() {
    $("body").css("min-height", $(window).height() - 90);
    $(".demo").css("min-height", $(window).height() - 160);
});

$(document).ready(function() {

    $("body").css("min-height", $(window).height() - 90);
    $(".demo").css("min-height", $(window).height() - 160);
    //获取模版底下的模版文件(btl文件)
    getConfigXml(getQueryString('folderName'));

    /*$(".yulan").empty().html("<img onmouseover='tip.start(this)' src=template/"+getQueryString('folderName')+"/index.jpg style='width:160px;height:200px;cursor:pointer;'>");*/
    removeElm();

});

var tip={$:function(ele){
	 if(typeof(ele)=="object")
	  return ele;
	 else if(typeof(ele)=="string"||typeof(ele)=="number")
	  return document.getElementById(ele.toString());
	  return null;
	 },
	 mousePos:function(e){
	  var x,y;
	  var e = e||window.event;
	  return{x:e.clientX+document.body.scrollLeft+document.documentElement.scrollLeft,
	y:e.clientY+document.body.scrollTop+document.documentElement.scrollTop};
	 },
	 start:function(obj){
	  var self = this;
	  var t = self.$("overview");
	  obj.onmousemove=function(e){
	  self.$("overviewimg").src = obj.getAttribute("src");
	   var mouse = self.mousePos(e);
	   t.style.left = mouse.x - 950 + 'px';
	   t.style.top = mouse.y-100+ 'px';
	  
	   t.style.display = '';
	  };
	  obj.onmouseout=function(){
	   t.style.display = 'none';
	  };
	 }
}

