package ch.ethz.matsim.students.samark;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

public class NetworkCreatorImpl {

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

	public static Map<Id<Link>,CustomLinkAttributes> findMostFrequentLinks_FromNetwork(Network network, int nMostFrequentLinks, EventHandler eventHandler, int iterationToRead){		// output is a map with all links above threshold and their traffic (number of link enter events)
		
		Map<Id<Link>,CustomLinkAttributes> customLinkMap;
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
	
	public static Map<Id<Link>,CustomLinkAttributes> findMostFrequentLinks_FromEventProcessedLinksMap(int nMostFrequentLinks, Map<Id<Link>,CustomLinkAttributes> alreadyEventHandledLinkMap){		// output is a map with all links above threshold and their traffic (number of link enter events)
		
		Map<Id<Link>,CustomLinkAttributes> customLinkMap;
		customLinkMap = alreadyEventHandledLinkMap;
		
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
	
	public static Map<Id<Link>,CustomLinkAttributes> setMainFacilities(TransitSchedule transitSchedule, Network network, Map<Id<Link>,CustomLinkAttributes> selectedLinks){

		// Go through all facilities and whenever a stop facility refers to a selected link, associate that stop facility with that link 
		// Check its transport mode and - if a mode exists already - associate only if this transport mode has the bigger facility than the one before (rail > bus)
		// How to check transport mode of a facility:
			// - Go through all lines --> all routes --> all stops
			// - Check if stops contain the link of question (each selectedLink)
			// - if yes, assess that transportMode e.g. "rail" in transitRoute.transitMode and return the mode for the selected link!
		
		LinkLoop:
		for (Id<Link> selectedLinkID : selectedLinks.keySet()) {
			for (TransitLine transitLine : transitSchedule.getTransitLines().values()) {
				for (TransitRoute transitRoute : transitLine.getRoutes().values()) {
					for (TransitRouteStop transitRouteStop : transitRoute.getStops()) {
						if(transitRouteStop.getStopFacility().getLinkId() == selectedLinkID) {
							String mode = transitRoute.getTransportMode();
							//System.out.println("Mode on detected transit stop facility is |"+mode+"|");
							if (mode=="rail") {	
								CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
								updatedAttributes.setDominantMode(mode);
								updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
								selectedLinks.put(selectedLinkID, updatedAttributes);
								//System.out.println("Added mode: "+mode);
								continue LinkLoop; // if mode is rail, we set the default to rail bc it is most dominant and move to next link (--> this link is completed)
							}
							else if (mode=="tram") {	
								CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
								updatedAttributes.setDominantMode(mode);
								updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
								selectedLinks.put(selectedLinkID, updatedAttributes);
								//System.out.println("Added mode: "+mode);

							}
							else if (mode=="bus") {
								if (selectedLinks.get(selectedLinkID).getDominantMode()==null || selectedLinks.get(selectedLinkID).getDominantMode()=="funicular") {
									CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
									updatedAttributes.setDominantMode(mode);
									updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
									selectedLinks.put(selectedLinkID, updatedAttributes);
									//System.out.println("Added mode: "+mode);									
								}
							}
							else if (mode=="funicular") {
								if (selectedLinks.get(selectedLinkID).getDominantMode()==null) {									
									CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
									updatedAttributes.setDominantMode(mode);
									updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
									selectedLinks.put(selectedLinkID, updatedAttributes);
									//System.out.println("Added mode: "+mode);
								}
							}
							else {
								System.out.println("Did not recognize mode: "+mode+", but adding it anyways...");
								CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
								updatedAttributes.setDominantMode(mode);
								updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
								selectedLinks.put(selectedLinkID, updatedAttributes);
							}

						}
					}
				}
			}
		}
	
		return selectedLinks;
	}

	public static Map<Id<Link>, CustomLinkAttributes> findLinksWithinBounds_FromEventProcessedLinksMap(Network network, Map<Id<Link>, CustomLinkAttributes> feasibleLinksWithDominantFacility,
			Coord networkCenterCoord, double minRadiusFromCenter,double maxRadiusFromCenter) {

		Map<Id<Link>,CustomLinkAttributes> feasibleTerminalLinks;
		feasibleTerminalLinks = feasibleLinksWithDominantFacility;
		System.out.println("Size is: "+feasibleTerminalLinks.size());
		
		double distanceFromCenter;
		Iterator<Id<Link>> linkIterator = feasibleTerminalLinks.keySet().iterator();
		while(linkIterator.hasNext()) {
			Id<Link> thisLinkID = linkIterator.next();
			// calculate distance with dominantFacilityLocation
			distanceFromCenter = GeomDistance.calculate(feasibleTerminalLinks.get(thisLinkID).dominantStopFacility.getCoord(), networkCenterCoord);
			if (distanceFromCenter < minRadiusFromCenter || distanceFromCenter > maxRadiusFromCenter ) {
				linkIterator.remove();
			}
		}
		System.out.println("Size is: "+feasibleTerminalLinks.size());

		return feasibleTerminalLinks;
	}
	
	public static Map<Id<Link>, CustomLinkAttributes> findLinksWithinBounds_FromNetwork(Network network,
			PT_StopTrafficCounter pt_StopTrafficCounter, int iterationToRead, Coord networkCenterCoord, double minRadiusFromCenter,double maxRadiusFromCenter) {

		Map<Id<Link>,CustomLinkAttributes> feasibleTerminalLinks;
		// make a custom linkMap and initialize with all network links
		feasibleTerminalLinks = new HashMap<Id<Link>,CustomLinkAttributes>();
		Iterator<Id<Link>> iterator = network.getLinks().keySet().iterator();   		// take network and put all links in linkTrafficMap
		while(iterator.hasNext()) {
			Id<Link> thisLinkID = iterator.next();
			feasibleTerminalLinks.put(thisLinkID, new CustomLinkAttributes());				//  - initiate traffic with default attributes
			feasibleTerminalLinks.put(iterator.next(), new CustomLinkAttributes());				//  - initiate traffic with default attribute
		}
		// create an event handler for departure & arrival events to add one to the map entry of the corresponding link
		PT_StopTrafficCounter myPT_StopTrafficCounter = new PT_StopTrafficCounter();			// this is a custom event handler for counting the number of traffic movements on that stop facility
		feasibleTerminalLinks = runPTStopTrafficScanner(myPT_StopTrafficCounter, feasibleTerminalLinks, iterationToRead);
		System.out.println("Size is: "+feasibleTerminalLinks.size());
		
		double distanceFromCenter;
		Iterator<Id<Link>> linkIterator = feasibleTerminalLinks.keySet().iterator();
		while(linkIterator.hasNext()) {
			Id<Link> thisLinkID = linkIterator.next();
			// calculate distance with FromNode;
			distanceFromCenter = GeomDistance.calculate(network.getLinks().get(thisLinkID).getFromNode().getCoord(), networkCenterCoord);
			if (distanceFromCenter < minRadiusFromCenter || distanceFromCenter > maxRadiusFromCenter ) {
				linkIterator.remove();
			}
		}
		System.out.println("Size is: "+feasibleTerminalLinks.size());

		return feasibleTerminalLinks;
	}

	public static Network createMetroNetworkFromCandidates(
			Map<Id<Link>, CustomLinkAttributes> customLinkMap, double maxNewMetroLinkDistance, Network mergerNetwork) {
		// TODO Auto-generated method stub
		// Create a new network from available config (maybe config changes something about coordinate system etc.)
		Config config = ConfigUtils.loadConfig("zurich_1pm/zurich_config.xml");
		// Initiate all nodes of facility location (in customLinkMap) with their names as from mergerNetwork
		// for every node:
			// add NEW links (with appropriate naming method) to all nodes within a specific radius
			// add NEW links if other facility node was on a next link to the link of this facility
		
		
		return null;
	}
	
}
