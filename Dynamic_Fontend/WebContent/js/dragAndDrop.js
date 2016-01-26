

$(document).ready(function() {		  
   $('.list_new_routes').sortable({connectWith: ".list_stations",
      tolerance: 'pointer',
      items: "> li:not(:first)",
      deactivate: function( event, ui ) 
      {
    	//  ui.item[0].innerHTML = ui.item[0].innerHTML  + '<a class="close" onclick="removeItem(' + ui.item[0].value + ')">×</a>';
      }
   }); 
	
   $('.list_stations').sortable({connectWith: ".list_new_routes",
	  tolerance: 'pointer'
   });
			
   $('.list_stations').bind('sortstop', function(event, ui) {
	  $(ui.item[0]).clone(true).appendTo('.list_new_routes');  
	  //$(this).sortable('cancel');
   });				
});

function removeItem(db_id){
	
	// var new_route_list = document.getElementById('id_new_route');
	// alert(new_route_list.length);
	 
	// var elem = document.getElementById('my_li');
	// elem.parentNode.removeChild(elem);
}
    