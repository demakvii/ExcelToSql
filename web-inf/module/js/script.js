var app = angular.module("mainApp", ['ngMaterial', 'ngMessages']);
app.controller('MainCtrl', ['$scope',  function($scope) {

 $scope.person = {
   name : 'Mayur'
  };
}]);

