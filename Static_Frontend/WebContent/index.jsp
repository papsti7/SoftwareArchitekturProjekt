<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<jsp:include page="/WEB-INF/util/style.jsp"></jsp:include>
</head>
<body style="background-color: light-grey">
	<div align="center">
		<div class="header">
			<h1>SA 2015</h1>
		</div>
		<div class="row" style="width: 95%;" align="center">
			<div class="col-sm-5" style="background-color: lavender;">

				<jsp:include page="/WEB-INF/searchtab.jsp"></jsp:include>
				<jsp:include page="/WEB-INF/map.jsp"></jsp:include>

			</div>
			<div class="col-sm-7" style="background-color: lavender; min-height: 570px;">
				<jsp:include page="/WEB-INF/table.jsp"></jsp:include>
			</div>
		</div>
	</div>	
	
<script>
	if('${type}' == '0' ){
	  $('.nav a[href="#stations"]').tab('show');
	  document.getElementById('name').value = '${name}';
	  document.getElementById('lon_min').value = '${lon_min}';
	  document.getElementById('lon_max').value = '${lon_max}';
	  document.getElementById('lat_min').value = '${lat_min}';
	  document.getElementById('lat_max').value = '${lat_max}';
	}
	else if('${type}' == '1' ){
	  $('.nav a[href="#connections"]').tab('show');
	  document.getElementById('name_from').value = '${name_from}';
	  document.getElementById('name_to').value = '${name_to}';
	}
	else if('${type}' == '2' ){
	  $('.nav a[href="#restaurants"]').tab('show');
	  document.getElementById('stationname').value = '${name}';
	  document.getElementById('distance').value = '${distance}';
	}
	else{
	  $('.nav a[href="#stations"]').tab('show');
	}
</script>
	
</body>
</html>
