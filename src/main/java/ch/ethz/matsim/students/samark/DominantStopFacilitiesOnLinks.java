package ch.ethz.matsim.students.samark;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

public class DominantStopFacilitiesOnLinks { 	// checks biggest infrastructure stop facility on that specific link
												// i.e. a train stop is dominant over a bus stop and therefore more viable for a new metro stop
												// the idea is to assign the best stop facility to that selected link

	public static Map<Id<Link>,CustomLinkAttributes> find(TransitSchedule transitSchedule, Network network, Map<Id<Link>,CustomLinkAttributes> selectedLinks){

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

		// write out dominant mode for every link:
		/*for (Id<Link> l : selectedLinks.keySet()) {
			System.out.println(l.toString()+" link has dominant facility "+selectedLinks.get(l).dominantMode);
		}
		System.out.println("Selected links size (=dominant stop facilities) is "+selectedLinks.size());
		*/
	
		return selectedLinks;
	}	



	public static Map<Id<Link>, CustomLinkAttributes> findFeasibleTerminalLinks(Network network,
			PT_StopTrafficCounter pt_StopTrafficCounter, int iterationToRead, Map<Id<Link>, CustomLinkAttributes> feasibleLinksWithDominantFacility,
			Coord networkCenterCoord, double minRadiusFromCenter,double maxRadiusFromCenter) {

		Map<Id<Link>,CustomLinkAttributes> feasibleTerminalLinks;
		if(feasibleLinksWithDominantFacility==null) {
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
			feasibleTerminalLinks = StopLocationGenerator.runPTStopTrafficScanner(myPT_StopTrafficCounter, feasibleTerminalLinks, iterationToRead);
		}
		else { 	// this is the case when the input link map has already undergone post-processing by event handling
			feasibleTerminalLinks = feasibleLinksWithDominantFacility;
		}
		System.out.println("Size is: "+feasibleTerminalLinks.size());
		
		
		double distanceFromCenter;
		
		Iterator<Id<Link>> linkIterator = feasibleTerminalLinks.keySet().iterator();
		while(linkIterator.hasNext()) {
			Id<Link> thisLinkID = linkIterator.next();
			if (feasibleLinksWithDominantFacility==null) {
				// calculate distance with FromNode;
				distanceFromCenter = GeomDistance.calculate(network.getLinks().get(thisLinkID).getFromNode().getCoord(), networkCenterCoord);
			}
			else {
				// calculate distance with dominantFacilityLocation
				distanceFromCenter = GeomDistance.calculate(feasibleTerminalLinks.get(thisLinkID).dominantStopFacility.getCoord(), networkCenterCoord);
			}
			
			if (distanceFromCenter < minRadiusFromCenter || distanceFromCenter > maxRadiusFromCenter ) {
				linkIterator.remove();
				//feasibleTerminalLinks.remove(thisLinkID);
			}
		}
		System.out.println("Size is: "+feasibleTerminalLinks.size());

		return feasibleTerminalLinks;
	}




}
