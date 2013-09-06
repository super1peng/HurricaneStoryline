/**
 * @author zhouwubai
 */

function changeMillisecondsToDateString(millionSeconds) {
	var myDate = new Date(millionSeconds);
	return myDate.toLocaleString().substring(0, 15);
}


//set markers related the events in the map when load the page
function setMarkers(map, eventsArray, storyline) {
	//before loop, we set variable index to 0, set previous markers unattached to map
	map.clearMarkers();
	for ( var i = 0; i < eventsArray.length; i++) {
		var markerLatLng = new google.maps.LatLng(
				eventsArray[i].latlng.latitude,
				eventsArray[i].latlng.longtitude);
		var marker = new google.maps.Marker({ position : markerLatLng,
		map : map,
		title : eventsArray[i].eventFormatAddress });
		console.log(marker.getZIndex());
		map.markers.push(marker);
		attachSecreteMessage(marker, eventsArray[i]);
	}
	
	for ( var i = 0; i < storyline.length; i++) {
		var markerLatLng = new google.maps.LatLng(
			storyline[i].latlng.latitude,
			storyline[i].latlng.longtitude);
		var marker = new google.maps.Marker({ position : markerLatLng,
		map : map,
		title : eventsArray[i].eventFormatAddress,
		icon: "http://openclipart.org/people/mightyman/green.svg",
		zIndex: google.maps.Marker.MAX_ZINDEX});
		console.log(marker.getZIndex());
		map.markers.push(marker);
		attachSecreteMessage(marker, storyline[i]);
	}
}



//set related content to the marker
function attachSecreteMessage(marker, event) {
	//content displayed on infowindow of this marker
	var content = "<div id='infoDiv'>"
			+ "<br> location: <a target = '_blank' href = '" + event.eventURL + "'> "
			+ event.eventLocation + "</a>" + "<br> Latitude: "
			+ event.latlng.latitude + "<br> Longtitude: "
			+ event.latlng.longtitude + "<br> Info: " + event.eventContent
			+ "<br> Date: "
			+ changeMillisecondsToDateString(event.eventDate)
			+ "<div><img src='./img/800px-Katrina_2005_track.jpg'>"
			+ "</div>";
	var infowindow = new google.maps.InfoWindow({ content : content,
	size : new google.maps.Size(50, 50) });
	//add listener to this marker
	google.maps.event.addListener(marker, 'click', function() {
		infowindow.open(map, marker);
	});
}