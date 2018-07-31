package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;


public class RunOwnScenario_I {


	static public void main(String[] args) {
		
	// load & create configuration and scenario
		Config config = ConfigUtils.createConfig();								// in this case it is empty files and structures
		Scenario scenario = ScenarioUtils.createScenario(config);
		Network network = scenario.getNetwork();								// NetworkFactory netFac = network.getFactory();

	// load, create & process network	
		final int XMax = 30;													// set network size in West-to-East
		final int YMax = 30;													// set network size in South-to-North		
		network = networkFiller.fill(XMax, YMax, network);						// Fill up network with nodes between XMax and YMax to make a perfect node grid - These nodes can be used as potential stop locations in a perfect and uniform network.
		networkFiller.writeToFile(XMax, YMax, network);
		
		
	// make a thinner and more realistic network by removing a percentage of nodes and its connecting links
		int removalPercentage = 25;
		boolean writeToFile = true;																			// if we want to keep 
		Network networkThin = networkFiller.thin(network, XMax, YMax, removalPercentage, writeToFile);		// make new static method in networkFiller		
	
	// TODO create loading function here to load a specific network that can be compared over several runs

		
	// Display all (remaining) nodes of current desired network
		/* 
		 * for (Id<Node> nID : networkThin.getNodes().keySet()) {
		 * System.out.println(nID.toString());}
		 */
		
	// load, create & process pt network
		TransitSchedule transitSchedule = scenario.getTransitSchedule();						// Create TransitSchedule placeholder and a factory
		TransitScheduleFactory transitScheduleFactory = transitSchedule.getFactory();
		

		int outerFramePercentage = 40;				// take only nodes on outer 50% of network (specify frame as outer 50% of network nodes)
		int minSpacingPercentage = 60;				// minimum spacing requirement between start and end node of a route so that sample routes are not too short!
		int nTransitLines = 10;
		String defaultPtMode = "bus";
		boolean blocksLane = false;
		
	// Make (nTransitLines) new TransitLines from random networkRoutes in given network 
		for(int r=0; r<nTransitLines; r++) {		
		
			// RandomNetworkRouteGenerator
			// - Chooses random start and end node on frame with a minimum Euclidean spacing between them
			// - Creates shortest path route between them with Dijkstra's			
			// - TODO OPTIONAL: make iterator dependent (r) names for networkRoute and transitRoute
			NetworkRoute networkRoute = NetworkRouteCreator.create(networkThin, XMax, YMax, outerFramePercentage, minSpacingPercentage);			// make a shortest path networkRoute between two random nodes in the outer regions of the network				
			ShortestPath.createAndWriteNetwork(config, networkThin, networkRoute, XMax, YMax, removalPercentage); 									// Store new network here consisting solely of shortest path route in order to display in VIA/ (or can I store route as such?) // or: Network shortestPathNetwork = ShortestPath.createAndWriteNetwork(...);
			
			// Create an array of stops along new networkRoute on the center of each of its individual links
			double stopTime = 30.0; 			// stop duration for vehicle in [seconds]
			double vehicleSpeed = 2.0/60; 		// 120 unit_link_lengths/hour = 2 unit_link_lengths/minute = 2/60 unit_link_lengths/second
			List<TransitRouteStop> stopArray = PublicTransportEngine.networkRouteStopsAllLinks(transitSchedule, networkThin, networkRoute, defaultPtMode, stopTime, vehicleSpeed, blocksLane);
			
			// Build TransitRoute from stops and NetworkRoute --> and add departures
			TransitRoute transitRoute = transitScheduleFactory.createTransitRoute(Id.create("transitRoute_"+r, TransitRoute.class ), networkRoute, stopArray, defaultPtMode);
			// Add (nDepartures) departures to TransitRoute
			int nDepartures = 10;
			double firstDepTime = 6.0*60*60;
			double departureSpacing = 15*60;
			transitRoute = PublicTransportEngine.addDeparturesToTransitRoute(transitSchedule, transitRoute, nDepartures, firstDepTime, departureSpacing);
			TransitLine transitLine = transitScheduleFactory.createTransitLine(Id.create("transitLine_"+r, TransitLine.class));
			transitLine.addRoute(transitRoute);
			transitSchedule.addTransitLine(transitLine);
			TransitScheduleWriter tsw = new TransitScheduleWriter(transitSchedule);
			tsw.writeFile("myInput/PT/scheduleOne.xml");
			
		}

		
	// create population by means of population factory
		Population population = scenario.getPopulation();
		PopulationFactory popFac = population.getFactory();
		
	
	}

}
