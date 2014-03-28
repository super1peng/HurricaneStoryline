package fiu.kdrg.visual.servlet;

import fiu.kdrg.visual.servlet.helper.Cacher;
import graphTheory.graph.Arc;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

/**
 * Servlet implementation class loadLocalSteinerTree
 */
@WebServlet("/loadLocalSteinerTree")
public class loadLocalSteinerTree extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public loadLocalSteinerTree() {
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
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		int dID = Integer.parseInt(request.getParameter("disasterID"));
		int eID = Integer.parseInt(request.getParameter("eventID"));
		
		//12896
		Set<Arc> arc = Cacher.query(dID, eID);
		
		Gson gson = new Gson();
		JsonObject ret = new JsonObject();
		JsonElement arcJson = gson.toJsonTree(arc);

		ret.add("arcs", arcJson);

		response.setContentType("application/json; Charset-utf-8");
		response.setHeader("pragma", "no-cache");
		response.setHeader("cache-control", "no-cache");

		JsonWriter jw = new JsonWriter(response.getWriter());
		gson.toJson(ret, jw);
		jw.flush();
		
	}

}
