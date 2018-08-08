package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
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
		Config config = ConfigUtils.loadConfig("zurich_1pm/zurich_config.xml"); // in this case it is empty files and
																				// structures

		Scenario scenario = ScenarioUtils.loadScenario(config);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
				new DefaultEnrichedTransitRouteFactory()); // why do we need this again?
		Network network = scenario.getNetwork(); // NetworkFactory netFac = network.getFactory();
		TransitSchedule transitSchedule = scenario.getTransitSchedule();

		Coord zurich_NetworkCenterCoord = new Coord(2683099.3305, 1247442.9076);
		double metroCityRadius = 1563.7356;

		// Initialize a customLinkMap
		Map<Id<Link>, CustomLinkAttributes> customLinkMap = NetworkCreatorImpl.createCustomLinkMap(network, null);

		// Run event handler to count movements on each stop facility and add traffic
		// data to customLinkMap
		int iterationToRead = 400; // set default = 400; Which iteration to read from output of inner loop
									// simulation
		Map<Id<Link>, CustomLinkAttributes> processedLinkMap = NetworkCreatorImpl
				.runPTStopTrafficScanner(new PT_StopTrafficCounter(), customLinkMap, iterationToRead, network, null);

		// Select all metro candidate links by setting bounds on their location (distance from city center)
		double minMetroRadiusFromCenter = metroCityRadius * 0.00; // set default = 0.00 to not restrict metro network in city center
		double maxMetroRadiusFromCenter = metroCityRadius * 2.50; // set default = 2.50 for reasonable results
		Map<Id<Link>, CustomLinkAttributes> links_withinRadius = NetworkCreatorImpl.findLinksWithinBounds(
				processedLinkMap, network, zurich_NetworkCenterCoord, minMetroRadiusFromCenter,
				maxMetroRadiusFromCenter, "zurich_1pm/created_input/1_zurich_network_WithinRadius"
						+ ((int) Math.round(metroCityRadius)) + ".xml"); // find most frequent links from all network links

		// Find most frequent links from input links
		int nMostFrequentLinks = 100;
		Map<Id<Link>, CustomLinkAttributes> links_mostFrequentInRadius = 
				NetworkCreatorImpl.findMostFrequentLinks(nMostFrequentLinks, links_withinRadius, network, null);

		// Set dominant transit stop facility in given network (from custom link list)
		Map<Id<Link>, CustomLinkAttributes> links_mostFrequentInRadiusMainFacilitiesSet = NetworkCreatorImpl.setMainFacilities(transitSchedule, 
						network, links_mostFrequentInRadius, "zurich_1pm/created_input/2_zurich_network_MostFrequentInRadius.xml");

		// Select all metro terminal candidates by setting bounds on their location (distance from city center)
		double minTerminalRadiusFromCenter = metroCityRadius * 0.67;
		double maxTerminalRadiusFromCenter = metroCityRadius * 1.67;
		Map<Id<Link>, CustomLinkAttributes> links_MetroTerminalCandidates = NetworkCreatorImpl.findLinksWithinBounds(links_mostFrequentInRadiusMainFacilitiesSet, 
				network, zurich_NetworkCenterCoord, minTerminalRadiusFromCenter, maxTerminalRadiusFromCenter, "zurich_1pm/created_input/3_zurich_network_MetroTerminalCandidate.xml"); // find most frequent links

		// Create a metro network from candidate links/stopFaiclities
		double maxNewMetroLinkDistance = 0.80 * metroCityRadius;
		Network newMetroNetwork = NetworkCreatorImpl.createMetroNetworkFromCandidates(
				links_mostFrequentInRadiusMainFacilitiesSet, maxNewMetroLinkDistance, network,
				"zurich_1pm/created_input/4_zurich_network_MetroNetwork.xml");
		
		// Conversions:
		// get [new map] node from [old map] refLink: Node newMapNode = newNetwork.getNodes.get(Id.createNodeId("MetroNodeLinkRef_"+oldMapRefLink.toString()))
		// get [old map] refLink from [new map] node: Link oldMapLink = newMapNode.parse
		// ...

		int nRoutes = 6;
		double minTerminalDistance = 2.60 * metroCityRadius;
		ArrayList<NetworkRoute> initialMetroRoutes = NetworkCreatorImpl.createInitialRoutes(newMetroNetwork,
				links_MetroTerminalCandidates, nRoutes, minTerminalDistance,
				"zurich_1pm/created_input/5_zurich_network_MetroInitialRoutes.xml");

		/*
		 * TransitSchedule newMetroSchedule =
		 * NetworkCreatorImpl.createNewTransitScheduleForNetwork(initialNetworkRoutes,
		 * newMetroNetwork); // include: create new vehicle type // write all new stop
		 * facilities // include: modify stop facilities // include: introduce
		 * departures etc. Network fromNetwork; Network toNetwork; Network
		 * extendedNetwork =
		 * NetworkCreatorImpl.addNetworkPlusTransitSchedule(fromNetwork, toNetwork); //
		 * include check if underlying properties such as coordinate system are
		 * compatible! // add all nodes and links of new network // connect explicitly
		 * every facility node to to/fromNode of that link // transitschedule: // loop
		 * through all facilities and routes etc. // merge vehicles explicitly with
		 * files etc.
		 */

	}

}
