var rootApp = angular.module('rootApp', ['ui.router', 'ui.grid', 'appControllers', 'appDirectives', 'appServices']);
rootApp.config(function($stateProvider, $urlRouterProvider){
    $urlRouterProvider.otherwise('/');
    $stateProvider
        .state('/',{
            url:'/',
            views:{
                '':{
                    templateUrl:"templates/common/login/main.html",
                    controller: "loginController"
                }
            }
        });
});


