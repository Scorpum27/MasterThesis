package ch.ethz.matsim.students.samark;

import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
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
		int nMostFrequentLinks = 300;
		
		// Use this section if you want to find all links/facilities above a certain traffic threshold (arrivals + departures)
			//double threshold = 2.0;
			//Map<Id<Link>,CustomLinkAttributes> linksAboveThreshold = StopLocationGenerator.findLinksAboveThreshold(network, threshold, new PT_StopTrafficCounter(), iterationToRead);
			//Map<Id<Link>,CustomLinkAttributes> mostFrequentLinks= StopLocationGenerator.findMostFrequentLinks(network, nMostFrequentLinks, new PT_StopTrafficCounter(), iterationToRead, linksAboveThreshold); // find most frequent links from a threshold selection
			//Please put in commentary style next line to avoid double execution of methods
		
		Map<Id<Link>,CustomLinkAttributes> mostFrequentLinks= StopLocationGenerator.findMostFrequentLinks(network, nMostFrequentLinks, new PT_StopTrafficCounter(), iterationToRead, null);	// find most frequent links from all network links
		Map<Id<Link>,CustomLinkAttributes> mostFeasibleLinks = DominantStopFacilitiesOnLinks.find(transitSchedule, network, mostFrequentLinks);		
		Coord zurich_NetworkCenterCoord = new Coord(2683099.3305, 1247442.9076);
		double minRadiusFromCenter = 1563.7356;
		Map<Id<Link>,CustomLinkAttributes> feasibleTerminalCandidateLinks= DominantStopFacilitiesOnLinks.findFeasibleTerminalLinks(network, new PT_StopTrafficCounter(), iterationToRead, mostFeasibleLinks, zurich_NetworkCenterCoord, minRadiusFromCenter);	// find most frequent links from all network links
		OutputTestingImpl.createNetworkFromCustomLinks(mostFeasibleLinks, network); // links are merged in one new network and facilities are placed as nodes in another new network		
		OutputTestingImpl.createNetworkFromCustomLinks(feasibleTerminalCandidateLinks, network); // links are merged in one new network and facilities are placed as nodes in another new network
	}
	
}
