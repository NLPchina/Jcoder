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
    			window.location = window.location.protocol+"//"+window.location.host;
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
			$.ajax({
                type: 'post',
                url: '/admin/main/left',
                success: function (result) {
                    console.log(result.obj);
                    $this.menus = result.obj;
                    Vue.nextTick(function(){
                    	vmApp.init();
                    });
                },
                error: function (error) {
                    JqdeBox.message(false, error);
                }
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

            // fire links with targets on different window
            /*$('#nav a[target="_blank"]').click(function (e) {
                e.preventDefault();
                window.open($(this).attr('href'));
            });
            {
        	"name": "接口任务",
        	"submenus": [{
        		"name": "按钮",
        		"url": "thread_list.html"
        	}]
        }, {
        	"name": "系统管理",
        	"submenus": [{
        		"name": "用户管理",
        		"url": "userManager.html"
        	}, {
        		"name": "Group Manager",
        		"url": "groupManager.html"
        	}]
        }
            *
            */

            // all links with hash tags are ignored
            /*$('#nav a[href="#"]').click(function (e) {
                e.preventDefault();
                if ($(this).parents('.menu-min').length == 0) {
                    $(this).parent().find('.submenu').slideToggle();
                }
            });*/
        },
        checkURL: function () {
        	var $this = this;
        	debugger;
            //get the url by removing the hash
            var url = location.hash.replace(/^#/, '');

            param = {};

            // Do this if url exists (for page refresh, etc...)
            if (url) {
            	url = 'modules/' + url.replace(/^\//, '');
                
                // console.log(url);

                var urls = url.split('?');

                if (urls.length > 1) {
                    window.param = _.chain(urls[1].split('&'))
                        .map(function (value) {
                            return value.split('=');
                        }).object().value();
                    // console.log(param);
                }

                var href = '/' + urls[0] ;
                // console.log(href);

                // remove all active class
                $('#nav li.active').removeClass("active");
                $('#nav li.open').removeClass("open");

                // match the url and add the active class
                $('#nav li:has(a[href="#' + href + '"])').addClass("active")
                    .parents('li').addClass("active").addClass("open")
                    .siblings().find('.submenu').slideUp('fast');


                /*if(vmApp.module && vmApp.module instanceof Vue)
                    vmApp.module.$destroy();*/

                // parse url to jquery
                Tools.loadURL(urls[0], $('#content'), function () {
                	$this.drawBreadCrumb();
                }, function () {
                	$this.drawBreadCrumb();
                });

            } else {
                //update hash
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