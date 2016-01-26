<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<script>
	var station_data;
	//One controller to rule them all :P 
	//Depending on the JSP type it does call different backend functions
	var app = angular.module('contentApp', []);
	app.controller('contentController', function($scope, $http) {
						var url = "";
						if(${type} == 0)
							url = "http://localhost:8080/SA-Service/sa/stations/${name}/${lon_min}/${lon_max}/${lat_min}/${lat_max}";
						else if(${type} == 1)
							url = "http://localhost:8080/SA-Service/sa/routes/getconnections/${name_from}/${name_to}";
						else if(${type} == 2)
							url = "http://localhost:8080/SA-Service/sa/restaurants/byNameAndDistance/${name}/${distance}";
						
						$http.get(url).then(function(response) {
									//The response json Array from the backend has to have the name "Content", otherwise
									//we would have to make another if/elseif/elseif, which is just cumbersome)
									$scope.content = response.data.Content;
									//This function is in map.jsp and updates the view
									updateMap(response.data.Content, ${type}, $http);
								}, function errorCallback(response) {
									//In case of an error just initialize a map without markers on it (homepage)
									initialize_map(); //Otherwise there would be no map when the request failed!
								});
					});
									
</script>


<div ng-app="contentApp" ng-controller="contentController">
	<table class="table table-striped table-bordered table-hover">

		<thead>
			<tr>
				<c:if test="${type == 0}">
					<th>Name</th>
					<th>lon</th>
					<th>lat</th>
				</c:if>
				<c:if test="${type == 1}">
					<th>Route Name</th>
					<th>From</th>
					<th>To</th>
				</c:if>
				<c:if test="${type == 2}">
					<th>Name</th>
					<th>Amenity</th>
					<th>Street</th>
				</c:if>
			</tr>
		</thead>
		
		<c:if test="${type == 0}">
			<tr ng-repeat="station in content">
				<td>{{ station.name }}</td>
				<td>{{ station.lon }}</td>
				<td>{{ station.lat }}</td>
			</tr>
		</c:if>
		<c:if test="${type == 1}">
			<tr ng-repeat="route in content">
				<td>{{ route.name }}</td>
				<td>{{ route.oname }}</td>
				<td>{{ route.vname }}</td>
			</tr>
		</c:if>
		<c:if test="${type == 2}">
			<tr ng-repeat="restaurant in content">
				<td>{{ restaurant.name }}</td>
				<td>{{ restaurant.amenity }}</td>
				<td>{{ restaurant.street }}</td>
			</tr>
		</c:if>
	</table>
</div>
