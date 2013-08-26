package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fiu.kdrg.storyline.StorylineGen;
import fiu.kdrg.storyline.StorylineGenBaseline;
import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.IOUtil;
import fiu.kdrg.util.Util;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * Servlet implementation class GetImageServlet
 */
@WebServlet("/GetImageServlet")
public class GetEventsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	    
//		String path = "U:/Research/BCIN/ADSE/StorylineData/data/eventData/multi_Hurricane_Katrina.txt";
		//String path = "/home/zhouwubai/Research/BCIN/ADSE/sandy_all_clean_nodup_events_latlng.txt";
		String path = Util.rootDir + "sandy_all_clean_nodup_events_latlng.txt";
		List<Event> eventArray = new ArrayList<Event>();
		eventArray = EventUtil.sortEventByDate(IOUtil.parseFileTOEvents(path));
		
		try {
			Gson gson = new Gson();
			JsonObject ret = new JsonObject();
			JsonElement allEventsJson = gson.toJsonTree(eventArray);
			ret.add("allevents", allEventsJson);
			
			StorylineGenBaseline storylineGen = new StorylineGenBaseline();
			storylineGen.loadEvents(path, 
					StorylineGenBaseline.dateFormat.parse("2012-10-24"), StorylineGenBaseline.dateFormat.parse("2012-11-06"), "sandy|hurricane|storm|disaster");
			List<Event> storyline = storylineGen.getStoryline();
			
			JsonElement storylineJson = gson.toJsonTree(storyline);
			
			ret.add("storyline", storylineJson);
			
		    response.setContentType("application/json; Charset-utf-8");
		    response.setHeader("pragma", "no-cache");
		    response.setHeader("cache-control", "no-cache");
		    
		    JsonWriter jw = new JsonWriter(response.getWriter());
		    gson.toJson(ret, jw);
		    jw.flush();
	    
	    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	}

}
