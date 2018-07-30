package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

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
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;


public class RunOwnScenario_I {


	static public void main(String[] args) {
		
	// load & create configuration and scenario
		Config config = ConfigUtils.createConfig();								// in this case it is empty files and structures
		Scenario scenario = ScenarioUtils.createScenario(config);
		
	// load, create & process network	
		final int XMax = 43;													// set network size in West-to-East
		final int YMax = 18;														// set network size in South-to-North
		
		Network network = scenario.getNetwork();								// NetworkFactory netFac = network.getFactory();
		network = networkFiller.fill(XMax, YMax, network);						// Fill up network with nodes between XMax and YMax to make a perfect node grid.
																				// These nodes can be used as potential stop locations in a perfect and uniform network.
		NetworkWriter nw = new NetworkWriter(network);
		String filepath = "myOutput/network_"+XMax+"x"+YMax+".xml";
		nw.write(filepath);
		
		
	// make a thinner and more realistic network by removing a percentage of nodes and its connecting links
		int removalPercentage = 25;
		boolean writeToFile = true;																			// if we want to keep 
		String filepathThin = "myOutput/network_"+XMax+"x"+YMax+"_Thin"+removalPercentage+".xml";
		Network networkThin = networkFiller.thin(network, removalPercentage, writeToFile, filepathThin);	// make new static method in networkFiller		
	
	// TODO create loading function here to load a specific network that can be compared over several runs

		
	// Display all (remaining) nodes of current desired network
		/* 
		 * for (Id<Node> nID : networkThin.getNodes().keySet()) {
		 * System.out.println(nID.toString());}
		 */

		
	// load, create & process pt network
		TransitSchedule transitSchedule = scenario.getTransitSchedule();				// Create TransitSchedule placeholder and a factory
		TransitScheduleFactory transitScheduleFactory = transitSchedule.getFactory();
		
		
	// RandomNetworkRouteGenerator
		// - Chooses random start and end node on frame with a minimum Euclidean spacing between them
	    // - Creates shortest path route between them with Dijkstra's
		
		int outerFramePercentage = 40;				// take only nodes on outer 50% of network (specify frame as outer 50% of network nodes)
		int minSpacingPercentage = 60;				// minimum spacing requirement between start and end node of a route so that sample routes are not too short!
		ArrayList<Node> routeNodeList = new ArrayList<Node>();
		do{
			routeNodeList = RandomRouteGeneratorShortest.createRandomRoute(networkThin, XMax, YMax, outerFramePercentage, minSpacingPercentage); 		// makes random starting points in network in outer network regions
		} while(routeNodeList==null);
		NetworkRoute networkRoute = NodeListToNetworkRoute.convert(networkThin, routeNodeList);																		// convert from node list format to network route by connecting the corresponding links
		
					// iterate through nodes on resulting list
					/* System.out.println("routeNodeListLength is: "+routeNodeList.size());	
					ListIterator<Node> netRouteIter = routeNodeList.listIterator();
					while(netRouteIter.hasNext()) {
						System.out.println("Current node is: "+netRouteIter.next().getId().toString());	
					} */

		
	// Store new network here consisting solely of shortest path route in order to display in VIA/ (or can I store route as such?)
		Network shortestPathNetwork = ShortestPath.createNetwork(config, networkThin, networkRoute);
		NetworkWriter nwShortestPath = new NetworkWriter(shortestPathNetwork);
		String filepathShortestPath = "myOutput/network_"+XMax+"x"+YMax+"_Thin"+removalPercentage+"_ShortestPath.xml";
		nwShortestPath.write(filepathShortestPath);
		
	/*
		// Create link list >> make NetworkRoute from link list
				Network network = scenario.getNetwork();
				NetworkFactory networkFactory = network.getFactory();
				Node node = networkFactory.createNode(Id.createNodeId(0), new Coord(-10, 10));
				Link link = networkFactory.createLink(Id.createLinkId("0_1"), node0, node1);
				RouteFactories = PopulationFactory.getRouteFactories();
				NetworkRoute networkRoute = RouteUtils.createNetworkRoute(List<Id<Link>> routeLinkIds, Network network); // alternatively maybe: Route route = RouteFactories.createRoute(Class<R> routeClass, Id<Link> startLinkId, Id<Link> endLinkId);
			// Create stop list (TransitStopFacilities)
				TransitStopFacility tStopFacility = transitScheduleFactory.createTransitStopFacility(Id<TransitStopFacility> facilityId, Coord coordinate, boolean blocksLane)
				TransitRouteStop tRouteStop = transitScheduleFactory.createTransitRouteStop(TransitStopFacility stop, double arrivalDelay, double departureDelay)
			// Build TransitRoute from stops and NetworkRoute
				TransitRoute tRoute = transitScheduleFactory.createTransitRoute(routeId, networkRoute, stops, mode);
			// Add departures to TransitRoute
				departure = transitScheduleFactory.createDeparture(Id<Departure> departureId, double time);
				tRoute.addDeparture(departure)
			// Make TransitLine and add TransitRoute to line
				transitLine = transitScheduleFactory.createTransitLine(Id<TransitLine> lineId)
			// Make TransitScheduleWriter
				new TransitScheduleWriter(tSchedule).writeFile("input/hapt/newschedule.xml");
		*/

		
	// create population by means of population factory
		Population population = scenario.getPopulation();
		PopulationFactory popFac = population.getFactory();
		
	
	}

}
