package fiu.kdrg.storyline.event;

import java.io.Serializable;

public class LatLng  implements Serializable{

	Float latitude;
	Float longtitude;
	
	public LatLng(){
		
	}
	
	public LatLng(Float latitude, Float longtitude) {
		// TODO Auto-generated constructor stub
		this.latitude = latitude;
		this.longtitude = longtitude;
	}

	public Float getLatitude() {
		return latitude;
	}

	public void setLatitude(Float latitude) {
		this.latitude = latitude;
	}

	public Float getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(Float longtitude) {
		this.longtitude = longtitude;
	}
	
	
	public boolean isValid() {
		 return !getLatitude().equals(0.0F) || !getLongtitude().equals(0.0F);
	}
	
	
}
