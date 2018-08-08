package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

public class OutputTestingImpl {

	public static void createNetworkFromCustomLinks(Map<Id<Link>,CustomLinkAttributes> customLinkMap, Network oldNetwork, String linksString) {
	// public static void createNetworkFromCustomLinks(Map<Id<Link>,CustomLinkAttributes> customLinkMap, Network oldNetwork, String linksString, String facilityNodesString) {	
		
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		Network strongestLinksNetwork = scenario.getNetwork();
		NetworkFactory stongestLinksFactory = strongestLinksNetwork.getFactory();
				
		Network dominantFacilities = scenario.getNetwork();
		NetworkFactory dominantFacilitiesFactory = dominantFacilities.getFactory();
		Node facilityNode;
		
		for (Id<Link> linkIDiter : customLinkMap.keySet()) {
			Node fromNode = stongestLinksFactory.createNode(oldNetwork.getLinks().get(linkIDiter).getFromNode().getId(), oldNetwork.getLinks().get(linkIDiter).getFromNode().getCoord());
			Node toNode = stongestLinksFactory.createNode(oldNetwork.getLinks().get(linkIDiter).getToNode().getId(), oldNetwork.getLinks().get(linkIDiter).getToNode().getCoord());
			if (strongestLinksNetwork.getNodes().containsKey(fromNode.getId())==false) {				
				strongestLinksNetwork.addNode(fromNode);
			}
			if (strongestLinksNetwork.getNodes().containsKey(toNode.getId())==false) {				
				strongestLinksNetwork.addNode(toNode);
			}
			Link linkBetweenNodes = stongestLinksFactory.createLink(linkIDiter, fromNode, toNode);
			strongestLinksNetwork.addLink(linkBetweenNodes);
			if (customLinkMap.get(linkIDiter).dominantStopFacility==null) {
				facilityNode = dominantFacilitiesFactory.createNode(Id.createNodeId("FacilityCoordNodeOfLink"+linkIDiter.toString()), 
						oldNetwork.getLinks().get(linkIDiter).getFromNode().getCoord());
			}
			else {
				facilityNode = dominantFacilitiesFactory.createNode(Id.createNodeId("FacilityCoordNodeOfLink"+linkIDiter.toString()), 
																						customLinkMap.get(linkIDiter).dominantStopFacility.getCoord());
			}
			dominantFacilities.addNode(facilityNode);
		}
			
		NetworkWriter networkWriterLinks = new NetworkWriter(strongestLinksNetwork);
		networkWriterLinks.write(linksString);
		
		// NetworkWriter networkWriterNodes = new NetworkWriter(dominantFacilities);
		// networkWriterNodes.write(facilityNodesString);
		
	}
	
	public static double getAverageTrafficOnLinks(Map<Id<Link>,CustomLinkAttributes> customLinkMap) {
		double totalTraffic = 0.0;
		for (Id<Link> linkID : customLinkMap.keySet()) {
			totalTraffic += customLinkMap.get(linkID).getTotalTraffic();
		}
		return totalTraffic/customLinkMap.size();
	}
	
	// this does not work due to casting failure from stop facility to activity facility
	public static void allFeasibleStopFacilitiesToFile(Map<Id<Link>,CustomLinkAttributes> mostFeasibleLinks) {
		List<TransitStopFacility> allTransitRouteStopFacilities = new ArrayList<TransitStopFacility>(mostFeasibleLinks.size());
		for (Id<Link> linkID : mostFeasibleLinks.keySet()) {
			allTransitRouteStopFacilities.add(mostFeasibleLinks.get(linkID).dominantStopFacility);
		}
		FacilitiesWriter facilitiesWriter = new FacilitiesWriter((ActivityFacilities) allTransitRouteStopFacilities); // !!! FAILS
		facilitiesWriter.write("zurich_1pm/created_input/newFacilities.xml");
	}
	
}
