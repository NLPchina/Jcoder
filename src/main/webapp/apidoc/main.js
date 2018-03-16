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
            var groupName = null, isGreen = true, isWarning = false, group = {};
            me.nav_apis = Array.prototype.concat($.map($.each(data, function (index, ele) {
                ele.sub && ele.sub.sort(function (a, b) {
                    var nameA = a.name.toUpperCase(), nameB = b.name.toUpperCase();
                    return nameA < nameB ? -1 : nameA > nameB ? 1 : 0;
                });
            }).concat(null), function (ele) {
                if (ele === null) {
                    group.cls = isGreen ? "" : (isWarning ? "alert-warning" : "alert-danger");
                    return;
                }

                var isNewGroup = groupName === null || groupName != ele.group;
                if (isNewGroup) {
                    if (groupName) {
                        group.cls = isGreen ? "" : (isWarning ? "alert-warning" : "alert-danger");
                    }
                    group = {name: groupName = ele.group, href: "#" + groupName, isGroup: true};
                    isGreen = true;
                    isWarning = false;
                }
                if (isGreen) isGreen &= ele.status;
                if (!isWarning) isWarning |= ele.status;
                return Array.prototype.concat(isNewGroup ? group : {}, {
                    name: ele.name,
                    cls: ele.status ? "" : "alert-danger",
                    href: "#" + groupName + "_" + ele.name,
                    isApi: true
                }, $.map(ele.sub || [], function (ele2) {
                    ele2.test = {
                        testing: false,
                        headers: [{name: null, value: null}],
                        params: $.map(ele2.sub || [], function (ele) {
                            return {name: ele.name, type: ele.type, content: ele.content, required: ele.required};
                        }),
                        response: {status: null, text: null}
                    };
                    ele2.returnContent = me.getJSONText(ele2.returnContent);
                    return {
                        name: ele2.name,
                        cls: ele.status ? "" : "alert-danger",
                        href: "#" + groupName + "_" + ele.name + "_" + ele2.name
                    };
                }));
            }));
            me.apis = data;
        }).always(function () {
            $("body #loader").remove();
            me.isLoading = false;

            //
            Vue.nextTick(function () {
                var $href = $(location.hash);
                if ($href.length > 0) $('html,body').animate({scrollTop: parseInt($href.offset().top)}, 0);
            });
            me.updateScrollspy();
        });

        // Content-Scroll on Navigation click.
        $('#scrollingNav').find('>.sidenav').on('click', 'a', function (e) {
            e.preventDefault();

            var href = $(this).attr('href');
            if ($(href).length < 1) {
                href = $("a[href^='" + href + "']:not(a[href='" + href + "']):first").attr('href');
            }
            if ($(href).length > 0) $('html,body').animate({scrollTop: parseInt($(href).offset().top)}, 400);
            location.hash = href;
        });
    },

    methods: {
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
