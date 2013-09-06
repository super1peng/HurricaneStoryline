/**
 * @author zhouwubai
 */

function changeMillisecondsToDateString(millionSeconds) {
				var myDate = new Date(millionSeconds);
				return myDate.toLocaleString().substring(0, 15);
}


var googleMapUtilObject = function(){
				
		return {
			
			//set markers related the events in the map when load the page
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
			
			
			//set related content to the marker
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
			}
			
		};
		
};