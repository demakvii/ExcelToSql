var app = angular.module('main');
app.controller('MainCtrl', ['$scope', '$location', 'studentFactory', function($scope, $location, studentFactory) {

    studentFactory.get(function(response) {

        $scope.students = response.data;
        for (var i = 0; i < $scope.students.length; i++) {
            $scope.students[i].isMale = $scope.students[i].isMale == true ? "male" : "female";
        }
    }, function(response) {});
    



    $scope.addStudent = function() {
        $location.path('/add');
    }


}]);
