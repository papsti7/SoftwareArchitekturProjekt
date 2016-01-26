<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<ul class="nav nav-tabs">
	<li class="active"><a data-toggle="tab" href="#stations">Stations</a></li>
	<li><a data-toggle="tab" href="#connections">Connections</a></li>
	<li><a data-toggle="tab" href="#restaurants">Restaurants</a></li>
</ul>

<div class="tab-content">
	<div id="stations" class="tab-pane fade in active">
		<form action="searchStations" method="post">

			<label for="name" class="col-sm-2 control-label">Name: </label>
			<div class="col-sm-10">
				<input type="text" class="form-control" id="name" name="name"
					placeholder="Jakominiplatz" value="">
			</div>


			<label for="lon" class="col-sm-2 control-label">Lon: </label>
			<div class="row">
				<div class="col-lg-4">
					<input type="number" class="form-control inline" id="lon_min"
						name="lon_min" placeholder="min" value="0">
				</div>
				<div class="col-lg-4">
					<input type="number" class="form-control inline" id="lon_max"
						name="lon_max" placeholder="max" value="1000">
				</div>
			</div>

			<label for="lat" class="col-sm-2 control-label">Lat: </label>
			<div class="row">
				<div class="col-lg-4">
					<input type="number" class="form-control inline" id="lat_min"
						name="lat_min" placeholder="min" value="0">
				</div>
				<div class="col-lg-4">
					<input type="number" class="form-control inline" id="lat_max"
						name="lat_max" placeholder="max" value="1000">
				</div>
			</div>
			<button type="submit" id="test"
				class="login dropdown-toggle loginmodal-submit"
				style="width: 150px; border-radius: 6px;">
				<span class="glyphicon glyphicon-search"></span> Search
			</button>

		</form>
	</div>
	<div id="connections" class="tab-pane fade">
		<form action="searchConnection" method="post">

			<label for="name" class="col-sm-2 control-label">From: </label>
			<div class="col-sm-10">
				<input type="text" class="form-control" id="name_from"
					name="name_from" placeholder="Jakominiplatz" value="">
			</div>
			<label for="name" class="col-sm-2 control-label">To: </label>
			<div class="col-sm-10">
				<input type="text" class="form-control" id="name_to" name="name_to"
					placeholder="Hauptplatz" value="">
			</div>

			<button type="submit" id="test"
				class="login dropdown-toggle loginmodal-submit"
				style="width: 150px; border-radius: 6px;">
				<span class="glyphicon glyphicon-search"></span> Search
			</button>

		</form>
	</div>
	<div id="restaurants" class="tab-pane fade">
		<form action="searchRestaurants" method="post">

<!-- 		<label for="id" class="col-sm-2 control-label">ID: </label>
			<div class="col-sm-10">
				<input type="text" class="form-control" id="id" name="id"
					placeholder="Area5" value="">
			</div>
 -->
 
 			<label for="name" class="col-sm-2 control-label">Stat.Name: </label>
			<div class="col-sm-10">
				<input type="text" class="form-control" id="stationname" name="stationname"
					placeholder="Jakomini" value="">
			</div>
 
			<label for="distance" class="col-sm-2 control-label">Distance: </label>
			<div class="col-sm-10">
				<input type="text" class="form-control" id="distance" name="distance"
					placeholder="0.1" value="">
			</div>

			<button type="submit" id="test"
				class="login dropdown-toggle loginmodal-submit"
				style="width: 150px; border-radius: 6px;">
				<span class="glyphicon glyphicon-search"></span> Search
			</button>

		</form>
	</div>
</div>