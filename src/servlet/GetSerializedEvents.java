package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import fiu.kdrg.storyline.StorylineGenBaseline;
import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.IOUtil;
import fiu.kdrg.util.Util;

/**
 * Servlet implementation class GetSerializedEvents
 */
@WebServlet("/GetSerializedEvents")
public class GetSerializedEvents extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSerializedEvents() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path = Util.rootDir + "events/storyline.out1";
		List<Event> eventArray = null;
		try {
			eventArray = (List<Event>) SerializeFactory.deSerialize(path);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Gson gson = new Gson();
			JsonObject ret = new JsonObject();
			JsonElement allEventsJson = gson.toJsonTree(eventArray);
			ret.add("allevents", allEventsJson);
			
			List<Event> storyline = new ArrayList<Event>(); //it is empty anyway
			
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
