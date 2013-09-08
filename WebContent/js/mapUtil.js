/**
 * @author zhouwubai
 */

// 

// user defined function

function changeMillisecondsToDateString(millionSeconds) {
				var myDate = new Date(millionSeconds);
				return myDate.toLocaleString().substring(0, 10);
}



function chooseNodesOfMainStoryline(events)
{
	var rtn = [];
	for(var i = 0; i < events.length; i++){
		if(events[i].isMainEvent)
			rtn.push(events[i]);
	}
	
	return rtn;
}

// pick up some default value if parameter are not given.
function pickDefaultParam(a, defaultVal)
{
  return a = typeof a !== 'undefined' ? a : defaultVal;
}





// Google Map Util Object


function GoogleMapUtilObject(){
	
	//default value
	var lineSymbol = {
			path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW
	};
	
	this.polyOptions = {
		    strokeColor: '#000000',
		    strokeOpacity: 1.0,
		    strokeWeight: 3,
		    icons: [{
		        icon: lineSymbol,
		        offset: '100%'
		      }]
		  };
	
};


GoogleMapUtilObject.prototype = {
		
		setMarkers: function(map, events) {
			//before loop, we set variable index to 0, set previous markers unattached to map
			map.clearMarkers();
			
			for ( var i = 0; i < events.length; i++) {
				var markerLatLng = new google.maps.LatLng(
						events[i].latlng.latitude,
						events[i].latlng.longtitude);
				var marker = new google.maps.Marker({ position : markerLatLng,
				map : map,
				title : events[i].eventContent,
				//icon: "http://openclipart.org/people/mightyman/green.svg",
				zIndex: google.maps.Marker.MAX_ZINDEX});
				map.markers.push(marker);
				this.attachInfoWindow(map,marker, events[i]);
			}
		},
		
		attachInfoWindow: function(map,marker,event) {
			//content displayed on infowindow of this marker
			var content = "<div id='infoDiv'>"
					+ "<br> location: <a target = '_blank' href = '" + event.eventURL + "'> "
					+ event.eventLocation + "</a>" + "<br> Latitude: "
					+ event.latlng.latitude + "<br> Longtitude: "
					+ event.latlng.longtitude + "<br> Info: " + event.eventContent
					+ "<br> Date: "
					+ changeMillisecondsToDateString(event.eventDate)
					+ "</div>";
			
			var infowindow = new google.maps.InfoWindow({ content : content,
			size : new google.maps.Size(50, 50) });
			//add listener to this marker
			google.maps.event.addListener(marker, 'click', function() {
				infowindow.open(map, marker);
			});
		},
		
		convertEventLatLng: function(event)
		{
			return new google.maps.LatLng(event.latlng.latitude,event.latlng.longtitude);
		},
		
		displayStoryline: function(map,events,polyOptions){
			
			polyOptions = pickDefaultParam(polyOptions, this.polyOptions);
			poly = new google.maps.Polyline(polyOptions);;
			poly.setMap(map);
			
			console.log(poly);
			var path = poly.getPath();
			for(var i = 0; i < events.length; i++){
				path.push(this.convertEventLatLng(events[i]));
			}
			
		},
		
		
		
		//setter and getters
		setPolyOptions: function(polyOptions)
		{
			this.polyOptions = polyOptions;
		},
		
		getPolyOptions: function()
		{
			return this.polyOptions;
		}
		
};