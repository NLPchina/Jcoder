var apiApp = angular.module('apiApp', []);
apiApp.controller('apiCtrl', function($scope, $http, $location) {
	
	//'vendor/api.json'
	$http.get('/apidoc/info').success(function(response) { 
		$scope.datas = response;
		
		$scope.atx = $location.$$protocol+"://"+$location.$$host+":"+$location.$$port;
		
		$scope.nav_datas = [{
			"name": "General",
			"href": "#api-_",
			"isGroup": true,
			"isActive": true,
			"sub": []
		}];
		
		$($scope.datas).each(function(){
			var action = this;
			action.isGroup = true;
			action.href = "#" + action.name;
			$scope.nav_datas.push(action);
			
			$(action.sub).each(function(){
				var method = this;
				method.isGroup = false;
				method.href = "#" + action.name + "_" + method.name;
				$scope.nav_datas.push(method);
			});
			
		});
		
	});
	
	$scope.clickNav = function(x){
		$($scope.nav_datas).each(function(){this.isActive=false;});
		x.isActive = true;
		
		event.preventDefault();
        var id = $(event.target).attr('href');
        if ($(id).length > 0)
            $('html,body').animate({ scrollTop: parseInt($(id).offset().top) }, 400);
        window.location.hash = x.href;
	};
	
});