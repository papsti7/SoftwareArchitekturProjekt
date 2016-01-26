var stationMap = null;
var connectionMap = null;
var restaurantMap = null;

function initialize() {
  var mapProp = {
    center: new google.maps.LatLng(47.0668014000000028, 15.4416869999999999),
    zoom: 13,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };

  stationMap = new google.maps.Map(document.getElementById("stationMap"), mapProp);
  connectionMap = new google.maps.Map(document.getElementById("connectionMap"), mapProp);
  restaurantMap = new google.maps.Map(document.getElementById("restaurantMap"), mapProp);
}
google.maps.event.addDomListener(window, 'load', initialize);

// --------------------------------------------------------- STATION MAP ------------------------------------- //

var stationMarkers = [];

function updateStationMap(data) {
  var filter = document.getElementById("searchStation").value;
  console.log("Stationmap filter: " + filter);

  //Delete old markers
  setMapOnAll(stationMarkers, null);

  if (filter.length >= 3) {
    angular.forEach(data, function(station) {
      if (station.name.toLowerCase().indexOf(filter) > -1) {
        createMarker(station.name, station.lat, station.lon, stationMap, stationMarkers);
      }
    });
  }
}

// --------------------------------------------------------- Connection MAP ------------------------------------- //

var connectionLines = [];
var connectionMarker = [];

function updateConnectionMap(data, $http) {
  //Get from to
  var station_from = document.getElementById("searchConnection_from").value;
  var station_to = document.getElementById("searchConnection_to").value;

  //Clear old markers and lines first
  setMapOnAll(connectionMarker, null);
  setMapOnAll(connectionLines, null);

  angular.forEach(data, function(route) {

    //Build the right url to get the stopps of this particular route
    var url = "http://localhost:8080/SA-Service/sa/routes/getstations/" + route.route_id;

    //Get the stations of each route (CAREFUL this is ASYNC!!!)
    $http.get(url).then(function(response) {
      //Used to store all vertices of the polyline
      var lineCoords = [];
      //Iterate over all stops of this route
      angular.forEach(response.data.Content, function(station) {
        if (station) {
          //Add the position to the poly line coordinates array
          var coordinates = new google.maps.LatLng(station.lat, station.lon);
          lineCoords.push(coordinates);

          //Check if its start or end position, and add a marker there
          //Todo: different colors for from and to station
          if (station.name.toLowerCase().indexOf(station_from) > -1) {
            createMarker(station.name, station.lat, station.lon, connectionMap, connectionMarker);
          } else if (station.name.toLowerCase().indexOf(station_to) > -1) {
            createMarker(station.name, station.lat, station.lon, connectionMap, connectionMarker);
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

      routeLine.setMap(connectionMap);

      //Add popup to line  //add a info windows (when you click on it) to the marker
      var infowindow = new google.maps.InfoWindow({
        content: route.name
      });

      routeLine.addListener("click", function() {
        infowindow.open(connectionMap, routeLine);
      });

      //Add it to the global list so we can remove it in the dynamic application
      connectionLines.push(routeLine);
    });
    //Do not insert code here, the call above is async!!!
  });
}

// --------------------------------------------------------- Restaurant MAP ------------------------------------- //

var restaurantMarker = [];
var circle = null;

function updateRestaurantMap(data, distance, stationName, http) {

  setMapOnAll(restaurantMarker, null);

  angular.forEach(data, function(restaurant) {
    var text = restaurant.name + "\n" + restaurant.city + " " + restaurant.housenumber;
    createMarker(text, restaurant.lat, restaurant.lon, restaurantMap, restaurantMarker);
  });

  //Get the position for the circle
  var url = "http://localhost:8080/SA-Service/sa/stations/" + stationName + "/0/1000/0/1000";

  //Get get the first station for the center of the circle
  http.get(url).then(function(response) {
    angular.forEach(data, function(station) {
      if (circle != null)
        circle.setMap(null);
      circle = new google.maps.Circle({
        center: new google.maps.LatLng(station.lat, station.lon),
        radius: 1.5 * distance * 1000,
        strokeColor: "#0000FF",
        strokeOpacity: 0.8,
        strokeWeight: 2,
        fillColor: "#0000FF",
        fillOpacity: 0.4
      });
      circle.setMap(restaurantMap);
    });
  });
}

// --------------------------------------------------------- Misc ------------------------------------- //
function createMarker(name, lat, lon, map, array) {
  var marker = new google.maps.Marker({
    position: new google.maps.LatLng(lat, lon),
  });

  //add a info windows (when you click on it) to the marker
  var infowindow = new google.maps.InfoWindow({
    content: name
  });

  marker.addListener("click", function() {
    infowindow.open(map, marker);
  });

  marker.setMap(map);
  array.push(marker);

  return marker;
}

function setMapOnAll(array, map) {
  //Clear old markers first
  for (var i = 0; i < array.length; i++) {
    array[i].setMap(map);
  }
  array.length = 0;
}
