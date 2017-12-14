/**
 * JqdeMods ajax
 */
var JqdeMods = {
    ajax: function (action, verb, ajaxParams) {
        var apiPath = Config.apiPath.replace(/\/?$/, '');
        var url = apiPath + '/qdeMods/ajax?action=' + action +   '&verb=' + verb;

        return new Promise(function (resolve, reject) {
            var data = null;
            if (ajaxParams) {
                if ($.type(ajaxParams) === "string") {
                    data = {'ajaxParams': ajaxParams};
                } else {
                    data = {'ajaxParams': JSON.stringify(ajaxParams)};
                }
            }

            $.ajax({
                type: 'post',
                url: url,
                data: data,
                cache: false,
                dataType: 'json',
                success: function (data, textStatus, jqXHR) {
                    //

                    if (data.success == false && data.message.indexOf('重新登录') > -1) {
                        if (window.location.hash) {
                            JqdeBox.alert(data.message, function () {
                                window.location = './login.html';
                            });
                        } else {
                            window.location = './login.html';
                        }
                        return;
                    }

                    resolve(data);
                },
                error: function (XMLHttpRequest, textStatus, errorThrown) {

                    console.log(false);
                    reject(XMLHttpRequest);
                }
            });
        })
    },
    get: function (url, success, error) {
        $.ajax({
            type: 'get',
            url: url,
            cache: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {
                if (success) success(data);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                if (error) error(XMLHttpRequest);
            }
        });
    },
    post: function (url, data, success, error) {
        $.ajax({
            type: 'post',
            url: url,
            data: data,
            cache: false,
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {
                if (success) success(data);
            },
            error: function (XMLHttpRequest, textStatus, errorThrown) {
                if (error) error(XMLHttpRequest);
            }
        });
    }
};

/**
 * bootbox 弹出框
 * @type {{alert: JqdeBox.alert, confirm: JqdeBox.confirm, prompt: JqdeBox.prompt, dialog: JqdeBox.dialog, loading: JqdeBox.loading, unloading: JqdeBox.unloading}}
 */
var JqdeBox = {
    // alert/confirm/prompt/dialog
    alert: function (message, callback) {
        bootbox.alert({
            message: "<span class='bigger-130'>" + message + "</span>",
            buttons: {
                ok: {
                    label: '<i class="fa fa-check"></i> 确定',
                    className: 'btn-success'
                }
            },
            callback: callback
        });
    },
    confirm: function (message, callback) {
        bootbox.confirm({
            message: "<span class='bigger-130'>" + message + "</span>",
            buttons: {
                cancel: {
                    label: '<i class="fa fa-times"></i> 取消',
                    className: 'btn-sm',
                },
                confirm: {
                    label: '<i class="fa fa-check"></i> 确定',
                    className: 'btn-success btn-sm'
                }
            },
            callback: function (result) {
                _.result($(this).data('bs.modal'), 'resetScrollbar');
                $.isFunction(callback) && callback(result);
            }
        });
    },
    prompt: function (title, inputType, callback) {

        if ($.isFunction(inputType)) {
            callback = inputType;
            inputType = 'text';
        }

        bootbox.prompt({
            title: title,
            inputType: inputType,
            buttons: {
                cancel: {
                    label: '<i class="fa fa-times"></i> 取消',
                    className: 'btn-sm',
                },
                confirm: {
                    label: '<i class="fa fa-check"></i> 确定',
                    className: 'btn-success btn-sm',
                }
            },
            callback: callback
        });
    },
    dialog: function (options) {
        var message = '<p><i class="fa fa-spin fa-spinner"></i> Loading...</p>';
        if (options.message) {
            message = options.message;
        }

        var dialog = bootbox.dialog({
            title: "<span class='bigger-130'>" + options.title + "</span>",
            message: message,
            buttons: {
                cancel: {
                    label: '<i class="fa fa-times"></i> 取消',
                    className: 'btn-sm',
                    callback: function () {
                        if (options.cancel)
                            return options.cancel();
                        return true;
                    }
                },
                confirm: {
                    label: '<i class="fa fa-check"></i> 确定',
                    className: 'btn-success btn-sm',
                    callback: function () {
                        _.result(dialog.data('bs.modal'), 'resetScrollbar');
                        if (options.confirm)
                            return options.confirm();
                        return true;
                    }
                }
            }
        });

        dialog.init(function () {
            if (options.url) {
                $.get(options.url, function (html) {
                    dialog.find('.bootbox-body').html(html);
                    if (options.init) options.init(dialog);
                })
            } else {
                if (options.init) options.init(dialog);
            }
        });

        return dialog;
    },
    hideAll: function () {
        bootbox.hideAll()
    },

    // loading
    loading: function () {
        this.dlgLoading = bootbox.dialog({
            message: '<div class="text-center"><i class="fa fa-spin fa-spinner"></i> Loading...</div>',
            backdrop: false,
            closeButton: false
        })
    },
    unloading: function () {
        var dlgLoading = this.dlgLoading;
        setTimeout(function () {
            if (dlgLoading) {
                _.result(dlgLoading.data('bs.modal'), 'resetScrollbar');
                dlgLoading.modal('hide');
            }
        }, 200);
    },

    // message box
    message: function (type, message) {
        if (type === true) type = 'success';
        if (type === false) type = 'error';
        $.gritter.add({
            title: '提示',
            time: 1000,
            text: message,
            class_name: 'gritter-' + type + ' gritter-light'
        });
    }
}