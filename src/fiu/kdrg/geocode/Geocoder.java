package fiu.kdrg.geocode;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import fiu.kdrg.db.DBConnection;
import fiu.kdrg.storyline.event.LatLng;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Cache in db, when mis-hit, query google
 * 
 * @author cshen001
 * 
 */
public class Geocoder {

	final protected static String PREFIXURL = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=";
	final protected static String queryAddressToDB = "SELECT Lat, Lng FROM Address WHERE Address = ?";
	final protected static String updateDBWiValidAddress = "INSERT INTO Address (Address, FormattedAddress, Lat, Lng, GoogleMapJson) VALUES (?,?,?,?,?)";
	final protected static String updateDBWiInvalidAddress = "INSERT INTO Address (Address, GoogleMapJson) VALUES (?,?)";

	static private Geocoder geocoder = null;
	
	Map<String, LatLng> cache;
	public Geocoder() {
		cache = new HashMap<String, LatLng>();
	}
	
	static public Geocoder getGeocoder() {
		if (geocoder == null) {
			geocoder = new Geocoder();
		}
		return geocoder;
	}
	
	int reqn = 0;
	
	protected LatLng getLatLngFromDB(String address) {
		LatLng geo = null;
		Connection conn = null;
		try {
			conn = DBConnection.getConnection();
			PreparedStatement ps = conn.prepareStatement(queryAddressToDB);
			ps.setString(1, address);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				float lat = rs.getFloat(1);
				float lng = rs.getFloat(2);
				geo = new LatLng(lat, lng);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return geo;
	}

	protected void saveInvalidLatLng(String address, String json) {
		Connection conn = null;
		try {
			conn = DBConnection.getConnection();
			PreparedStatement ps = conn
					.prepareStatement(updateDBWiInvalidAddress);
			ps.setString(1, address);
			ps.setString(2, json);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void saveValidLatLng(String address,
			String formattedAddress, float lat, float lng, String json) {
		Connection conn = null;
		try {
			conn = DBConnection.getConnection();
			PreparedStatement ps = conn
					.prepareStatement(updateDBWiValidAddress);
			ps.setString(1, address);
			ps.setString(2, formattedAddress);
			ps.setFloat(3, lat);
			ps.setFloat(4, lng);
			ps.setString(5, json);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public LatLng getLatLng(String address) {
		// a little preprocess
		List<String> locations = new ArrayList<String>(Arrays.asList(address.split(" \\| ")));
		locations.remove("East Coast");
		address = StringUtils.join(locations, " | ");

		LatLng geo = cache.get(address);
		if (geo != null)
			return geo;
		
		geo = getLatLngFromDB(address);
		if (geo == null) {
			try {
				String url = PREFIXURL;
				url += URLEncoder.encode(address, "utf-8");
				String jsonStr = getJsonFromURL(url);
				JsonElement root = new JsonParser().parse(jsonStr);
				String status = root.getAsJsonObject().get("status")
						.getAsString();
				
				if (!status.equals("OK")) {
					if (status.equals("OVER_QUERY_LIMIT"))
						System.err.println("OVER_QUERY_LIMIT");
					else
						saveInvalidLatLng(address, jsonStr);
				} else {
					JsonElement results = root.getAsJsonObject().get("results");
					JsonElement firstResult = results.getAsJsonArray().get(0);

					String formatAddress = firstResult.getAsJsonObject()
							.get("formatted_address").getAsString();
					Float latitude = firstResult.getAsJsonObject()
							.get("geometry").getAsJsonObject().get("location")
							.getAsJsonObject().get("lat").getAsFloat();
					Float longtitude = firstResult.getAsJsonObject()
							.get("geometry").getAsJsonObject().get("location")
							.getAsJsonObject().get("lng").getAsFloat();
					
					saveValidLatLng(address, formatAddress, latitude, longtitude, jsonStr);
					geo = new LatLng(latitude, longtitude);
				}
				reqn++;
				System.err.print("requests google api: " + reqn + "\r");
				Thread.sleep((long)Math.max(1000, Math.random() * 3));

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (geo != null) 
			cache.put(address, geo);
		
		return geo;
	}

	protected String getJsonFromURL(String url)
			throws MalformedURLException, IOException {
		InputStream is = (new URL(url)).openStream();

		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));

			StringBuilder sb = new StringBuilder();
			int ch;
			while ((ch = rd.read()) != -1) {
				sb.append((char) ch);
			}
			return sb.toString();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			is.close();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		Geocoder geocoder = Geocoder.getGeocoder();
		LatLng latlng = geocoder.getLatLng("East Coast | North Carolina | Mid-Atlantic | New England");
		if (latlng != null) {
			System.out.println(latlng.getLatitude());
			System.out.println(latlng.getLongtitude());
		}
	}
}
