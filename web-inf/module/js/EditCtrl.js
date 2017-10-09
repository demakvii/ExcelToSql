var app = angular.module('main');
app.controller('EditCtrl', ['$scope', '$location', '$routeParams', 'studentFactory', function($scope, $location, $routeParams, studentFactory) {

    $scope.student = {};

    studentFactory.getStudentById($routeParams.rollNo, function(response) {
        $scope.student = response.data;

        $scope.student.isMale = $scope.student.isMale == true ? "male" : "female";

    }, function(response) {});
    $scope.goBack = function() {

        $location.path('/');
    }

    $scope.onSave = function() {
        if ($scope.student.rollNo.length != 0 && $scope.student.name.length != 0 && $scope.student.age.length != 0 && $scope.student.email.length != 0 && $scope.student.date.length != 0 && $scope.student.isMale.length != 0) {
            $scope.student.isMale = $scope.student.isMale == "male" ? true : false;
            studentFactory.editStudent($routeParams.rollNo, $scope.student, function(response) {
                console.log('Record Edited!');

                $scope.goBack();
            }, function(response) {});

        } else
            console.log('Some Field are empty');
    }
}]);
