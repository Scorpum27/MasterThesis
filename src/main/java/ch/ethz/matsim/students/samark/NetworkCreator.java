package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouteFactory;

public class NetworkCreator {

	public static void main(String[] args) {
	// TODO METRO Network
	
		// Run RunScenario First to have simulation output that can be analyzed
		Config config = ConfigUtils.loadConfig("zurich_1pm/zurich_config.xml");					// in this case it is empty files and structures
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class, new DefaultEnrichedTransitRouteFactory());						// why do we need this again?
		Network network = scenario.getNetwork();												// NetworkFactory netFac = network.getFactory();
		
		TransitSchedule transitSchedule = scenario.getTransitSchedule();
		
		int iterationToRead = 400;				// Which iteration to read from output of inner loop simulation
		int nMostFrequentLinks = 80;
		Coord zurich_NetworkCenterCoord = new Coord(2683099.3305, 1247442.9076);
		double metroCityRadius = 1563.7356;
		
		// Select all metro candidate links by setting bounds on their location (distance from city center)
			double minMetroRadiusFromCenter = metroCityRadius*0.00;
			double maxMetroRadiusFromCenter = metroCityRadius*2.50;
		Map<Id<Link>,CustomLinkAttributes> links_allMetroCandidates = NetworkCreatorImpl.findLinksWithinBounds_FromNetwork(network, new PT_StopTrafficCounter(), iterationToRead, zurich_NetworkCenterCoord, minMetroRadiusFromCenter, maxMetroRadiusFromCenter);	// find most frequent links from all network links
		OutputTestingImpl.createNetworkFromCustomLinks(links_allMetroCandidates, network, "zurich_1pm/created_input/zurich_network_AllCandidateLinks.xml", "zurich_1pm/created_input/zurich_network_AllCandidateLinksFacilitiesNodes.xml"); // links are merged in one new network and facilities are placed as nodes in another new network
		
		// Find most frequent links from already event handled links list
		Map<Id<Link>,CustomLinkAttributes> links_HighFrequency_fromList= NetworkCreatorImpl.findMostFrequentLinks_FromEventProcessedLinksMap(nMostFrequentLinks, links_allMetroCandidates);	// find most frequent links from all network links
		// Set dominant transit stop facility in given network (from custom link list)
		Map<Id<Link>,CustomLinkAttributes> links_HighFrequency_MainFacilitiesSet = NetworkCreatorImpl.setMainFacilities(transitSchedule, network, links_HighFrequency_fromList);		
		// Write network to file
		OutputTestingImpl.createNetworkFromCustomLinks(links_HighFrequency_MainFacilitiesSet, network, "zurich_1pm/created_input/zurich_network_StrongestCandidateLinks.xml", "zurich_1pm/created_input/zurich_network_StrongestLinksFacilitiesCoordNodes.xml"); // links are merged in one new network and facilities are placed as nodes in another new network		
		
		// Select all metro terminal candidates by setting bounds on their location (distance from city center)
			double minTerminalRadiusFromCenter = metroCityRadius*0.67;
			double maxTerminalRadiusFromCenter = metroCityRadius*2.00;
		Map<Id<Link>,CustomLinkAttributes> links_MetroTerminalCandidates = NetworkCreatorImpl.findLinksWithinBounds_FromEventProcessedLinksMap(network, links_HighFrequency_MainFacilitiesSet, zurich_NetworkCenterCoord, minTerminalRadiusFromCenter, maxTerminalRadiusFromCenter);	// find most frequent links from all network links
		OutputTestingImpl.createNetworkFromCustomLinks(links_MetroTerminalCandidates, network, "zurich_1pm/created_input/zurich_network_TerminalCandidateLinks.xml", "zurich_1pm/created_input/zurich_network_TerminalCandidateFacilitiesNodes.xml"); // links are merged in one new network and facilities are placed as nodes in another new network
		
		double maxNewMetroLinkDistance = metroCityRadius;
		Network newMetroNetwork = NetworkCreatorImpl.createMetroNetworkFromCandidates(links_HighFrequency_MainFacilitiesSet, maxNewMetroLinkDistance, network);
		// Maybe add this argument to newMetroSchedule: TransitAttributes transitAttributes = new TransitAttributes([Vehicle, etc...]);
		int nRoutes = 5;
		ArrayList<NetworkRoute> initialNetworkRoutes = NetworkCreatorImpl.createInitialRoutes(newMetroNetwork, links_MetroTerminalCandidates, nRoutes);
		TransitSchedule newMetroSchedule = NetworkCreatorImpl.createNewTransitScheduleForNetwork(initialNetworkRoutes, newMetroNetwork);
			// include: create new vehicle type
			// write all new stop facilities
			// include: modify stop facilities
			// include: introduce departures etc
			Network fromNetwork;
			Network toNetwork;
		Network extendedNetwork = NetworkCreatorImpl.addNetworkPlusTransitSchedule(fromNetwork, toNetwork);
			// include check if underlying properties such as coordinate system are compatible!
			// add all nodes and links of new network
			// connect explicitly every facility node to to/fromNode of that link
			// transitschedule:
				// loop through all facilities and routes etc.
				// merge vehicles explicitly with files etc.
		
		
	
	
	}
	
}
