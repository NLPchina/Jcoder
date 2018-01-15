new Vue({
    el: '#vmApiDoc',

    data: {
        apis: null,
        nav_apis: null
    },

    mounted: function () {
        var me = this;

        $.getJSON("/apidoc/info").done(function (data) {
            me.nav_apis = Array.prototype.concat($.map(me.apis = data, function (ele) {
                return Array.prototype.concat({
                    name: ele.name,
                    status: ele.status,
                    isGroup: true,
                    isActive: false,
                    href: "#" + ele.name
                }, $.map(ele.sub || [], function (ele2) {
                    return {
                        name: ele2.name,
                        status: ele2.status,
                        isGroup: false,
                        isActive: false,
                        href: "#" + ele.name + "_" + ele2.name
                    };
                }));
            }));
        });

        // Content-Scroll on Navigation click.
        $('#scrollingNav').find('>.sidenav').on('click', 'a', function (e) {
            e.preventDefault();

            var href = $(this).attr('href');
            if ($(href).length > 0) {
                $('html,body').animate({scrollTop: parseInt($(href).offset().top)}, 400);
            }
            location.hash = href;
        });

        //
        $('#sections').on('click', 'input[name="checkTest"]', function () {
            $(this).closest("article").find("div[name='testBlock']").toggleClass("hidden");
        });
    },

    methods: {
        clickNav: function (x) {
            $(this.nav_apis).attr("isActive", false);
            x.isActive = true;
        }
    }
});
