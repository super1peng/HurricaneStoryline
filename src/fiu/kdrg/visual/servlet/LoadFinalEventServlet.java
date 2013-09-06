package fiu.kdrg.visual.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.Util;

/**
 * Servlet implementation class LoadFinalEventServlet
 */
@WebServlet("/LoadFinalEventServlet")
public class LoadFinalEventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoadFinalEventServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		ArrayList<Event> filterEvents = null;
		try {
			filterEvents = (ArrayList<Event>) SerializeFactory.deSerialize(Util.rootDir + "finalResult.out");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Gson gson = new Gson();
		JsonObject ret = new JsonObject();
		JsonElement storylineJson = gson.toJsonTree(filterEvents);
		
		ret.add("storyline", storylineJson);
		
	    response.setContentType("application/json; Charset-utf-8");
	    response.setHeader("pragma", "no-cache");
	    response.setHeader("cache-control", "no-cache");
	    
	    JsonWriter jw = new JsonWriter(response.getWriter());
	    gson.toJson(ret, jw);
	    jw.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
