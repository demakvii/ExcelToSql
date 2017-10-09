var app = angular.module('main');
app.controller('DeleteCtrl',['$scope','$location','studentFactory' ,'$routeParams',function($scope, $location, studentFactory,$routeParams) {

    console.log('Record Deleted!');
    studentFactory.deleteStudent($routeParams.rollNo, function(response) {
 $location.path('/');
    }, function(response) {});
   


}]);
