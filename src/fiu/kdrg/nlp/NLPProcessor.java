package fiu.kdrg.nlp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.NamedEntity;
import fiu.kdrg.storyline.event.RawEvent;
import fiu.kdrg.util.DatePicker;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.Util;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class NLPProcessor implements Runnable {

	private Properties props;
	private StanfordCoreNLP pipeline;
	private NLPController mycontroller;
	private Thread myThread;
	private int minLength;
	private List<String> jobList;
	private NLPPreprocessor preprocessor;

	public NLPProcessor(Properties props) {
		this(props, null, null);
	}

	public NLPProcessor(Properties props, NLPController controller,
			List<String> jobList) {

		// TODO Auto-generated constructor stub
		minLength = 100;// default value
		this.props = props;
		this.jobList = jobList;
		this.mycontroller = controller;
		this.pipeline = new StanfordCoreNLP(props);
		preprocessor = new NLPPreprocessor();
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (mycontroller.getSource() == null
				|| mycontroller.getDestination() == null) {
			System.out.println("Please set sourceFolder and storageFolder!");
			return;
		}

		String text = "";
		List<Event> events = new ArrayList<Event>();

		for (String file : jobList) {
			text += preprocessor.preprocessFile(file, minLength);
		}

		List<RawEvent> rawEvents = processString2RawEvents(text);
		// EventUtil.displayRawEvents(rawEvents);

		if (rawEvents == null || rawEvents.isEmpty())
			return;

		rawEvents = modifyAmbiguousRawEventMatch(rawEvents);
		// EventUtil.displayRawEvents(rawEvents);

		if (rawEvents == null || rawEvents.isEmpty())
			return;

		events = getFinedEvent(rawEvents);

		// if(events != null && !events.isEmpty())
		// events.addAll(events);

		synchronized (mycontroller.getEventContainer()) {

			// System.out.println(text);
			System.out.println("events got by " + this.getMyThread().getName());
			EventUtil.displayEvents(events);

			if (null != events && !events.isEmpty())
				mycontroller.getEventContainer().addAll(events);
			else {
				System.out.println(this.getMyThread().getName()
						+ " get empty result");
			}

		}

	}

	// -------------------------------------------------------------------------

	public StanfordCoreNLP getPipeline() {
		return pipeline;
	}

	public void setPipeline(StanfordCoreNLP pipeline) {
		this.pipeline = pipeline;
	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public NLPController getMycontroller() {
		return mycontroller;
	}

	public void setMycontroller(NLPController mycontroller) {
		this.mycontroller = mycontroller;
	}

	public int getMinLength() {
		return minLength;
	}

	public void setMinLength(int minLength) {
		this.minLength = minLength;
	}

	public List<String> getJobList() {
		return jobList;
	}

	public void setJobList(List<String> jobList) {
		this.jobList = jobList;
	}

	public NLPPreprocessor getPreprocessor() {
		return preprocessor;
	}

	public void setPreprocessor(NLPPreprocessor preprocessor) {
		this.preprocessor = preprocessor;
	}

	public Thread getMyThread() {
		return myThread;
	}

	public void setMyThread(Thread myThread) {
		this.myThread = myThread;
	}

	// -----------------------------------------------------------------------------------------------------------------
	private List<CoreMap> process(String text) {
		Annotation document = new Annotation(text);

		pipeline.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		return sentences;
	}

	/**
	 * compare two entities to see where they belongs to same entity this is
	 * based on rules
	 */
	private boolean isSameEntity(NamedEntity ner1, NamedEntity ner2, int range) {
		if (null == ner1 || null == ner2)
			return false;

		if (!ner1.getType().equals(ner2.getType()))
			return false;
		else if (Math.abs(ner2.getBeginPosition() - ner1.getBeginPosition()) > (ner1
				.getEntityText().length() + range)) {
			// remember to add the length of previous entity content
			return false;
		} else
			return true;
	}

	/**
	 * call private function and set the default range to two
	 * 
	 * @param ner1
	 * @param ner2
	 * @return
	 */
	public boolean isSameEntity(NamedEntity ner1, NamedEntity ner2) {
		return isSameEntity(ner1, ner2, 10);
	}

	/**
	 * roughly judge whether we can borrow some entity(LOCATION, DATE) from
	 * previous event. It based on distance between these two entities' position
	 * 
	 * @param entity
	 * @param preEntity
	 * @param range
	 * @return
	 */
	private boolean isEntityReferable(NamedEntity entity,
			NamedEntity preEntity, int range) {
		if (null == entity || null == preEntity)
			return false;

		if ((Math.abs(entity.getBeginPosition() - preEntity.getBeginPosition())) > range)
			return false;

		return true;
	}

	/**
	 * borrower borrow certain type of NamedEntity from lender, if success,
	 * return this NamedEntity, else return null we search this type of
	 * NamedEntity from end of RawEnent, and then get first one we find, return
	 * null if find none
	 * 
	 * @param borrower
	 * @param lender
	 * @param type
	 * @return
	 */
	public NamedEntity borrowEntity(NamedEntity borrower, RawEvent lender,
			String type) {
		if (null == lender || lender.getEntities().isEmpty())
			return null;

		List<NamedEntity> tmp = lender.getEntities();

		for (int i = tmp.size() - 1; i >= 0; i--) {
			if (tmp.get(i).getType().equals(type)
					&& isEntityReferable(borrower, tmp.get(i), 500))// default
																	// range 200
				return tmp.get(i);

		}

		return null;
	}

	public List<RawEvent> processString2RawEvents(String[] paragraphs) {
		List<RawEvent> events = new ArrayList<RawEvent>();
		for (String paragraph : paragraphs)
			events.addAll(processString2RawEvents(paragraph));
		return events;
	}

	public List<RawEvent> processString2RawEvents(String text) {
		List<CoreMap> sentences = this.process(text);
		List<RawEvent> events = new ArrayList<RawEvent>();

		for (CoreMap sentence : sentences) {
			RawEvent rawEvent = new RawEvent();
			rawEvent.setSentence(sentence.toString());

			String type;
			String entityText;
			int beginPosition;

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// get Entity type of this token
				type = token.get(NamedEntityTagAnnotation.class);
				beginPosition = token.beginPosition();
				entityText = token.get(TextAnnotation.class);
				NamedEntity previousEntity;

				// we only care about Location, Date, Duration, Time
				if (type.equals(NamedEntity.LOCATION_ENTITY)) {
					if (rawEvent.getEntities().isEmpty())
						rawEvent.getEntities()
								.add(new NamedEntity(type, entityText,
										beginPosition));
					else {
						previousEntity = rawEvent.getEntities().get(
								rawEvent.getEntities().size() - 1);
						// See whether previous entity and current entity are
						// the same entity
						if (isSameEntity(previousEntity, new NamedEntity(type,
								entityText, beginPosition), 4)) {
							rawEvent.getEntities()
									.get(rawEvent.getEntities().size() - 1)
									.setEntityText(
											previousEntity.getEntityText()
													.trim()
													+ " "
													+ entityText.trim());
						} else {
							rawEvent.getEntities().add(
									new NamedEntity(type, entityText,
											beginPosition));
						}
					}
				}

				if (type.equals(NamedEntity.DATE_ENTITY)) {
					if (rawEvent.getEntities().isEmpty())
						rawEvent.getEntities()
								.add(new NamedEntity(type, entityText,
										beginPosition));
					else {
						previousEntity = rawEvent.getEntities().get(
								rawEvent.getEntities().size() - 1);
						// See whether previous entity and current entity are
						// the same entity
						if (isSameEntity(previousEntity, new NamedEntity(type,
								entityText, beginPosition), 10)) {
							rawEvent.getEntities()
									.get(rawEvent.getEntities().size() - 1)
									.setEntityText(
											previousEntity.getEntityText()
													.trim()
													+ " "
													+ entityText.trim());
						} else {
							rawEvent.getEntities().add(
									new NamedEntity(type, entityText,
											beginPosition));
						}
					}
				}

			}

			// generate one rawEvent by every sentence
			if (!rawEvent.getEntities().isEmpty()
					&& rawEvent.containsEntityType(NamedEntity.DATE_ENTITY))
				events.add(rawEvent);

		}

		return events;

	}

	/**
	 * modify events, so that for the return variable modifiedEvents, every
	 * element in it has and only has one LOCATION entity and DATE entity
	 * 
	 * @param events
	 * @return
	 */
	static private List<RawEvent> modifyAmbiguousRawEventMatch(
			List<RawEvent> events) {
		if (null == events || events.isEmpty())
			return null;

		List<RawEvent> modifiedEvents = new ArrayList<RawEvent>();

		for (int i = 0; i < events.size(); i++) {
			// if current rawEvents contains both two type entities. no borrow
			// from previous
			// but we still need to process in case of multiple same type
			// entities
			RawEvent event = events.get(i);

			if (event.containsEntityType(NamedEntity.LOCATION_ENTITY)
					&& event.containsEntityType(NamedEntity.DATE_ENTITY)) {
				int waitForMatchEntity = 0;

				List<NamedEntity> entities = event.getEntities();

				for (int j = 0; j < entities.size(); j++) {
					// if types are different, matched them and seperate to
					// multiple independent event
					if (!entities.get(waitForMatchEntity).getType()
							.equals(entities.get(j).getType())) {
						NamedEntity matched = new NamedEntity(entities.get(j)
								.getType(), entities.get(j).getEntityText(),
								entities.get(j).getBeginPosition());

						for (int k = waitForMatchEntity; k < j; k++) {
							List<NamedEntity> tmpEntities = new ArrayList<NamedEntity>();
							tmpEntities.add(new NamedEntity(entities.get(k)
									.getType(),
									entities.get(k).getEntityText(), entities
											.get(k).getBeginPosition()));

							tmpEntities.add(matched);

							modifiedEvents.add(new RawEvent(
									event.getSentence(), tmpEntities));
						}

						waitForMatchEntity = j + 1; // modify waitForMatchEntity
													// for next match
					}

				}

				// after for loop, if waitForMatchEntity != entities.size . it
				// means there are some entities not matched.
				// roll back and match it
				// if(waitForMatchEntity != entities.size())
				// {
				// NamedEntity borrower = new
				// NamedEntity(entities.get(waitForMatchEntity).getType(),
				// entities.get(waitForMatchEntity).getEntityText(),
				// entities.get(waitForMatchEntity).getBeginPosition());
				// String borrowType = (borrower.getType()
				// .equals(NamedEntity.LOCATION_ENTITY) ?
				// NamedEntity.DATE_ENTITY : NamedEntity.LOCATION_ENTITY);
				//
				// //borrow from who, we need focus on this, maybe we should
				// borrow from previous events.get(i - 1),i,e, the last one
				// NamedEntity borrowedEntity = borrowEntity(borrower,
				// modifiedEvents.get(modifiedEvents.size() - 1), borrowType);
				// if(null == borrowedEntity)
				// continue; //fail to borrow
				//
				// for(int j = waitForMatchEntity; j < entities.size(); j++)
				// {
				// NamedEntity borrowerEntity = new
				// NamedEntity(entities.get(j).getType(),
				// entities.get(j).getEntityText(),
				// entities.get(j).getBeginPosition());
				//
				// List<NamedEntity> tmpEntities = new ArrayList<NamedEntity>();
				// tmpEntities.add(borrowedEntity);
				// tmpEntities.add(borrowerEntity);
				//
				// modifiedEvents.add(new RawEvent(event.getSentence(),
				// tmpEntities));
				// }
				//
				// }

			}
			// else
			// if(event.containsEntityType(NamedEntity.LOCATION_ENTITY))//Only
			// contains LOCATION, borrow TIME from last Event
			// {
			// List<NamedEntity> entities = event.getEntities();
			//
			// NamedEntity borrower = new NamedEntity(entities.get(0).getType(),
			// entities.get(0).getEntityText(),
			// entities.get(0).getBeginPosition());
			//
			// if(modifiedEvents.size() == 0)
			// continue;
			//
			// NamedEntity borrowedEntity = borrowEntity(borrower,
			// modifiedEvents.get(modifiedEvents.size() - 1),
			// NamedEntity.DATE_ENTITY);
			//
			// if(null == borrowedEntity)
			// continue; //fail to borrow
			//
			// for(int j = 0; j < entities.size(); j++)
			// {
			// NamedEntity borrowerEntity = new
			// NamedEntity(entities.get(j).getType(),
			// entities.get(j).getEntityText(),
			// entities.get(j).getBeginPosition());
			//
			// List<NamedEntity> tmpEntities = new ArrayList<NamedEntity>();
			// tmpEntities.add(borrowedEntity);
			// tmpEntities.add(borrowerEntity);
			//
			// modifiedEvents.add(new RawEvent(event.getSentence(),
			// tmpEntities));
			// }
			//
			// }
			// else if(event.containsEntityType(NamedEntity.DATE_ENTITY))//one
			// contains date
			// {
			// List<NamedEntity> entities = event.getEntities();
			//
			// NamedEntity borrower = new NamedEntity(entities.get(0).getType(),
			// entities.get(0).getEntityText(),
			// entities.get(0).getBeginPosition());
			//
			// if(modifiedEvents.size() == 0)
			// continue;
			//
			// NamedEntity borrowedEntity = borrowEntity(borrower,
			// modifiedEvents.get(modifiedEvents.size() - 1),
			// NamedEntity.LOCATION_ENTITY);
			//
			// if(null == borrowedEntity)
			// continue; //fail to borrow
			//
			// for(int j = 0; j < entities.size(); j++)
			// {
			// NamedEntity borrowerEntity = new
			// NamedEntity(entities.get(j).getType(),
			// entities.get(j).getEntityText(),
			// entities.get(j).getBeginPosition());
			//
			// List<NamedEntity> tmpEntities = new ArrayList<NamedEntity>();
			// tmpEntities.add(borrowedEntity);
			// tmpEntities.add(borrowerEntity);
			//
			// modifiedEvents.add(new RawEvent(event.getSentence(),
			// tmpEntities));
			// }
			// }
		}

		return modifiedEvents;
	}

	/**
	 * convert rawEvent to Event object.
	 * 
	 * @param events
	 *            events that we get after call
	 *            modifyAmbiguousRawEventMath(method)
	 * @return
	 */
	static public List<Event> getFinedEvent(List<RawEvent> events) {

		// events = modifyAmbiguousRawEventMatch(events);
		List<Event> finedEvents = new ArrayList<Event>();

		if (null == events || events.isEmpty())
			return finedEvents;
		
		for (int i = 0; i < events.size(); i++) {
			RawEvent tmpRawEvent = events.get(i);
			List<NamedEntity> tmpEntities = tmpRawEvent.getEntities();
			
			String eventContent = tmpRawEvent.getSentence();
			String eventLocation = "";
			String eventDate = "";
			String eventURL = "https://www.google.com";
			String tmpY = null,tmpM = null,tmpD = null;

			for (int j = 0; j < tmpEntities.size(); j++) {
				String enTxt = tmpEntities.get(j).getEntityText();
				if (tmpEntities.get(j).getType()
						.equals(NamedEntity.DATE_ENTITY)) {
					// test if it contains year, month, day
					if(null == tmpM)
						tmpM = Util.extractStringByRE(enTxt, EventUtil.MONTH_REGEX);

					if(null == tmpD)
						tmpD = Util.extractStringByRE(enTxt, EventUtil.DAY_REGEX);

					if(null == tmpY)
						tmpY = Util.extractStringByRE(enTxt, EventUtil.YEAR_REGEX);
				}

				if (tmpEntities.get(j).getType()
						.equals(NamedEntity.LOCATION_ENTITY))
					eventLocation += " | " + enTxt;
			}
			if (eventLocation.length() > 0)
				eventLocation = eventLocation.substring(3);
			
			if(null != tmpM && null != tmpD && null != tmpY)	{
				eventDate = tmpM + " " + tmpD + ", " + tmpY;
				if (eventLocation.length() > 0) {
					Long tmpEventDate;
					try {
						tmpEventDate = Util.parseDate2Milionseconds(eventDate);

						finedEvents.add(new Event(eventURL, eventContent,
								eventLocation, tmpEventDate));

					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return finedEvents;

	}
	
	
	
	static public List<Event> getFinedEvent(List<RawEvent> events, String date) {

		// events = modifyAmbiguousRawEventMatch(events);
		List<Event> finedEvents = new ArrayList<Event>();
		if (null == events || events.isEmpty())
			return finedEvents;

		DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
		LocalDate ld = null;
		try {
			ld = formatter.parseLocalDate(date);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if(ld == null)
			return getFinedEvent(events); //still using original function

		for (int i = 0; i < events.size(); i++) {
			RawEvent tmpRawEvent = events.get(i);
			List<NamedEntity> tmpEntities = tmpRawEvent.getEntities();
			
			String eventContent = tmpRawEvent.getSentence();
			String eventLocation = "";
			String eventDate = "";
			String eventURL = "https://www.google.com";
			String tmpY = null,tmpM = null,tmpD = null, tmpW = null;

			boolean dateFlag = false;
			for (int j = 0; j < tmpEntities.size(); j++) {
				String enTxt = tmpEntities.get(j).getEntityText();
				if (tmpEntities.get(j).getType()
						.equals(NamedEntity.DATE_ENTITY)) {
					dateFlag = true;
					// test if it contains year, month, day
					if(null == tmpM)
						tmpM = Util.extractStringByRE(enTxt, EventUtil.MONTH_REGEX);

					if(null == tmpD)
						tmpD = Util.extractStringByRE(enTxt, EventUtil.DAY_REGEX);
					
					if(null == tmpY)
						tmpY = Util.extractStringByRE(enTxt, EventUtil.YEAR_REGEX);
					
					if(null == tmpW)
						tmpW = Util.extractStringByRE(enTxt, EventUtil.WEEKDAY_REGEX);
				}

				if (tmpEntities.get(j).getType()
						.equals(NamedEntity.LOCATION_ENTITY))
					eventLocation += " | " + enTxt;
			}
			if (eventLocation.length() > 0) {
				eventLocation = eventLocation.substring(3);
			
				String tmpYb = tmpY,tmpMb = tmpM,tmpDb = tmpD;
				LocalDate tmpld = null;
			
				//try to make following four situations survive
				//only year is missing
				if(null == tmpY && null != tmpM && null != tmpD) {
					tmpYb = ld.getYear() + "";
				} else if(null != tmpY && null != tmpM && null != tmpW && null == tmpD) { 
					//only day is missing
					tmpDb = ld.getDayOfMonth() + "";
				} else if( !(null != tmpY && null != tmpM && null != tmpD) && tmpW != null) {
					//something is missing, but week day given
					tmpld= DatePicker.getNearestDayOfWeekBefore(ld, tmpW);
					tmpYb = tmpld.getYear() + "";
					tmpMb = EventUtil.MONTH_MAPPER[tmpld.getMonthOfYear() - 1];
					tmpDb = tmpld.getDayOfMonth() + "";
				} else if (null == tmpY && null == tmpM && null == tmpD && null == tmpW && dateFlag) {
					//everything is missing, but do have some date entity,make some random guess before publish date
					int guess = (int) (1 + Math.random() * 7);
					tmpld = DatePicker.getNearestDayOfWeekBefore(ld, guess);
					tmpYb = tmpld.getYear() + "";
					tmpMb = EventUtil.MONTH_MAPPER[tmpld.getMonthOfYear() - 1];
					tmpDb = tmpld.getDayOfMonth() + "";
				}
				
				if (null != tmpYb && null != tmpMb && null != tmpDb && eventLocation.length() > 0) {
					Long tmpEventDate;
					eventDate = tmpMb + " " + tmpDb + ", " + tmpYb;
					try {
						tmpEventDate = Util.parseDate2Milionseconds(eventDate);
							finedEvents.add(new Event(eventURL, eventContent,
									eventLocation, tmpEventDate));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return finedEvents;

	}
	

}
