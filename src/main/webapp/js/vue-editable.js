(function() {
	var VueEditable = {
		install: function(Vue) {
			Vue.prototype.$editable = function(e,callback) {
				var target=e.target,value=target.innerText;
				target.innerHTML = "<input type='text' value='" + value + "' id='_editable' style='width:80%;box-sizing:border-box;background:transparent;font-size:13px;color:red;text-align:center'>";
				var input = document.getElementById('_editable');
				input.focus();
				var len = input.value.length;
				if (document.selection) {
					var sel = input.createTextRange();
					sel.moveStart('character', len);
					sel.collapse();
					sel.select();
				} else if (typeof input.selectionStart == 'number' && typeof input.selectionEnd == 'number') {
					input.selectionStart = input.selectionEnd = len;
				}

				var action = function() {
					if (value != this.value && this.value != '') {
						target.innerHTML = this.value;
						callback(this.value)
					} else {
						target.innerHTML = value;
					}
					input.removeEventListener("blur", action, false);
				};
				input.addEventListener("blur", action, false); 
			}
		}
	}

	if (typeof exports == "object") {
		module.exports = VueEditable;
	} else if (typeof define == "function" && define.amd) {
		define([], function() {
			return VueEditable;
		});
	} else if (window.Vue) {
		window.VueEditable = VueEditable;
		Vue.use(VueEditable);
	};
})();