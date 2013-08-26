package fiu.kdrg.nlp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fiu.kdrg.geocode.Geocoder;
import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.util.IOUtil;

public class NLPController {

    public static void main(String[] args) throws Exception {

    	
    	Properties props = new Properties();
    	props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.put("ner.model.3class", "");
		props.put("ner.model.MISCclass", "");
    	
		String dir = System.getProperty("user.dir");
//		String source = dir + "/data/rawData/Hurricane_Katrina";
		String source = dir + "/data/rawData/Hurricane_Katrina/";
		String destination = dir + "/data/eventData/multi_Hurricane_Katrina.txt";
		
		NLPController controller = new NLPController(props, source, destination); 
		controller.start(4);
   }
    
    
    private String source;
    private String destination;
    private boolean finished;
    private Properties props;
    private int realNumOfProcessor = 1;//probably different from number user input
	List<Thread> processors;
	private List<Event> eventContainer;
	Geocoder geocoder;
    
    public NLPController() {
		// TODO Auto-generated constructor stub
    	source = null;
    	destination = null;
	}
    
    
    public NLPController(Properties props, String source, String destination){
    	
    	this.source = source;
    	this.destination = destination;
    	this.props = props;
    	this.processors = new ArrayList<Thread>();
    	this.eventContainer = new ArrayList<Event>();
    	geocoder = new Geocoder();
    }
	
	
	public String getSource() {
		return source;
	}



	public void setSource(String source) {
		this.source = source;
	}



	public String getDestination() {
		return destination;
	}



	public void setDestination(String destination) {
		this.destination = destination;
	}


	public boolean isFinished() {
		return finished;
	}


	public void setFinished(boolean finished) {
		this.finished = finished;
	}


	public Properties getProps() {
		return props;
	}


	public void setProps(Properties props) {
		this.props = props;
	}

	
	public int getRealNumOfProcessor() {
		return realNumOfProcessor;
	}


	public void setRealNumOfProcessor(int realNumOfProcessor) {
		this.realNumOfProcessor = realNumOfProcessor;
	}


	public List<Thread> getProcessors() {
		return processors;
	}


	public void setProcessors(List<Thread> processors) {
		this.processors = processors;
	}


	public List<Event> getEventContainer() {
		return eventContainer;
	}


	public void setEventContainer(List<Event> eventContainer) {
		this.eventContainer = eventContainer;
	}


	/**
	 * start processor threads and monitor their behave
	 * @param numberOfProcessor
	 */
	public void start(int numberOfProcessor)
	{
		if(numberOfProcessor <= 0) return;

		List<String> jobList = getJobList();
		if(jobList.isEmpty()) return;
		
		
		int numberJobsEach = jobList.size() / numberOfProcessor;
		if(numberJobsEach * numberOfProcessor < jobList.size()){
			numberJobsEach += 1;
		}
		
		//calculate real number of thread
		realNumOfProcessor = (int) Math.ceil(jobList.size() / numberJobsEach);
		
		//create processor and distribute job to them
		for(int i = 0; i < realNumOfProcessor; i++){

			int begin, end;
			
			if((i+1) * numberJobsEach < jobList.size()){
				begin = i * numberJobsEach;
				end = (i + 1) * numberJobsEach;
			}
			else{
				begin = i * numberJobsEach;
				end = jobList.size();//joblist.sublist(begin, end) end , exclusive
			}
			
			NLPProcessor processor = new NLPProcessor(props, this, jobList.subList(begin, end));
			Thread thread = new Thread(processor);
			processor.setMyThread(thread);
			thread.start();
			System.out.println(thread.getName() + " is running");
			processors.add(thread);	

		}
		
		
		
		//monitor to manage all threads
		Thread monitor = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				while(!finished){
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					finished = true;
					for(int i = 0; i < processors.size(); i++)
					{
						if(processors.get(i).isAlive())
						{
							finished = false;
						}
					}
					
				}
				
				System.out.println("All threads are dead");
				System.out.println("Coding and Writing");
				
				
				//write the answer
				//last one write the answer
				if(null != eventContainer && !eventContainer.isEmpty())
				{
					System.out.println("Coding.......");
					
			    	try {
						//eventContainer = geocoder.geocodeEvent(eventContainer);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	
					System.out.println("writing........");
					IOUtil.writeEventsToFile(eventContainer, destination);
				}
				else
				{
					System.out.println("no result find");
				}
			}
			
		});
		
		
		monitor.start();
		
	}
	
	
	/**
	 * get jobList
	 * @return
	 */
	private List<String> getJobList()
	{
		List<String> jobList = new ArrayList<String>();
		File tmpSource = new File(source);
		//get jobList
		if(tmpSource.isFile())
			jobList.add(source);
		else if(tmpSource.isDirectory()){
			
			String[] files = new File(source).list();
			for(String str : files)
			{
				String absolutePath = tmpSource.getAbsolutePath() + "/" + str;
				jobList.add(absolutePath);
			}
			
		}
		
		return jobList;
	}
	
	
    
}