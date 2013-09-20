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


function eventsToMVCArray(events){	
	var rtn = [];
	for(var i = 0; i < events.length; i++){
		var tmp = new google.maps.LatLng(events[i].latlng.latitude,events[i].latlng.longtitude);
		rtn.push(tmp);
	}
	return new google.maps.MVCArray(rtn);
}


// Google Map Util Object


function FiuStorylineMapUtilObject(){
	
	var self = this;
	
	self.storylineMarkers = [];
	self.mediumStorylineMarkers = [];
	
	
	self.storylinePoly = new google.maps.Polyline();
	self.mediumStorylinePoly = new google.maps.Polyline();
	self.heatMap = new google.maps.visualization.HeatmapLayer({});
	
	//singleton InfoWindow
	self.infowindow = new google.maps.InfoWindow({size : new google.maps.Size(50, 50) });
	self.markerCluster = {};
	
	//default value
	var lineSymbol = {
			path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW
	};
	
	self.polyOptions = {
		    strokeColor: '#000000',
		    strokeOpacity: 1.0,
		    strokeWeight: 3,
		    icons: [{
		        icon: lineSymbol,
		        offset: '100%'
		      }]
		  };
	self.redPolyOptions = {
		    strokeColor: '#FF0000',
		    strokeOpacity: 1.0,
		    strokeWeight: 3,
		    icons: [{
		        icon: lineSymbol,
		        offset: '100%'
		      }]
		  };
	
	self.markerClusterOptions = {
			gridSize: 50, maxZoom: 15
	};
	
	self.iconURL = "http://openclipart.org/people/mightyman/green.svg";
	
	
	FiuStorylineMapUtilObject.prototype.setMarkers = function(map,events){
		
		//before loop, we set variable index to 0, set previous markers unattached to map
		self.clearMarkers(self.storylineMarkers);
		
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
			self.storylineMarkers.push(marker);
			
			self.addListenerByClickMarker(marker);
			self.addListenerBydbClickMarkerToZoomIn(marker);		
		}
	};
	
	
	
	FiuStorylineMapUtilObject.prototype.setLayerTwoMarker = function(map,events){
		
		self.clearMarkers(self.mediumStorylineMarkers);
		
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
			self.mediumStorylineMarkers.push(marker);
			
			self.addListenerByClickMarker(marker);
			//var mc = new MarkerClusterer(map, this.mediumStorylineMarkers, this.markerClusterOptions);
			
		}
	};
	
	//to avoid issues caused by closure
	FiuStorylineMapUtilObject.prototype.addListenerByClickMarker = function(marker){
		google.maps.event.addListener(marker, 'click', function(event) {
//			console.log(1);
//			map.panTo(marker.getPosition());
			self.infowindow.setContent(self.getInfoWindowContent(marker.event));
			self.infowindow.open(map,marker);
		});
	};
	
	
	// its better to set those function as private
	FiuStorylineMapUtilObject.prototype.addListenerBydbClickMarkerToZoomIn = function(marker){
		google.maps.event.addListener(marker, 'dblclick', function(event) {
//			console.log(2);
			map.setZoom(6);
			map.setCenter(marker.getPosition());

			self.clearPoly(self.storylinePoly);
			self.clearPoly(self.mediumStorylinePoly);
			//refThis.displayPoly(map,neighbor,refThis.mediumStorylinePoly,refThis.redPolyOptions);
			
			var fname = "storyline" + marker.event.id + ".out";
			console.log(fname);
			$.get("LoadFinalEventServlet",{fileName:fname},function(rtnData){
				var layer2Storyline = rtnData.events;
//				console.log(layer2Storyline);
				self.heatMap.setData(eventsToMVCArray(layer2Storyline));			
				self.heatMap.setMap(map);
				
			});
		});
	};
	
	
	
	
	FiuStorylineMapUtilObject.prototype.clearMarkers = function(markers){
        for (var i = 0; i < markers.length; i++) {
            markers[i].setMap(null);
        }
        markers = [];
    };
    
    
    FiuStorylineMapUtilObject.prototype.clearPoly = function(poly){
    	poly.setMap(null);
    	poly.getPath().clear();
    };
	
    FiuStorylineMapUtilObject.prototype.attachInfoWindow = function(map,marker,event) {
		
		var infowindow = new google.maps.InfoWindow({ 
			content : self.getInfoWindowContent(event),
			size : new google.maps.Size(50, 50) });
		//add listener to this marker
		google.maps.event.addListener(marker, 'click', function() {
			infowindow.open(map, marker);
		});
	};
	
	
	
	
	FiuStorylineMapUtilObject.prototype.convertEventLatLng = function(event)
	{
		return new google.maps.LatLng(event.latlng.latitude,event.latlng.longtitude);
	};
	
	FiuStorylineMapUtilObject.prototype.displayPoly = function(map,events,poly,polyOptions){
		
		polyOptions = pickDefaultParam(polyOptions, self.polyOptions);
		poly.setOptions(polyOptions);
		poly.setMap(map);
		
		var path = poly.getPath();
		for(var i = 0; i < events.length; i++){
			path.push(self.convertEventLatLng(events[i]));
		}
		
	};
	
	
	
	
	//setter and getters
	FiuStorylineMapUtilObject.prototype.setPolyOptions = function(polyOptions)
	{
		self.polyOptions = polyOptions;
	};
	
	FiuStorylineMapUtilObject.prototype.setEvents = function(events){
		self.events = events;
	};
	
	FiuStorylineMapUtilObject.prototype.getPolyOptions = function()
	{
		return self.polyOptions;
	};
	
	FiuStorylineMapUtilObject.prototype.getInfoWindowContent = function(event){		
		var content = "<div id='infoDiv'>"
			+ "<br> location: <a target = '_blank' href = '" + event.eventURL + "'> "
			+ event.eventLocation + "</a>" + "<br> Latitude: "
			+ event.latlng.latitude + "<br> Longtitude: "
			+ event.latlng.longtitude + "<br> Info: " + event.eventContent
			+ "<br> Date: "
			+ changeMillisecondsToDateString(event.eventDate)
			+ "</div>";
	
		return content;
	};
	
};






function FiuGoogleHeatMap(){
	
	var self = this;
	
	self.heatmap = new google.maps.visualization.HeatmapLayer({});
	
	FiuGoogleHeatMap.prototype.setMap = function(map){
		self.map = map;
	};
	
	FiuGoogleHeatMap.prototype.setData = function(data){
		self.data = data;
	};
	
	FiuGoogleHeatMap.prototype.showHeatMap = function(){
		self.heatmap.setMap(self.map);
	};
	
	FiuGoogleHeatMap.prototype.toggleHeatMap = function(){
		self.heatmap.setMap(self.heatmap.getMap()? null : self.map);
	};
	
	
};























