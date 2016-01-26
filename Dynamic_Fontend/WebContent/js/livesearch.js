var app = angular.module("liveSearchApp", ["xeditable"]);

app.run(function(editableOptions) {
  editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

app.controller('stationCtrl', function($scope, $http, $location, $anchorScroll) {
  //Variables used by the filtering/ordering
  $scope.stations = [];
  $scope.restaurants = [];
  $scope.sortType = "name";
  $scope.sortReverse = false;
  $scope.searchName = "";

  //Store the singleton so we can access it in the updateData function
  $scope.http = $http;

  //Updatedata function, called when we enter text in searchStation input field
  $scope.updateData = function($scope) {

    // clear feedback panel
    document.getElementById("feedback_div").innerHTML = '';

    //Check if the text is longer than 3, so we can take some action
    var searchBar = document.getElementById("searchStation");
    updateStationMap($scope.stations);
    if (searchBar != null && searchBar.value.length == 3) {

      var url = "http://localhost:8080/SA-Service/sa/stations/" + searchBar.value + "/0/1000/0/1000";
      console.log("Update with url: " + url);
      //Access the previous stored singleton
      $scope.http.get(url)
        .then(function(response) {
          $scope.stations = response.data.Content;
          //Prepare the suggestions
          var options = "";
          angular.forEach(response.data.Content, function(station) {
            //Only add it to the options if its not already added!
            if (options.indexOf(station.name) == -1) {
              //Add the html option tag
              options += '<option value="' + station.name + '" />';
            }
          });
          //Replace the old options of the optionlist
          var search_hint_list = document.getElementById("station_list");
          search_hint_list.innerHTML = options;

          updateStationMap($scope.stations);
        });
    }
  };
  $scope.saveStation = function($scope, id, name, lon, lat) {
    //$scope.station updated yet
    console.log("Update with id: " + id);
    console.log("Update with name: " + name);
    console.log("Update with lon: " + lon);
    console.log("Update with lat: " + lat);
    var Jsonobj = '{"name" : "' + name + '", "lon" : ' + parseFloat(lon) + ', "lat" : ' + parseFloat(lat) + '}';
    console.log(Jsonobj);
    var url = "http://localhost:8080/SA-Service/sa/stations/" + id;
    console.log(url);
    $scope.http({
        url: url,
        method: "PUT",
        data: JSON.parse(Jsonobj),
        headers: {
          'Content-Type': 'application/json'
        }
      })
      .then(function(response) {
          // success
          console.log("Success");
        },
        function(response) { // optional
          // failed
          console.log("failed with: " + response);
        });
  };

  // remove user
  $scope.removeStation = function($scope, station) {
    console.log("Update with id: " + station.id);
    console.log("index: " + station.index);
    console.log("Update with name: " + station.name);
    console.log("Update with lon: " + station.lon);
    console.log("Update with lat: " + station.lat);
    var url = "http://localhost:8080/SA-Service/sa/stations/" + station.id;
    console.log(url);
    $scope.http({
        url: url,
        method: "DELETE"

      })
      .then(function(response) {
          // success
          console.log("Success deleted");
        },
        function(response) { // optional
          // failed
          console.log("failed deleting with: " + response);
        });
    //var index = $scope.stations.indexOf(station);
    //$scope.stations.splice(index, 1);
  };

  // add user
  $scope.addStation = function(index, $scope) {
    var name = document.getElementById('adding_name').value;
    var lon = document.getElementById('adding_lon').value;
    var lat = document.getElementById('adding_lat').value;

    console.log("Update with name: " + name);
    console.log("Name size: " + name.length);
    console.log("Update with lon: " + lon);
    console.log("Update with lat: " + lat);
    if (name.length < 3) {
      alert("Please enter more than 3 charaters to the name");
      return;
    }
    var url = "http://localhost:8080/SA-Service/sa/stations/";
    console.log(url);
    var Jsonobj = '{"name" : "' + name + '", "lon" : ' + parseFloat(lon) + ', "lat" : ' + parseFloat(lat) + '}';
    console.log(Jsonobj);
    $scope.http({
        url: url,
        method: "POST",
        data: JSON.parse(Jsonobj),
        headers: {
          'Content-Type': 'application/json'
        }
      })
      .then(function(response) {
          // success
          console.log("Success adding");
        },
        function(response) { // optional
          // failed
          console.log("failed adding with: " + response);
        });

    //$scope.stations.push({"id" : 0, "name" : name, "lon" : lon, "lat" : lat});

    $scope.adding_name = "";
    $scope.adding_lon = "";
    $scope.adding_lat = "";

    return;
  };

  $scope.clearAdding = function() {
    $scope.adding_name = "";
    $scope.adding_lon = "";
    $scope.adding_lat = "";

    return;
  }

  // -------------------------------- CONNECTION SEARCH ------------------------------------ //

  $scope.connections = [];
  $scope.toDeleteConnection; //AddRoute will check if the name is the same, if so addroute will delete the route first, and then reupload

  $scope.searchConnection = function($scope) {

    if (searchConnection_from != null && searchConnection_to != null) {

      var url = "http://localhost:8080/SA-Service/sa/routes/getconnections/" + searchConnection_from.value + "/" + searchConnection_to.value;
      console.log("Update with url: " + url);
      //Access the previous stored singleton
      $scope.http.get(url)
        .then(function(response) {
          $scope.connections = response.data.Content;
          updateConnectionMap(response.data.Content, $scope.http);
        });
    }
  };

  $scope.removeConnection = function($scope, connection) {
    var url = "http://localhost:8080/SA-Service/sa/routes/" + connection.route_id;
    console.log("Delete connection: " + url);
    $scope.http({
        url: url,
        method: "DELETE"
      })
      .then(function(response) {
          // success
          console.log("Success deleted");
        },
        function(response) { // optional
          // failed
          console.log("failed deleting with: " + response);
        });
    var index = $scope.connections.indexOf(connection);
    $scope.connections.splice(index, 1);
  };

  $scope.editConnection = function($scope, connection) {
    var url = "http://localhost:8080/SA-Service/sa/routes/getstations/" + connection.route_id;
    console.log("Update with url: " + url);

    //Set the edit name
    $scope.toDeleteConnectionID = connection.route_id;
    $scope.toDeleteConnectionName = connection.name;

    //Clear the ul
    $(".list_new_routes").empty();
    document.getElementById("id_new_route").innerHTML = '<li class="list-group-item list-group-item-info"> <h4>Drop Stations here</h4></li>';

    //Set the name of the route
    document.getElementById("route_name").value = connection.name;

    //Access the previous stored singleton
    $scope.http.get(url)
      .then(function(response) {
        var stops = response.data.Content;

        angular.forEach(stops, function(stop) {
          //Add the station to the list of selected stops
          console.log("Stop: " + stop.name);
          var element = '<li class="list-group-item" value="' + stop.id + '">' + stop.name + ' ' + stop.lat + ' ' + stop.lon + '</li>';
          console.log("Element: " + element);
          $(".list_new_routes").append(element);
        });
      });
  }


  // -------------------------------- Add new route ------------------------------------ //

  // save new Route
  $scope.saveRoute =
    function($scope) {

      var route_name = document.getElementById("route_name").value;

      if (!route_name) {
        document.getElementById("feedback_div").innerHTML = '<div class="panel panel-danger"><div class="panel-heading">Please enter a Routename!</div></div>';
        return;
      }

      var nums = document.getElementById("id_new_route");
      var listItem = nums.getElementsByTagName("li");
      console.log("Number of elements: " + listItem);

      var JSONString = '';

      if (listItem.length <= 2) {
        document.getElementById("feedback_div").innerHTML = '<div class="panel panel-danger"><div class="panel-heading">Please enter at least two Stations!</div></div>';
        return;
      }

    /*  //Try to delete a route with this name
      var url = "http://localhost:8080/SA-Service/sa/routes/byName/" + route_name;
      console.log("Delete connection: " + url);
      $scope.http({
          url: url,
          method: "DELETE"
        })
        .then(function(response) {
          // success
          console.log("Success deleted");
        });
*/
      for (var i = 1; i < listItem.length; i++) {
        var num = parseInt(listItem[i].value);

        if (i == 1) {
          JSONString = JSONString + '[' + num;
        } else {
          JSONString = JSONString + ',' + num;
        }
      }
      JSONString = JSONString + ']';
      console.log(JSONString);

      // send data to Backend
      var url = "http://localhost:8080/SA-Service/sa/routes/";
      console.log("URL: " + url);
      console.log("Stations: " + JSONString);

      $scope.http({
          url: url,
          method: "POST",
          data: {
            'name': route_name,
            'stations': JSON.parse(JSONString)
          },
          headers: {
            'Content-Type': 'application/json'
          }
        })
        .then(function(response) {
            // success
            document.getElementById("feedback_div").innerHTML = '<div class="panel panel-success"><div class="panel-heading">Success create Route</div></div>';

            // clear Stationslist and Routename
            document.getElementById("id_new_route").innerHTML = '<li class="list-group-item list-group-item-info"> <h4>Drop Stations here</h4></li>';
            document.getElementById("route_name").value = '';
            console.log("Added route");

          },
          function(response) {
            // failed
            document.getElementById("feedback_div").innerHTML = '<div class="panel panel-success"><div class="panel-heading">Success create Route</div></div>';
            console.log("Failed to add route");
          });
    };



  //Updatedata function, called when we enter text in searchStation input field
  $scope.updateNearRestaurantData = function($scope) {
    // clear feedback panel
    //document.getElementById("feedback_div").innerHTML = '';

    //Check if the text is longer than 3, so we can take some action
    var searchBar = document.getElementById("searchStation");
    var distance = document.getElementById("distance");
    if (searchBar != null && distance != null) {

      var url = "http://localhost:8080/SA-Service/sa/restaurants/byNameAndDistance/" + searchBar.value + "/" + distance.value;
      console.log("Update with url: " + url);
      //Access the previous stored singleton
      $scope.http.get(url)
        .then(function(response) {
          $scope.restaurants = response.data.Content;
          updateRestaurantMap(response.data.Content, distance.value, searchBar.value, $scope.http);
        }, function(response) {});
    }
  };

});
