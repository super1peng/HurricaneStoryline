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

function distOfMarkerAndEvent(marker,event){
	var latDiff = marker.getPosition().lat() - event.latlng.latitude;
	var longDiff = marker.getPosition().lng() - event.latlng.longtitude;
	return Math.sqrt(Math.pow(latDiff,2) + Math.pow(longDiff,2));
}

function chooseMarkerNeighbors(marker,events,radius){
	var rtnEvents = [];
	
	for(var i = 0; i < events.length; i++){
		if(distOfMarkerAndEvent(marker, events[i]) <= radius)
			rtnEvents.push(events[i]);
	}
	return rtnEvents;
}

// pick up some default value if parameter are not given.
function pickDefaultParam(a, defaultVal)
{
  return a = typeof a !== 'undefined' ? a : defaultVal;
}


// Google Map Util Object


function FiuStorylineMapUtilObject(){
	
	this.storylineMarkers = [];
	this.mediumStorylineMarkers = [];
	this.storylinePoly = new google.maps.Polyline();
	this.mediumStorylinePoly = new google.maps.Polyline();
	//singleton InfoWindow
	this.infowindow = new google.maps.InfoWindow({size : new google.maps.Size(50, 50) });
	
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
	this.redPolyOptions = {
		    strokeColor: '#FF0000',
		    strokeOpacity: 1.0,
		    strokeWeight: 3,
		    icons: [{
		        icon: lineSymbol,
		        offset: '100%'
		      }]
		  };
	this.iconURL = "http://openclipart.org/people/mightyman/green.svg";
	
	
};


FiuStorylineMapUtilObject.prototype = {
		
		setMarkers: function(map, events) {
			//before loop, we set variable index to 0, set previous markers unattached to map
			this.clearMarkers(this.storylineMarkers);
			
			for ( var i = 0; i < events.length; i++) {
				var markerLatLng = new google.maps.LatLng(
						events[i].latlng.latitude,
						events[i].latlng.longtitude);
				var marker = new google.maps.Marker({ position : markerLatLng,
				map : map,
				title : events[i].eventContent,
				//icon: "http://openclipart.org/people/mightyman/green.svg",
				zIndex: google.maps.Marker.MAX_ZINDEX});
				
				marker.event = events[i];  //relate event with this marker.
				this.storylineMarkers.push(marker);
				
				var refThis = this;
				this.addListenerByClickMarker(marker, refThis);
				this.addListenerBydbClickMarkerToZoomIn(marker, refThis);
				
			}
		},
		
		setLayerTwoMarker: function(map,events){
			
			this.clearMarkers(this.mediumStorylineMarkers);
			
			for ( var i = 0; i < events.length; i++) {
				var markerLatLng = new google.maps.LatLng(
						events[i].latlng.latitude,
						events[i].latlng.longtitude);
				var marker = new google.maps.Marker({ position : markerLatLng,
				map : map,
				title : events[i].eventContent,
				icon: "http://openclipart.org/people/mightyman/green.svg",
				zIndex: google.maps.Marker.MAX_ZINDEX});
				
				marker.event = events[i];  //relate event with this marker.
				this.mediumStorylineMarkers.push(marker);
				
				var refThis = this;
				this.addListenerByClickMarker(marker, refThis);
				
			}
		},
		
		//to avoid issues caused by closure
		addListenerByClickMarker: function(marker, refThis){
			google.maps.event.addListener(marker, 'click', function(event) {
//				console.log(1);
//				map.panTo(marker.getPosition());
				refThis.infowindow.setContent(refThis.getInfoWindowContent(marker.event));
				refThis.infowindow.open(map,marker);
			});
		},
		
		// its better to set those function as private
		addListenerBydbClickMarkerToZoomIn:function(marker,refThis){
			google.maps.event.addListener(marker, 'dblclick', function(event) {
//				console.log(2);
				map.setZoom(7);
				map.setCenter(marker.getPosition());
				var neighbor = chooseMarkerNeighbors(marker, events, 6);
				refThis.setLayerTwoMarker(map,neighbor);
				refThis.clearPoly(refThis.storylinePoly);
				refThis.clearPoly(refThis.mediumStorylinePoly);
				refThis.displayPoly(map,neighbor,refThis.mediumStorylinePoly,refThis.redPolyOptions);
			});
		},
		
		clearMarkers: function(markers){
            for (var i = 0; i < markers.length; i++) {
                markers[i].setMap(null);
            }
            markers = [];
        },
        
        clearPoly: function(poly){
        	poly.setMap(null);
        	poly.getPath().clear();
        },
		
		attachInfoWindow: function(map,marker,event) {
			
			var infowindow = new google.maps.InfoWindow({ 
				content : this.getInfoWindowContent(event),
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
		
		displayPoly: function(map,events,poly,polyOptions){
			
			polyOptions = pickDefaultParam(polyOptions, this.polyOptions);
			poly.setOptions(polyOptions);
			poly.setMap(map);
			
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
		},
		
		getInfoWindowContent: function(event){		
			var content = "<div id='infoDiv'>"
				+ "<br> location: <a target = '_blank' href = '" + event.eventURL + "'> "
				+ event.eventLocation + "</a>" + "<br> Latitude: "
				+ event.latlng.latitude + "<br> Longtitude: "
				+ event.latlng.longtitude + "<br> Info: " + event.eventContent
				+ "<br> Date: "
				+ changeMillisecondsToDateString(event.eventDate)
				+ "</div>";
		
			return content;
		}
		
};