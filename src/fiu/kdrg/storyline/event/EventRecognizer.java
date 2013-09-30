package fiu.kdrg.storyline.event;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import fiu.kdrg.nlp.NLPProcessor;

class TextJob {
	public TextJob(String url, String text) {
		super();
		this.url = url;
		this.text = text;
	}
	String url;
	String text;
}

class JobList {
	Queue<TextJob> list;
	public static final int MAX_AVAILABLE = 22;
	private final Semaphore available = new Semaphore(0, true);
	private final Semaphore capacity = new Semaphore(MAX_AVAILABLE, true);
	public JobList() {
		list = new LinkedList<TextJob>();
	}
	
	public TextJob getJob() throws InterruptedException {
		available.acquire();
		TextJob x = list.poll();
		capacity.release();
		return x;
	}

	public void putItem(TextJob x) throws InterruptedException {
		capacity.acquire();
		list.offer(x);
		available.release();
	}
}

public class EventRecognizer implements Runnable {
	
	static public void main(String[] args) throws Exception {
		String inputfile = "../sandy_all_clean_nodup_text.txt";
		String outputfile = "../sandy_all_clean_nodup_events.txt";
		
		recognizeEvents(inputfile, outputfile);
	}

	private static void recognizeEvents(String inputfile, String outputfile)
			throws IOException, UnsupportedEncodingException,
			FileNotFoundException, InterruptedException {
		Properties props = new Properties();
    	props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		props.put("ner.model.3class", "");
		props.put("ner.model.MISCclass", "");

		JobList jobList = new JobList();
		PrintWriter pw =  new PrintWriter(new FileWriter(outputfile)); //new PrintWriter(System.out);
		
		ExecutorService executor = Executors.newFixedThreadPool(JobList.MAX_AVAILABLE);

		for(int i = 0; i < JobList.MAX_AVAILABLE; i++) {
			executor.execute(new EventRecognizer(props, pw, jobList));
		}
		
		executor.shutdown();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputfile), "utf-8"));
		String line;
		
		StringBuilder sb = new StringBuilder(200 * 1024);
		String url = null;
		while((line = br.readLine())!= null) {
			if (line.startsWith("__START_A_DOC_CSHEN__")) {
				sb = new StringBuilder(200 * 1024);
				url = br.readLine();
				
			} else if (line.startsWith("__END_A_DOC_CSHEN__")) {
				String input = sb.toString();
				TextJob x = new TextJob(url, input);
				jobList.putItem(x);
			} else {
				sb.append(line).append("\n");
			}
		}
		br.close();
		
		for(int i = 0; i < JobList.MAX_AVAILABLE; i++) {
			jobList.putItem(null);
		}
		
		executor.awaitTermination(1000, TimeUnit.DAYS);
		pw.close();
	}
	
	NLPProcessor processor;
	PrintWriter pw;
	JobList jobList;
	
	public EventRecognizer(Properties props, PrintWriter pw, JobList jobList) {
		super();
		processor = new NLPProcessor(props);
		this.pw = pw;
		this.jobList = jobList;
	}
	
	public EventRecognizer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		while(true) {
			TextJob job = null;
			synchronized (jobList) {
				try {
					job = jobList.getJob();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (job == null)
				break;
			List<RawEvent> rawevents = processor.processString2RawEvents(job.text.split("\n"));
//			rawevents = NLPProcessor.modifyAmbiguousRawEventMatch(rawevents);
			List<Event> events = NLPProcessor.getFinedEvent(rawevents);
			synchronized (pw) {
				try {
					for (Event event : events) {
						pw.print(job.url + "\t");
						pw.print(new SimpleDateFormat("yyyy-MM-dd").format(new Date(event.eventDate)) + "\t");
						pw.print(event.eventLocation + "\t");
						pw.println(event.eventContent);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("failed to extract url: " + job.url);
				}
			}
		}
		
	}
	
}
