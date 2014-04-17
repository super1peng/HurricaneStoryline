/**
 * @author zhouwubai
 */

// user defined function
function loadFinalEvents(id) {

	var mapUtil = new FiuStorylineMapUtilObject();
	mapInitialize();
	console.log(mapUtil);
	
	$.get("LoadFinalEventServlet", {
		fileName : "allEvents" + id + ".out"
	}, function(rtnData) {
		var events = rtnData.events;
		mapUtil.setAllEvents(events);
		
		$('.showAll').unbind("click");
		$('.showAll').click(function() {
			var markers = convertEvents2Markers(map, events);
			mapUtil.setAllMarkers(markers);
		});
		
		$('.clearAll').unbind("click");
		$('.clearAll').click(function() {
			mapUtil.clearMarkers(mapUtil.allMarkers);
		});
	});
	

	$.get("LoadFinalEventServlet", {
		fileName : "finalResult" + id + ".out"
	}, function(rtnData) {

		var events = rtnData.events;
		mapUtil.setDominateEvents(events);
		
		$('.showDom').unbind("click");
		$('.showDom').click(function() {
			var markers = convertEvents2Markers(map, events);
			mapUtil.setDominateMarkers(markers);
		});
		
		$('.clearDom').unbind("click");
		$('.clearDom').click(function() {
			mapUtil.clearMarkers(mapUtil.dominateMarkers);
		});
		
		$('.showStorylinePath').unbind("click");
		$('.showStorylinePath').click(function() {
			console.log(mapUtil);
			var storylineEvents = chooseNodesOfMainStoryline(events);
			updateContentOfStoryPanel(storylineEvents);
			var markers = convertEvents2Markers(map, storylineEvents);
			mapUtil.setStorylineMarkers(markers);
			mapUtil.displayStorylinePoly(map, storylineEvents);
		});

	}, 'json');

}

function changeMillisecondsToDateString(millionSeconds) {
	var myDate = new Date(millionSeconds);
	return myDate.toLocaleString().substring(0, 10);
}

function chooseNodesOfMainStoryline(events) {
	var rtn = [];
	for (var i = 0; i < events.length; i++) {
		if (events[i].isMainEvent)
			rtn.push(events[i]);
	}

	return rtn;
}


function pickDefaultParam(a, defaultVal) {
	return a = typeof a !== 'undefined' ? a : defaultVal;
}


//some function related google map
function getInfoWindowContent(event) {
	var content = "<div id='infoDiv'>"
			+ "<br> location: <a target = '_blank' href = '"
			+ event.eventURL + "'> " + event.eventLocation + "</a>"
			+ "<br> Latitude: " + event.latlng.latitude
			+ "<br> Longtitude: " + event.latlng.longtitude + "<br> Info: "
			+ event.eventContent + "<br> Date: "
			+ changeMillisecondsToDateString(event.eventDate) + "</div>";

	return content;
}


function attachInfoWindow(map, marker, event) {

	var infowindow = new google.maps.InfoWindow({
		content : getInfoWindowContent(event),
		size : new google.maps.Size(50, 50)
	});
	// add listener to this marker
	google.maps.event.addListener(marker, 'click', function() {
		infowindow.open(map, marker);
	});
}



function convertEventLatLng(event) {
	return new google.maps.LatLng(event.latlng.latitude,
			event.latlng.longtitude);
};


function eventsToMVCArray(events) {
	var rtn = [];
	for (var i = 0; i < events.length; i++) {
		rtn.push(convertEventLatLng(event[i]));
	}
	return new google.maps.MVCArray(rtn);
}



function convertEvents2Markers(map, events) {

	var rtnMarkers = [];
	for (var i = 0; i < events.length; i++) {
		var markerLatLng = convertEventLatLng(events[i]);
		var marker = new google.maps.Marker({
			position : markerLatLng,
			map : map,
			title : events[i].eventContent,
			zIndex : google.maps.Marker.MAX_ZINDEX
		});
		
		marker.event = events[i]; // relate event with this marker.
		rtnMarkers.push(marker);
	}
	return rtnMarkers;
}





//for hidable
function updateContentOfStoryPanel(events) {

	var storyPanel = {};
	var leftN = Math.ceil(events.length / 2);
	var eventL = events.slice(0, leftN);
	var eventR = events.slice(leftN);

	var leftHtml = "";
	for (var i = 0; i < eventL.length; i++) {
		leftHtml += "<li class='eventID_'" + events[i].id + ">" + (i + 1)
				+ ". <a href=#>" + events[i].eventContent + "</a></li>";
	}
	storyPanel.leftHtml = '<ul style="list-style-type:none">' + leftHtml
			+ '</ul>';

	var rightHtml = "";
	for (var i = 0; i < eventR.length; i++) {
		rightHtml += "<li class='eventID_'" + events[i + leftN].id + ">"
				+ (i + leftN + 1) + ". <a href=#>"
				+ events[i + leftN].eventContent + "</a></li>";
	}
	storyPanel.rightHmtl = '<ul style="list-style-type:none">' + rightHtml
			+ '</ul>';

	$('.storyline_left').html(storyPanel.leftHtml);
	$('.storyline_right').html(storyPanel.rightHmtl);
}



function setHidableHeight(height) {
	$('.hidable').css('height', height);
	$('.hidable').css('bottom', height);
	$('.storylinePanel').css('height', height);
	$('.storyline_left').css('height', height);
	$('.storyline_right').css('height', height);
}


function clickHidableToggler() {
	var current = $('.hidable_toggler_btn em').attr('class');
	var flag = (current.trim() == "control_arrow_down");
	if (flag) {
		$('.hidable_toggler_btn em').removeClass("control_arrow_down");
		$('.hidable_toggler_btn em').addClass("control_arrow_up");
		setHidableHeight(0);
	} else {
		$('.hidable_toggler_btn em').removeClass("control_arrow_up");
		$('.hidable_toggler_btn em').addClass("control_arrow_down");
		setHidableHeight(250);
	}
}



// singleton InfoWindow
var infowindow = new google.maps.InfoWindow({
	size : new google.maps.Size(50, 50)
});
function FiuStorylineMapUtilObject() {

	var self = this;
	
	self.allEvents = [];
	self.dominateEvents = [];
	self.storylineEvents = [];

	self.allMarkers = [];
	self.dominateMarkers = [];
	self.storylineMarkers = [];
	self.mediumStorylineMarkers = [];

	self.storylinePoly = new google.maps.Polyline();
	self.mediumStorylinePoly = [];
	
	// default value
	var lineSymbol = {
		path : google.maps.SymbolPath.FORWARD_CLOSED_ARROW
	};

	self.polyOptions = {
		strokeColor : '#000000',
		strokeOpacity : 1.0,
		strokeWeight : 3,
		icons : [ {
			icon : lineSymbol,
			offset : '100%'
		} ]
	};
	
	self.redPolyOptions = {
		strokeColor : '#FF0000',
		strokeOpacity : 1.0,
		strokeWeight : 3,
		icons : [ {
			icon : lineSymbol,
			offset : '100%'
		} ]
	};

	self.iconURL = "http://openclipart.org/people/mightyman/green.svg";

	
	// to avoid issues caused by closure
	FiuStorylineMapUtilObject.prototype.addListenerByClickMarker = function(marker) {
		google.maps.event.addListener(marker, 'click',function(event) {
			infowindow.setContent(getInfoWindowContent(marker.event));
			infowindow.open(map, marker);
		});
	};

	
	FiuStorylineMapUtilObject.prototype.clearAll = function(){
		self.clearMarkers(self.allMarkers);
		self.clearMarkers(self.dominateMarkers);
		self.clearMarkers(self.storylineMarkers);
		self.clearPoly(self.storylinePoly);
	};
	
	
	FiuStorylineMapUtilObject.prototype.clearMarkers = function(markers) {
		for (var i = 0; i < markers.length; i++) {
			markers[i].setMap(null);
		}
		markers = [];
	};

	
	FiuStorylineMapUtilObject.prototype.clearPoly = function(poly) {
		poly.setMap(null);
		poly.getPath().clear();
	};


	FiuStorylineMapUtilObject.prototype.displayStorylinePoly = function(map, events,polyOptions) {

		self.clearPoly(self.storylinePoly);
		
		polyOptions = pickDefaultParam(polyOptions, self.polyOptions);
		self.storylinePoly.setOptions(polyOptions);
		self.storylinePoly.setMap(map);

		var path = self.storylinePoly.getPath();
		for (var i = 0; i < events.length; i++) {
			path.push(convertEventLatLng(events[i]));
		}
	};


	//setters
	FiuStorylineMapUtilObject.prototype.setAllEvents = function(events) {
		self.allEvents = events;
	};
	
	FiuStorylineMapUtilObject.prototype.setDominateEvents = function(events){
		self.dominateEvents = events;
	};
	
	FiuStorylineMapUtilObject.prototype.setStorylineEvents = function(events){
		self.storylineEvents = events;
	};
	
	FiuStorylineMapUtilObject.prototype.setAllMarkers = function(markers){
		self.allMarkers = markers;
		self.addClickListener(markers);
	};
	
	FiuStorylineMapUtilObject.prototype.setDominateMarkers = function(markers){
		self.dominateMarkers = markers;
		self.addClickListener(markers);
	};
	
	FiuStorylineMapUtilObject.prototype.setStorylineMarkers = function(markers){
		self.storylineMarkers = markers;
		self.addClickListener(markers);
	};
	
	FiuStorylineMapUtilObject.prototype.setMediumStorylineMarkers = function(markers){
		self.mediumStorylineMarkers = markers;
		self.addClickListener(markers);
	};
	
	FiuStorylineMapUtilObject.prototype.addClickListener = function(markers){
		for(var i = 0; i < markers.length; i++){
			self.addListenerByClickMarker(markers[i]);
		}
	};
};
