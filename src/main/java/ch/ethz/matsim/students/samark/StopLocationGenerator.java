package ch.ethz.matsim.students.samark;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.EventHandler;

public class StopLocationGenerator {

		public static Map<Id<Link>,CustomLinkAttributes> findLinksAboveThreshold(Network network, double threshold, EventHandler eventHandler, int iterationToRead){		// output is a map with all links above threshold and their traffic (number of link enter events)
			
			// make a custom linkMap and initialize with all network links
			Map<Id<Link>,CustomLinkAttributes> customLinkMap = new HashMap<Id<Link>,CustomLinkAttributes>();
			Iterator<Id<Link>> iterator = network.getLinks().keySet().iterator();   		// take network and put all links in linkTrafficMap
			while(iterator.hasNext()) {
				Id<Link> thisLinkID = iterator.next();
				customLinkMap.put(thisLinkID, new CustomLinkAttributes());				//  - initiate traffic with default attributes
				customLinkMap.put(iterator.next(), new CustomLinkAttributes());				//  - initiate traffic with default attribute
			}
			
			// create an event handler for departure & arrival events to add one to the map entry of the corresponding link
			PT_StopTrafficCounter myPT_StopTrafficCounter = new PT_StopTrafficCounter();			// this is a custom event handler for counting the number of traffic movements on that stop facility
			customLinkMap = runPTStopTrafficScanner(myPT_StopTrafficCounter, customLinkMap, iterationToRead);
			
			// remove all links below threshold
			Map<Id<Link>,CustomLinkAttributes> linksAboveThreshold = new HashMap<Id<Link>,CustomLinkAttributes>();
			Iterator<Entry<Id<Link>, CustomLinkAttributes>> thresholdIterator = customLinkMap.entrySet().iterator();
			while (thresholdIterator.hasNext()) {
				Entry<Id<Link>, CustomLinkAttributes> entry = thresholdIterator.next();
				if(threshold <= entry.getValue().getTotalTraffic()) {
					linksAboveThreshold.put(entry.getKey(), entry.getValue());
				}
			}
			double average = OutputTestingImpl.getAverageTrafficOnLinks(linksAboveThreshold);
			System.out.println("Average pt traffic on links (person arrivals + departures) is: "+average);
			System.out.println("Number of links above threshold is: "+linksAboveThreshold.size());
			return linksAboveThreshold;
		}
	
		public static Map<Id<Link>,CustomLinkAttributes> findMostFrequentLinks(Network network, int nMostFrequentLinks, EventHandler eventHandler, int iterationToRead, Map<Id<Link>,CustomLinkAttributes> alreadyEventHandledLinkMap){		// output is a map with all links above threshold and their traffic (number of link enter events)
			
			Map<Id<Link>,CustomLinkAttributes> customLinkMap;
			if(alreadyEventHandledLinkMap==null) {
				// make a custom linkMap and initialize with all network links
				customLinkMap = new HashMap<Id<Link>,CustomLinkAttributes>();
				Iterator<Id<Link>> iterator = network.getLinks().keySet().iterator();   		// take network and put all links in linkTrafficMap
				while(iterator.hasNext()) {
					Id<Link> thisLinkID = iterator.next();
					customLinkMap.put(thisLinkID, new CustomLinkAttributes());				//  - initiate traffic with default attributes
					customLinkMap.put(iterator.next(), new CustomLinkAttributes());				//  - initiate traffic with default attribute
				}
				// create an event handler for departure & arrival events to add one to the map entry of the corresponding link
				PT_StopTrafficCounter myPT_StopTrafficCounter = new PT_StopTrafficCounter();			// this is a custom event handler for counting the number of traffic movements on that stop facility
				customLinkMap = runPTStopTrafficScanner(myPT_StopTrafficCounter, customLinkMap, iterationToRead);
			}
			else { 	// this is the case when the input link map has already undergone postprocessing by event handling
				customLinkMap = alreadyEventHandledLinkMap;
			}
			
			// add links if they are within top nMostFrequentlinks
			Map<Id<Link>,CustomLinkAttributes> mostFrequentLinks = new HashMap<Id<Link>,CustomLinkAttributes>(nMostFrequentLinks);
			int i = 0;
			for (Id<Link> linkID : customLinkMap.keySet()) {
				mostFrequentLinks.put(linkID, customLinkMap.get(linkID));
				i++;
				if (i == nMostFrequentLinks) {
					break;
				}
			}
			
			// add other links from customLinkMap if they have more traffic than previous minimum link
			for (Id<Link> linkID : customLinkMap.keySet()) {
				Id<Link> minTrafficLinkID = minimumTrafficLink(mostFrequentLinks);
				Double minTraffic = mostFrequentLinks.get(minimumTrafficLink(mostFrequentLinks)).getTotalTraffic();
				if (customLinkMap.get(linkID).getTotalTraffic() > minTraffic && mostFrequentLinks.containsKey(linkID)==false) {
					mostFrequentLinks.put(linkID, customLinkMap.get(linkID));
					mostFrequentLinks.remove(minTrafficLinkID);
				}
			}
			// calculate and display average
			double average = OutputTestingImpl.getAverageTrafficOnLinks(mostFrequentLinks);
			System.out.println("Average pt traffic on most frequent n="+nMostFrequentLinks+" links (person arrivals + departures) is: "+average);
			System.out.println("Number of most frequent links is: "+mostFrequentLinks.size());
			
			return mostFrequentLinks;
		}
	
		
		public static Id<Link> minimumTrafficLink(Map<Id<Link>,CustomLinkAttributes> linkSet){
			double minTraffic = Double.MAX_VALUE;
			Id<Link> minLinkID = null;
			for (Id<Link> linkID : linkSet.keySet()) {
				if (linkSet.get(linkID).getTotalTraffic() < minTraffic) {
					minTraffic = linkSet.get(linkID).getTotalTraffic();
					minLinkID = linkID;
				}
			}
			return minLinkID;
		}
		
		
		public static Map<Id<Link>,CustomLinkAttributes> runPTStopTrafficScanner(PT_StopTrafficCounter myPT_StopTrafficCounter, Map<Id<Link>,CustomLinkAttributes> emptyCustomLinkMap, int iterationToRead) {
			myPT_StopTrafficCounter.CustomLinkMap = emptyCustomLinkMap;
			EventsManager myEventsManager = EventsUtils.createEventsManager();
			myEventsManager.addHandler(myPT_StopTrafficCounter);
			MatsimEventsReader reader = new MatsimEventsReader(myEventsManager);
			String eventsFile = "zurich_1pm/simulation_output/ITERS/it."+iterationToRead+"/"+iterationToRead+".events.xml.gz";
			reader.readFile(eventsFile);
			
			return myPT_StopTrafficCounter.CustomLinkMap;
		}

	
}
