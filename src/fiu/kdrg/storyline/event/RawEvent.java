package fiu.kdrg.storyline.event;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines Events we get from one sentence
 * But this is raw data probably including many Entities, such as
 * lots of Location Entity, lots of Date Entities. We map them by its AnnotationType
 * i,e. Location, Date, Number, Ordinal, Money, Duration, Percent, Organization, Time
 * @author zhouwubai
 *
 */
public class RawEvent {
	
	
	String sentence;
	List<NamedEntity> entities;
	
	public RawEvent() {
		// TODO Auto-generated constructor stub
		entities = new ArrayList<NamedEntity>();
	}
	
	public RawEvent(String sentence, List<NamedEntity> entities)
	{
		this.sentence = sentence;
		this.entities = entities;
	}

	public String getSentence() {
		return sentence;
	}

	public void setSentence(String sentence) {
		this.sentence = sentence;
	}

	public List<NamedEntity> getEntities() {
		return entities;
	}

	public void setEntities(List<NamedEntity> entities) {
		this.entities = entities;
	}
	
	
	
	
	
	/**
	 * test whether this sentence contains entity of specific type
	 * @param type
	 * @return
	 */
	public boolean containsEntityType(String type)
	{
		if(entities.isEmpty())
			return false;
		else
		{
			for(int i = 0; i < entities.size(); i++)
			{
				if(entities.get(i).getType().equals(type))
					return true;
			}
		}
		
		return false;
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String desc = sentence + "\n";
		for(NamedEntity ne : entities){
			desc += ne.entityText + " | ";
		}
		desc = desc.substring(0, desc.length() - 3);
		
		return desc;
	}

}
