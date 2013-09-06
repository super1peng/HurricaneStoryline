package fiu.kdrg.visual.servlet;

import java.util.ArrayList;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.Util;

public class Test {

	
	public static void main(String[] args) {
		
		ArrayList<Event> filterEvents = null;
		try {
			filterEvents = (ArrayList<Event>) SerializeFactory.deSerialize(Util.rootDir + "filterEvents.out");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
