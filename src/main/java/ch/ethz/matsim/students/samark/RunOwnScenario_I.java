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
import org.matsim.vehicles.FreightCapacity;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.Vehicles;
import org.matsim.vehicles.VehiclesFactory;


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
				
	// load, create & process pt network
		TransitSchedule transitSchedule = scenario.getTransitSchedule();						// Create TransitSchedule placeholder and a factory
		TransitScheduleFactory transitScheduleFactory = transitSchedule.getFactory();

	// make a new vehicleType
		String vehicleTypeName = "MagicalBus";
		double vehicleLength = 15;
		double maxVelocity = 100/3.6;
		int vehicleSeats = 15;
		int vehicleStandingRoom = 15;
		PublicTransportEngine.createNewVehicleType(scenario, vehicleTypeName, vehicleLength, maxVelocity, vehicleSeats, vehicleStandingRoom);

	// Make (nTransitLines) new TransitLines from random networkRoutes in given network 
	// TODO make a method for entire loop: 
	// TODO newNetworkWithTransitSchedule(Scenario scenario, int nTransitLines, int outerFramePercentage, int minSpacingPercentage,
	//										String defaultPtMode, boolean blocksLane, double stopTime, double vehicleSpeed, int nDepartures, double firstDepTime, double departureSpacing)	
		
		int nTransitLines = 10;
		for(int lineNr=0; lineNr<nTransitLines; lineNr++) {		
		
			// RandomNetworkRouteGenerator
			// - Chooses random start and end node on frame with a minimum Euclidean spacing between them
			// - Creates shortest path route between them with Dijkstra's			
			// - TODO OPTIONAL: make iterator dependent (r) names for networkRoute and transitRoute
				int outerFramePercentage = 40;				// take only nodes on outer 50% of network (specify frame as outer 50% of network nodes)
				int minSpacingPercentage = 60;				// minimum spacing requirement between start and end node of a route so that sample routes are not too short!
			NetworkRoute networkRoute = NetworkRouteCreator.create(networkThin, XMax, YMax, outerFramePercentage, minSpacingPercentage);			// make a shortest path networkRoute between two random nodes in the outer regions of the network				
			ShortestPath.createAndWriteNetwork(config, networkThin, networkRoute, lineNr, XMax, YMax, removalPercentage); 									// Store new network here consisting solely of shortest path route in order to display in VIA/ (or can I store route as such?) // or: Network shortestPathNetwork = ShortestPath.createAndWriteNetwork(...);
			
			// Create an array of stops along new networkRoute on the center of each of its individual links
				String defaultPtMode = "bus";
				boolean blocksLane = false;
				double stopTime = 30.0; 					// stop duration for vehicle in [seconds]
				double vehicleSpeed = 2.0/60; 				// 120 unit_link_lengths/hour = 2 unit_link_lengths/minute = 2/60 unit_link_lengths/second
			List<TransitRouteStop> stopArray = PublicTransportEngine.networkRouteStopsAllLinks(
					transitSchedule, networkThin, networkRoute, defaultPtMode, stopTime, vehicleSpeed, blocksLane);
			
			// Build TransitRoute from stops and NetworkRoute --> and add departures
				int nDepartures = 10;
				double firstDepTime = 6.0*60*60;
				double departureSpacing = 15*60;
				VehicleType magicalBus = scenario.getVehicles().getVehicleTypes().get(Id.create(vehicleTypeName, VehicleType.class));
				String vehicleFileLocation = "myInput/PT/vehicles.xml";
			TransitRoute transitRoute = transitScheduleFactory.createTransitRoute(Id.create("transitRoute_"+lineNr, TransitRoute.class ), networkRoute, stopArray, defaultPtMode);
			transitRoute = PublicTransportEngine.addDeparturesAndVehiclesToTransitRoute(scenario, transitSchedule, transitRoute, nDepartures, firstDepTime, departureSpacing, magicalBus, vehicleFileLocation); // Add (nDepartures) departures to TransitRoute
			
			
			// Build TransitLine from TrasitRoute
			TransitLine transitLine = transitScheduleFactory.createTransitLine(Id.create("transitLine_"+lineNr, TransitLine.class));
			transitLine.addRoute(transitRoute);
			
			// Add new line to schedule
			transitSchedule.addTransitLine(transitLine);			


			
			
			
			
		}	// end of TransitLine creator loop

		// Write TransitSchedule to corresponding file
		TransitScheduleWriter tsw = new TransitScheduleWriter(transitSchedule);
		tsw.writeFile("myInput/PT/scheduleOne.xml");
		
		
	// create population by means of population factory
		Population population = scenario.getPopulation();
		PopulationFactory popFac = population.getFactory();
		
	
	}

}
