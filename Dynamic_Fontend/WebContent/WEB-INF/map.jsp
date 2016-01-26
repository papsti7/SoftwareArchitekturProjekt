<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>


<script src="http://maps.googleapis.com/maps/api/js"></script>
<script>
	//Map object, defined after the dom is loaded
	var map = null;

	//Global containers, used to delete some entries later in the dynamic application
	var markers = [];
	var lines = [];

	//Init the map
	function initialize_map() {
		var mapProp = {
			center : new google.maps.LatLng(47.0674801999999985,
					15.4425892999999999), //Jakomini
			zoom : 12,
			mapTypeId : google.maps.MapTypeId.STREETMAP
		};
		map = new google.maps.Map(document.getElementById("googleMap"), mapProp);
	}
	
	//Just adds an red marker on the given position, the name will be display in the infoWindow when clicked
	//Will also return the marker for further customization
	function addMarker(name, lat, lon) {
		var marker = new google.maps.Marker({
			position : new google.maps.LatLng(lat, lon),
		});

		//add a info windows (when you click on it) to the marker
		var infowindow = new google.maps.InfoWindow({
			content : name
		});

		marker.addListener("click", function() {
			infowindow.open(map, marker);
		});

		//Activate it on the map
		marker.setMap(map);

		//Add it to the global list
		markers.push(marker);
		
		return marker;
	}

	//Will get called when we get new data from the angular controller
	function updateMap(map_data, type, $http) {

		if (!map)
			initialize_map();

		if (type == 0) { //Stations
			//Iterate over the response (all stations)
			angular.forEach(map_data, function(station) {
				addMarker(station.name, station.lat, station.lon);
			});
		}
		else if(type == 2){ // Restaurants		
			//Iterate over the response (all restaurants)
			angular.forEach(map_data, function(restaurant) {
				addMarker(restaurant.name, restaurant.lat, restaurant.lon);
			});	
		}
		else if(type == 1) { //Connections
			//Iterate over all connections
			angular.forEach(map_data, function(route) {
				//Used to store all vertices of the polyline
				var lineCoords = [];
				//Build the right url to get the stopps of this particular route
				var url = "http://localhost:8080/SA-Service/sa/routes/getstations/" + route.route_id;
				
				//Get the stations of each route (CAREFUL this is ASYNC!!!)
				$http.get(url).then(function(response) {
					//Iterate over all stops of this route
					angular.forEach(response.data.Content, function(station) {
						if(station){
							//Add the position to the poly line coordinates array
							var coordinates = new google.maps.LatLng(station.lat, station.lon);
							lineCoords.push(coordinates);
							
							//Check if its start or end position, and add a marker there (Does not work because we can enter: "Jako")
							if(station.name == "${name_from}") {
								addMarker(station.name, station.lat, station.lon);
							} else if(station.name == "${name_to}") {
								addMarker(station.name, station.lat, station.lon);
							}
						}
					});
					//Create a simple route with the coordinate array filled above
					var routeLine = new google.maps.Polyline({
					    path: lineCoords,
					    geodesic: true,
					    strokeColor: '#FF0000',
					    strokeOpacity: 1.0,
					    strokeWeight: 2
					  });

					  routeLine.setMap(map);
						
					  //Add it to the global list so we can remove it in the dynamic application
					  lines.push(routeLine);
				});
				//Do not insert code here, the call above is async!!!
			});
		}
	}
</script>

<div id="googleMap" style="height: 400px;"></div>
