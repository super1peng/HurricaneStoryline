package fiu.kdrg.storyline.event;

/**
 * This class represents Entity Unit we can get by using StanfordNLP
 * i,e  Location, Date, Number, Ordinal, Money, Duration, Percent, Organization, Time
 * This class also remember it's begin position in the text
 * @author zhouwubai
 *
 */
public class NamedEntity {

	public static String LOCATION_ENTITY = "LOCATION";
	public static String DATE_ENTITY = "DATE";
	public static String DURATION_ENTITY = "DURATION";
	public static String TIME_ENTITY = "TIME";
	
	String type;
	String entityText;
	int beginPosition;
	
	public NamedEntity(String type, String entityText, int beginPosition) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.entityText = entityText;
		this.beginPosition = beginPosition;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getEntityText() {
		return entityText;
	}
	
	public void setEntityText(String entityText) {
		this.entityText = entityText;
	}
	
	public int getBeginPosition() {
		return beginPosition;
	}
	
	public void setBeginPosition(int beginPosition) {
		this.beginPosition = beginPosition;
	}

	
}
