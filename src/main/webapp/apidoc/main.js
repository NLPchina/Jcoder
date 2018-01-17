new Vue({
    el: '#vmApiDoc',

    data: {
        isLoading: true,
        apis: null,
        nav_apis: null,
        baseUri: location.protocol + "//" + location.host
    },

    mounted: function () {
        var me = this;

        $.getJSON("/apidoc/info").done(function (data) {
            me.nav_apis = Array.prototype.concat($.map(data, function (ele) {
                return Array.prototype.concat({
                    name: ele.name,
                    status: ele.status,
                    isGroup: true,
                    isActive: false,
                    href: "#" + ele.name
                }, $.map(ele.sub || [], function (ele2) {
                    ele2.test = {
                        testing: false,
                        headers: [{name: null, value: null}],
                        params: $.map(ele2.sub || [], function (ele) {
                            return {name: ele.name, type: ele.type, content: ele.content}
                        }),
                        response: {status: null, text: null}
                    };
                    ele2.retrunContent = me.getJSONText(ele2.retrunContent);
                    return {
                        name: ele2.name,
                        status: ele2.status,
                        isGroup: false,
                        isActive: false,
                        href: "#" + ele.name + "_" + ele2.name
                    };
                }));
            }));
            me.apis = data;
        }).always(function () {
            $("body #loader").remove();
            me.isLoading = false;

            //
            me.updateScrollspy();
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
    },

    methods: {
        clickNav: function (x) {
            $(this.nav_apis).attr("isActive", false);
            x.isActive = true;
        },

        changeHeader: function (headers, index) {
            if (headers.length - 1 === index) {
                headers.push({name: null, value: null});
                this.updateScrollspy();
            }
        },

        deleteHeader: function (headers, index) {
            headers.splice(index, 1);
            this.updateScrollspy();
        },

        submit: function (currentTarget, testObj) {
            var me = this, headers = {}, $form = $(currentTarget), res = testObj.response;
            $.each(testObj.headers, function (i, ele) {
                if ($.trim(ele.name)) {
                    headers[$.trim(ele.name)] = $.trim(ele.value);
                }
            });

            $form.ajaxSubmit({
                headers: headers,
                beforeSubmit: function (arr, $form, options) {
                    res.status = res.text = null;
                    $form.addClass("position-relative").find("div:last").removeClass("hidden");
                    return true;
                },
                success: function (responseText, statusText, xhr, $form) {
                    $form.removeClass("position-relative").find("div:last").addClass("hidden");
                    res.status = xhr.status;
                    res.text = me.getJSONText(responseText);
                    me.updateScrollspy();
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    $form.removeClass("position-relative").find("div:last").addClass("hidden");
                    res.status = jqXHR.status;
                    res.text = me.getJSONText(jqXHR.responseText);
                    me.updateScrollspy();
                }
            });
        },

        getJSONText: function (text) {
            var jsonText;
            try {
                jsonText = JSON.stringify(JSON.parse(text), null, 4);
            } catch (e) {
                jsonText = text;
            }
            return jsonText;
        },

        updateScrollspy: function () {
            Vue.nextTick(function () {
                $("[data-spy='scroll']").each(function () {
                    $(this).scrollspy('refresh');
                });
            });
        }
    }
});
