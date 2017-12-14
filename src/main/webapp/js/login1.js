/**
 * login
 */

var vmLogin = new Vue({
    el: '#vmLogin',
    data: function () {
        var userId = '', password = '', verification_code = '',vCodeImg='qdeMods/ajax?action=captchaAction&verb=getKaptchaImage', rememberMe = false;
        if (localStorage.rememberMe) {
            rememberMe = eval(localStorage.rememberMe);
        }
        if (rememberMe) {
            userId = localStorage.userId;
            password = localStorage.password;
        }
        return {
            userId: userId,
            password: password,
            rememberMe: rememberMe,
            verification_code: verification_code,
            vCodeImg:vCodeImg,
            error: '',
        }
    },
    methods: {
        login: function () {
            $.ajax({
                type: 'post',
                url: Config.apiPath + '/qdeMods/login',
                data: {
                    userId: vmLogin.userId,
                    password: vmLogin.password,
                    verification_code: vmLogin.verification_code
                },
                success: function (result) {
                	if (result.indexOf('模块导航') > -1) {
                        vmLogin.error = '';
                        if (vmLogin.rememberMe) {
                            localStorage.rememberMe = true;
                            localStorage.userId = vmLogin.userId;
                            localStorage.password = vmLogin.password;
                        } else {
                            localStorage.rememberMe = false;
                            localStorage.userId = null;
                            localStorage.password = null;
                        }

                        location = './';
                    } else {
                        vmLogin.error = eval('(' + result + ')').message;
                        vmLogin.reflush() ;
                    }
                } 
            });
        },
        
        reflush: function(){
        	vmLogin.vCodeImg='qdeMods/ajax?action=captchaAction&verb=getKaptchaImage&_'+new Date() ;
        }
    }
});

//you don't need this, just used for changing background
jQuery(function ($) {
    $('#btn-login-dark').on('click', function (e) {
        $('body').attr('class', 'login-layout');
        $('#id-text2').attr('class', 'white');
        $('#id-company-text').attr('class', 'blue');

        e.preventDefault();
    });
    $('#btn-login-light').on('click', function (e) {
        $('body').attr('class', 'login-layout light-login');
        $('#id-text2').attr('class', 'grey');
        $('#id-company-text').attr('class', 'blue');

        e.preventDefault();
    });
    $('#btn-login-blur').on('click', function (e) {
        $('body').attr('class', 'login-layout blur-login');
        $('#id-text2').attr('class', 'white');
        $('#id-company-text').attr('class', 'light-blue');

        e.preventDefault();
    });

});