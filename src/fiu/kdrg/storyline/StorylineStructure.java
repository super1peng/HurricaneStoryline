package fiu.kdrg.storyline;

import java.util.*;

import fiu.kdrg.storyline.event.*;

class StorylineNode {
	Event repr;
	Set<Event> covered;
	List<StorylineNode> followers;
	StorylineNode parent;
}

public class StorylineStructure {

	List<StorylineNode> roots;
	
	
	public static void main(String[] args) {
		

	}

}
