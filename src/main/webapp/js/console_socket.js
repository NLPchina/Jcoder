// from 序列化为 json
(function($) {
	$.fn.serializeJson = function() {
		var serializeObj = {};
		var array = this.serializeArray();
		var str = this.serialize();
		$(array).each(
				function() {
					if (serializeObj[this.name]) {
						if ($.isArray(serializeObj[this.name])) {
							serializeObj[this.name].push(this.value);
						} else {
							serializeObj[this.name] = [
									serializeObj[this.name], this.value ];
						}
					} else {
						serializeObj[this.name] = this.value;
					}
				});
		return serializeObj;
	};
})(jQuery);

$(function() {
	"use strict";

	var header = $('#header');
	var content = $('#console');
	var input = $('#input');
	var logged = false;
	var socket = atmosphere;
	var subSocket;
	var transport = 'websocket';
	var autoScroll = true;
	var status = false;
	var count = 0;
	var console_filter = $('#console_filter');
	var console_contains = $('#console_contains');

	// We are now ready to cut the request
	var request = {
		url : '/console',
		contentType : "application/json",
		logLevel : 'info',
		transport : transport,
		trackMessageLength : true,
		reconnectInterval : 5000
	};

	request.onOpen = function(response) {
		openStyle();
		request.uuid = response.request.uuid;
	};

	request.onClientTimeout = function(r) {
		content
				.append($(
						'<p>',
						{
							text : 'Client closed the connection after a timeout. Reconnecting in '
									+ request.reconnectInterval
						}));
		input.attr('disabled', 'disabled');
		closeStyle();
		setTimeout(function() {
			subSocket = socket.subscribe(request);
		}, request.reconnectInterval);
	};

	request.onReopen = function(response) {
		openStyle();
		content.html($('<p>', {
			text : 'Atmosphere re-connected using ' + response.transport
		}));
	};

	// For demonstration of how you can customize the fallbackTransport using
	// the onTransportFailure function
	request.onTransportFailure = function(errorMsg, request) {
		atmosphere.util.info(errorMsg);
		request.fallbackTransport = "long-polling";
		content
				.append($(
						'<h3>',
						{
							text : 'Atmosphere Chat. Default transport is  WebSocket, fallback is '
									+ request.fallbackTransport
						}));
	};

	request.onMessage = function(response) {
		var message = response.responseBody;

		var temp = console_contains.val();

		if (temp != undefined && temp != "" && message.indexOf(temp) == -1) {
			return;
		}

		temp = console_filter.val();
		if (temp != "" && message.indexOf(temp) >= 0) {
			return;
		}

		content.append('<p>'+message+'</p>');
		if (autoScroll) {
			content.scrollTop(content.prop("scrollHeight"));
		}
		if (count++ > 100) { // 删除溢出的元素
			content.children()[0].remove()
		}
	};

	content.scroll(function() {
		if (content.prop("scrollHeight") - content.scrollTop() > 50 + $(
				'#console').height()) {
			autoScroll = false;
		} else {
			autoScroll = true;
		}
	});

	request.onClose = function(response) {
		content.append('connect has been closed!');
		closeStyle();
	};

	request.onError = function(response) {
		content.append('<p>err!!!!!!!!!!!!!!!!!!!!</p>');
		closeStyle();
	};

	request.onReconnect = function(request, response) {
		content.append('<p>reconnect server!</p>');
		input.attr('disabled', 'disabled');
	};

	function closeStyle() {
		status = false;
		$("#socket_connected").removeClass("btn-success")
		$("#script_start").removeClass("btn-success")
		$("#script_stop").removeClass("btn-danger")
	}

	function openStyle() {
		status = true;
		$("#socket_connected").addClass("btn-success")
		$("#script_start").addClass("btn-success")
		$("#script_stop").addClass("btn-danger")
	}

	$("#console_clear").click(function() {
		content.empty();
	});

	$("#script_start").click(function() {
		if (status) {
			$("#code").val(editor.getValue());
			var json = $("#taskForm").serializeJson()
			if (status) {
				$.post("/run_api", {
					"json" : JSON.stringify(json)
				}, function(result) {
					content.append("<p>" + JSON.stringify(result) + "</p>");
				}, "json").error(function() {
					content.append("<p>run err !</p>");
				});
			} else {
				content.append("<p>server has beein break ! please reconnect</p>");
			}
		} else {
			content.append("<p>server has beein break ! please reconnect</p>");
		}
	});

	$("#script_stop").click(function() {
		if (status) {
			$("#code").val(editor.getValue());
			var json = $("#taskForm").serializeJson()
			if (status) {
				$.post("/stop_api", {
					"json" : JSON.stringify(json)
				}, function(result) {
					content.append("<p>" + JSON.stringify(result) + "</p>");
				}, "json").error(function() {
					content.append("<p>run err !" + JSON.stringify(result) + "</p>");
				});
			} else {
				content.append("<p>server has beein break ! please reconnect</p>");
			}
		} else {
			content.append("<p>server has beein break ! please reconnect</p>");
		}
	});

	$("#socket_connected").click(function() {
		if (!status) {
			content.empty();
			subSocket = socket.subscribe(request);
		} else {
			subSocket.close();
		}
	});

	subSocket = socket.subscribe(request);

});