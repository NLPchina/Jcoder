
var Jcoder = {
    ajax: function (url,method, ajaxParams,callback) {
    	return new Promise(function (resolve, reject) {
    		$.ajax({
    			type: method,
    			url: url,
    			data: ajaxParams,
    			cache: false,
    			dataType: 'json',
    			success: function (data, textStatus, jqXHR) {
    				if (callback) callback();
    				resolve(data);
    			},
    			error: function (XMLHttpRequest, textStatus, errorThrown) {
    				if (errorThrown=="450") {
                        if (window.location.hash) {
                            JqdeBox.alert(XMLHttpRequest.responseJSON.message, function () {
                                window.location = './login.html';
                            });
                        } else {
                            window.location = './login.html';
                        }
                        return;
                    }else{
                        reject(XMLHttpRequest);
                    }
    			}
    		});
    	});
    }
}

var vmApp = new Vue({
    el: '#vmApp',
    data: {
        userId: '',
        userName: '',
        userType:'',
	  	AUTH_MAP:null,
	  	GROUP_LIST:null,
        menus: [],
        iconCls: ['fa fa-cogs', 'fa fa-users', 'glyphicon glyphicon-align-justify', 'fa fa-book', 'glyphicon glyphicon-th-large', 'fa fa-desktop', 'fa fa-gift', 'glyphicon glyphicon-book', 'fa fa-bar-chart-o', 'fa fa-gavel','fa-picture-o', 'fa-tag', 'fa-folder', 'fa-list'],
        modulePath: '',
        item: {},
        regURL: null,
        dbDelta: 0,
        indexDelta: 0
    },
    mounted: function () {
    	var $this = this;
    	this.userName = localStorage.getItem("userName");
    	if(this.userName == null || this.userName == undefined){
    		JqdeBox.alert("请重新登录！",function(){
    			window.location = 'login.html';
    		});
    	}
    	this.userId = localStorage.getItem("userId");
    	this.userType = localStorage.getItem("userType"); 
    	this.AUTH_MAP = localStorage.getItem("AUTH_MAP"); 
    	this.GROUP_LIST = localStorage.getItem("GROUP_LIST");
    	$(window).on('hashchange', function () {
            $this.checkURL();
        });
        $this.initMenus();
    },
    methods: {
    	initMenus:function(){
    		var $this = this;
            Jcoder.ajax('/admin/main/left', 'post',null,null).then(function (result) {
                $this.menus = result.obj;
                Vue.nextTick(function(){
                    vmApp.init();
                });
            });
    	},
    	init: function () {
            if ($('#nav').length) {
                this.checkURL();
            }

            $('#nav a[href!="#"]').click(function (e) {
                e.preventDefault();
                window.location.hash = $(this).attr('href');
            });
        },
        checkURL: function () {
        	debugger;
        	
        	var $this = this;
            var url = location.hash.replace(/^#/, '');

            param = {};

            if (url) {
            	url = 'modules/' + url.replace(/^\//, '');

                var urls = url.split('?');

                if (urls.length > 1) {
                    window.param = _.chain(urls[1].split('&'))
                        .map(function (value) {
                            return value.split('=');
                        }).object().value();
                }

                var href = '/' + urls[0] ;
                
                $('#nav li.active').removeClass("active");
                $('#nav li.open').removeClass("open");

                $('#nav li:has(a[href="#' + href.replace('/modules','') + '"])').addClass("active")
                    .parents('li').addClass("active").addClass("open")
                    .siblings().find('.submenu').slideUp('fast');

                Tools.loadURL(urls[0], $('#content'), function () {
                	$this.drawBreadCrumb();
                }, function () {
                	$this.drawBreadCrumb();
                });

            } else {
                window.location.hash = $('#nav > li:first-child > a[href!="#"]').attr('href');
            }
        },
        // UPDATE BREADCRUMB
        drawBreadCrumb: function () {
            $("#breadcrumbs ul.breadcrumb").empty();
            $("#breadcrumbs ul.breadcrumb").append($('<li><i class="ace-icon fa fa-home home-icon"></i> 首页 </li>'));
            $('#nav li.active > a').each(function () {
                $("#breadcrumbs ul.breadcrumb").append($("<li></li>")
                    .html($.trim($(this).clone().children(".badge").remove().end().text())));
            });
        },
        logout: function () {
            JqdeBox.confirm('您确定要退出吗？', function (result) {
                if (result) {
                	localStorage.clear();
                    $.ajax({
                        type: 'post',
                        url: '/admin/loginOut',
                        success: function (result) {
                            location = 'login.html';
                        },
                        error: function (error) {
                            JqdeBox.message(false, error);
                        }
                    });
                }
            });
        },
    }
});


