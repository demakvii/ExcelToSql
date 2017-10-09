    var app = angular.module('main');
app.controller('AddCtrl', ['$scope', '$location', 'studentFactory', function($scope, $location, studentFactory) {
    $scope.student = {};
    $scope.goBack = function() {
        $location.path("/");
    }

    $scope.onAdd = function() {
        if ($scope.student.rollNo.length != 0 && $scope.student.name.length != 0 && $scope.student.age.length != 0 && $scope.student.email.length != 0 && $scope.student.date != null && $scope.student.isMale.length != 0) {
            $scope.student.isMale = $scope.student.isMale == "male" ? true : false;
            studentFactory.addStudent($scope.student, function(response) {
                console.log('Record Added!');

                $scope.goBack();
            }, function(response) { console.log(response) });


        } else {
            console.log('Some Fields are empty' + $scope.student.date);
        }
    }
}]);
// mainController.$inject = ['$scope'];