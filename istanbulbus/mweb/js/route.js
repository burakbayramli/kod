$().ready(function() {
  all_routes();
  $("#from").setOptions({
     max: 20
  });              
  $("#to").setOptions({
     max: 20
  });
  $('#find_route').click(function(){
      find_route();
  });  
});      
function all_routes() {
  $.getJSON('/route_names', {},
    function(data){
       $("#from").focus().autocomplete(data);
       $("#to").focus().autocomplete(data);
  });        
}      
function find_route() {
  from = $("#from").val();
  to = $("#to").val();
  $("#loading").show();
  $.getJSON('/calculate_route', {'from': from, 'to': to},
    function(data){
      html = "<div>";
      for (var i=0;i<data.length;i++){
	if (data[i] != null) {
	  for (var j=0; j<data[i].length; j++){
	    html += "Bus # " + 
	      "<a href='http://www.iett.gov.tr/saat/orer.php?hid=hat&hatcode=" + data[i][j][2] + "' target='_blank'>" +  data[i][j][2] +  "</a> at " + 
	      data[i][j][1] + ", " +
	      data[i][j][3] + " stops " +
	      "<a href='http://harita.iett.gov.tr/?hat=" + data[i][j][2] + "' target='_blank'>[map]</a>" ;
	    html += "<br/>"; 
	  }
	  html += "<br/>";	  
	}
      }
      html += "</div>";
      $("#results").html(html);
      $("#loading").hide();
    }).complete(function() { $("#loading").hide(); });
  
      
}      
