/**
 * @author zhouwubai
 */

// 

// user defined function

/**
 * function that change milliseconds to dataString
 */
function changeMillisecondsToDateString(millionSeconds) {
				var myDate = new Date(millionSeconds);
				return myDate.toLocaleString().substring(0, 10);
}

/**
 * function that choose nodes belongs to storyline
 * @param events
 * @returns {Array}
 */
function chooseNodesOfMainStoryline(events)
{
	var rtn = [];
	for(var i = 0; i < events.length; i++){
		if(events[i].isMainEvent)
			rtn.push(events[i]);
	}
	
	return rtn;
}


/**
 * distance between a marker and event
 * @param marker
 * @param event
 * @returns
 */
function distOfMarkerAndEvent(marker,event){
	var latDiff = marker.getPosition().lat() - event.latlng.latitude;
	var longDiff = marker.getPosition().lng() - event.latlng.longtitude;
	return Math.sqrt(Math.pow(latDiff,2) + Math.pow(longDiff,2));
}


/**
 * choose neighbor events of a marker
 * @param marker
 * @param events candidate events.
 * @param radius
 * @returns {Array} array that contains events that near marker
 */
function chooseMarkerNeighbors(marker,events,radius){
	var rtnEvents = [];
	
	for(var i = 0; i < events.length; i++){
		if(distOfMarkerAndEvent(marker, events[i]) <= radius)
			rtnEvents.push(events[i]);
	}
	return rtnEvents;
}

/**
 * if a parameter is not set, then choose a defaultVal
 * @param a
 * @param defaultVal
 * @returns
 */
function pickDefaultParam(a, defaultVal)
{
  return a = typeof a !== 'undefined' ? a : defaultVal;
}


/**
 * convert events' location to a MVCArray
 * @param events
 * @returns {google.maps.MVCArray}
 */
function eventsToMVCArray(events){	
	var rtn = [];
	for(var i = 0; i < events.length; i++){
		var tmp = new google.maps.LatLng(events[i].latlng.latitude,events[i].latlng.longtitude);
		rtn.push(tmp);
	}
	return new google.maps.MVCArray(rtn);
}


/**
 * 
 * @param event
 */
function updateContentOfStoryPanel(event){
	
	var storyPanel = {};
	leftN = Math.ceil(event.length / 2);
	eventL = event.slice(0,leftN);
	eventR = event.slice(leftN);
	
	var leftHtml = "";
	for(var i=0;i<eventL.length;i++){
		leftHtml += "<li class='eventID_'"+event[i].id+">"+(i+1)+". <a href=#>"+event[i].eventContent+"</a></li>";
	}
	storyPanel.leftHtml = '<ul style="list-style-type:none">'+leftHtml+'</ul>';
	
	var rightHtml = "";
	for(var i=0;i<eventR.length;i++){
		rightHtml += "<li class='eventID_'"+event[i+leftN].id+">"+(i+leftN+1)+". <a href=#>"+event[i+leftN].eventContent+"</a></li>";
	}
	storyPanel.rightHmtl = '<ul style="list-style-type:none">'+rightHtml+'</ul>';
	
	$('.storyline_left').html(storyPanel.leftHtml);
	$('.storyline_right').html(storyPanel.rightHmtl);
}



function setHidableHeight(height){
	$('.hidable').css('height',height);
	$('.hidable').css('bottom',height);
	$('.storylinePanel').css('height',height);
	$('.storyline_left').css('height',height);
	$('.storyline_right').css('height',height);
}


/**
 * toggle hidable area.
 */
function clickHidableToggler(){
	var current = $('.hidable_toggler_btn em').attr('class');
	var flag = (current.trim() == "control_arrow_down");
	if(flag){
		$('.hidable_toggler_btn em').removeClass("control_arrow_down");
		$('.hidable_toggler_btn em').addClass("control_arrow_up");
		setHidableHeight(0);
	}else{
		$('.hidable_toggler_btn em').removeClass("control_arrow_up");
		$('.hidable_toggler_btn em').addClass("control_arrow_down");
		setHidableHeight(250);
	}
}
//user defined object
// Google Map Util Object


function FiuStorylineMapUtilObject(){
	
	var self = this;
	
	self.storylineMarkers = [];
	self.mediumStorylineMarkers = [];
	
	
	self.storylinePoly = new google.maps.Polyline();
	self.mediumStorylinePoly = new google.maps.Polyline();
	self.heatMap = new google.maps.visualization.HeatmapLayer({radius:20});
	
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
			
			updateContentOfStoryPanel(events);
			
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
			
			var heatEvents = chooseMarkerNeighbors(marker,self.allEvents,4);
			console.log(heatEvents.length);
			self.heatMap.setData(eventsToMVCArray(heatEvents));			
			self.heatMap.setMap(map);
			
			var fname = "storyline" + marker.event.id + ".out";
			console.log(fname);
			$.get("LoadFinalEventServlet",{fileName:fname},function(rtnData){
				var layer2Storyline = rtnData.events;
//				console.log(layer2Storyline);
				updateContentOfStoryPanel(layer2Storyline);
				
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
	
	
	FiuStorylineMapUtilObject.prototype.setAllEvents = function(events){
		self.allEvents = events;
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
