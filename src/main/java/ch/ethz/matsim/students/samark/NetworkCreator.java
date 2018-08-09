package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.Vehicles;

import ch.ethz.matsim.baseline_scenario.BaselineModule;
import ch.ethz.matsim.baseline_scenario.transit.BaselineTransitModule;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouteFactory;
import ch.ethz.matsim.baseline_scenario.zurich.ZurichModule;

public class NetworkCreator {

	public static void main(String[] args) {
		// TODO METRO Network

		// Run RunScenario First to have simulation output that can be analyzed
		Config originalConfig = ConfigUtils.loadConfig("zurich_1pm/zurich_config.xml"); // in this case it is empty files and structures
		Scenario originalScenario = ScenarioUtils.loadScenario(originalConfig);
		originalScenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
				new DefaultEnrichedTransitRouteFactory()); // why do we need this again?
		Network originalNetwork = originalScenario.getNetwork(); // NetworkFactory netFac = network.getFactory();
		TransitSchedule originalTransitSchedule = originalScenario.getTransitSchedule();
		
		Coord zurich_NetworkCenterCoord = new Coord(2683099.3305, 1247442.9076);
		double metroCityRadius = 1563.7356;

		// Initialize a customLinkMap
		Map<Id<Link>, CustomLinkAttributes> customLinkMap = NetworkCreatorImpl.createCustomLinkMap(originalNetwork, null);

		// Run event handler to count movements on each stop facility and add traffic
		// data to customLinkMap
		int iterationToRead = 400; // set default = 400; Which iteration to read from output of inner loop
									// simulation
		Map<Id<Link>, CustomLinkAttributes> processedLinkMap = NetworkCreatorImpl
				.runPTStopTrafficScanner(new PT_StopTrafficCounter(), customLinkMap, iterationToRead, originalNetwork, null);

		// Select all metro candidate links by setting bounds on their location (distance from city center)
		double minMetroRadiusFromCenter = metroCityRadius * 0.00; // set default = 0.00 to not restrict metro network in city center
		double maxMetroRadiusFromCenter = metroCityRadius * 2.50; // set default = 2.50 for reasonable results
		Map<Id<Link>, CustomLinkAttributes> links_withinRadius = NetworkCreatorImpl.findLinksWithinBounds(
				processedLinkMap, originalNetwork, zurich_NetworkCenterCoord, minMetroRadiusFromCenter,
				maxMetroRadiusFromCenter, "zurich_1pm/created_input/1_zurich_network_WithinRadius"
						+ ((int) Math.round(metroCityRadius)) + ".xml"); // find most frequent links from all network links

		// Find most frequent links from input links
		int nMostFrequentLinks = 100;
		Map<Id<Link>, CustomLinkAttributes> links_mostFrequentInRadius = 
				NetworkCreatorImpl.findMostFrequentLinks(nMostFrequentLinks, links_withinRadius, originalNetwork, null);

		// Set dominant transit stop facility in given network (from custom link list)
		Map<Id<Link>, CustomLinkAttributes> links_mostFrequentInRadiusMainFacilitiesSet = NetworkCreatorImpl.setMainFacilities(originalTransitSchedule, 
						originalNetwork, links_mostFrequentInRadius, "zurich_1pm/created_input/2_zurich_network_MostFrequentInRadius.xml");

		// Select all metro terminal candidates by setting bounds on their location (distance from city center)
		double minTerminalRadiusFromCenter = metroCityRadius * 0.67;
		double maxTerminalRadiusFromCenter = metroCityRadius * 1.67;
		Map<Id<Link>, CustomLinkAttributes> links_MetroTerminalCandidates = NetworkCreatorImpl.findLinksWithinBounds(links_mostFrequentInRadiusMainFacilitiesSet, 
				originalNetwork, zurich_NetworkCenterCoord, minTerminalRadiusFromCenter, maxTerminalRadiusFromCenter, "zurich_1pm/created_input/3_zurich_network_MetroTerminalCandidate.xml"); // find most frequent links

		// Create a metro network from candidate links/stopFaiclities
		double maxNewMetroLinkDistance = 0.80 * metroCityRadius;
		Network metroNetwork = NetworkCreatorImpl.createMetroNetworkFromCandidates(
				links_mostFrequentInRadiusMainFacilitiesSet, maxNewMetroLinkDistance, originalNetwork,
				"zurich_1pm/created_input/4_zurich_network_MetroNetwork.xml");
		
		
		// ----- Everything in old system up to here ----- //
		// CONVERSIONS:
		// 	Get [new map] node from [old map] refLink: Node newMapNode = newNetwork.getNodes.get(Id.createNodeId("MetroNodeLinkRef_"+oldMapRefLink.toString()))
		// 	---> Id<Node> metroNodeId = metroNodeFromOriginalLink(Id<Link> originalLinkRefID) 
		// 	Get [old map] refLink from [new map] node: Link oldMapLink = newMapNode.parse
		// 	---> Id<Link> originalLinkId = orginalLinkFromMetroNode(Id<Node> metroNodeId)

		int nRoutes = 4;
		double minTerminalDistance = 2.40 * metroCityRadius;
		ArrayList<NetworkRoute> initialMetroRoutes = NetworkCreatorImpl.createInitialRoutes(metroNetwork,
				links_MetroTerminalCandidates, nRoutes, minTerminalDistance, "zurich_1pm/created_input/5_zurich_network_MetroInitialRoutes.xml");
		Network separateRoutesNetwork = NetworkCreatorImpl.networkRoutesToNetwork(initialMetroRoutes, metroNetwork, "zurich_1pm/created_input/5_zurich_network_MetroInitialRoutes.xml");
		
		// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
		
		// Load & Create Schedules and Factories
		Config metroConfig = ConfigUtils.createConfig();								// this is totally default and may be modified as required
		Scenario metroScenario = ScenarioUtils.createScenario(metroConfig);
		TransitSchedule metroSchedule = metroScenario.getTransitSchedule();
		TransitScheduleFactory metroScheduleFactory = metroSchedule.getFactory();
		
		// Create a New Metro Vehicle
		String vehicleTypeName = "metro";  double vehicleLength = 50;  double maxVelocity = 80/3.6;	 int vehicleSeats = 100; int vehicleStandingRoom = 100;
		VehicleType metroVehicleType = PublicTransportImpl.createNewVehicleType(vehicleTypeName, vehicleLength, maxVelocity, vehicleSeats, vehicleStandingRoom);
		metroScenario.getVehicles().addVehicleType(metroVehicleType);
		
		// Generate TransitLines and Schedules on NetworkRoutes --> Add to Transit Schedule
		int nTransitLines = initialMetroRoutes.size();
		for(int lineNr=1; lineNr<=nTransitLines; lineNr++) {		
		
			// networkRoute
			NetworkRoute metroNetworkRoute = initialMetroRoutes.get(lineNr-1);
			
			// Create an array of stops along new networkRoute on the center of each of its individual links
				String defaultPtMode = "metro";  boolean blocksLane = false;  double stopTime = 45.0; /*stopDuration [s];*/  double maxVehicleSpeed = 80/3.6;  /*[m/s]*/
				List<TransitRouteStop> stopArray = PublicTransportImpl.createAndAddNetworkRouteStops(
					metroSchedule, metroNetwork, metroNetworkRoute, defaultPtMode, stopTime, maxVehicleSpeed, blocksLane);
			
				
				
			// Build TransitRoute from stops and NetworkRoute --> and add departures
				double tFirstDep = 6.0*60*60;  double tLastDep = 20.5*60*60;  double depSpacing = 15*60;  int nDepartures = (int) ((tLastDep-tFirstDep)/depSpacing);
				String vehicleFileLocation = "zurich_1pm/created_input/PT_Files/vehicles.xml";
			TransitRoute transitRoute = metroScheduleFactory.createTransitRoute(Id.create("TransitRoute_LineNr"+lineNr, TransitRoute.class ), metroNetworkRoute, stopArray, defaultPtMode);
			transitRoute = PublicTransportImpl.addDeparturesAndVehiclesToTransitRoute(metroScenario, metroSchedule, transitRoute, nDepartures, tFirstDep, depSpacing, metroVehicleType, vehicleFileLocation); // Add (nDepartures) departures to TransitRoute
						
			// Build TransitLine from TrasitRoute
			TransitLine transitLine = metroScheduleFactory.createTransitLine(Id.create("TransitLine_Nr"+lineNr, TransitLine.class));
			transitLine.addRoute(transitRoute);
			
			// Add new line to schedule
			metroSchedule.addTransitLine(transitLine);			

		}	// end of TransitLine creator loop

		// Write TransitSchedule to corresponding file
		TransitScheduleWriter tsw = new TransitScheduleWriter(metroSchedule);
		tsw.writeFile("zurich_1pm/created_input/PT_Files/MetroSchedule.xml");
				
		Network mergedNework = PublicTransportImpl.mergeRoutesNetworkToOriginalNetwork(separateRoutesNetwork, originalNetwork, "zurich_1pm/created_input/Networks/MergedNetwork.xml");
		TransitSchedule mergedTransitSchedule = PublicTransportImpl.mergeAndWriteTransitSchedules(metroSchedule, originalTransitSchedule, "zurich_1pm/created_input/PT_Files/MergedSchedule.xml");
		Vehicles mergedVehicles = PublicTransportImpl.mergeAndWriteVehicles(metroScenario.getVehicles(), originalScenario.getVehicles(), "zurich_1pm/created_input/PT_Files/MergedVehicles.xml");
		
		// --------------------------------------------------------------------------------------------------------------

		Config config0 = ConfigUtils.loadConfig("zurich_1pm/zurich_config.xml"); 		
		config0.getModules().get("controler").addParam("outputDirectory", "SimulationOutputMerged");
		config0.getModules().get("network").addParam("inputNetworkFile", "created_input/Networks/MergedNetwork.xml");
		config0.getModules().get("transit").addParam("transitScheduleFile","created_input/PT_Files/MergedSchedule.xml");
		config0.getModules().get("transit").addParam("vehiclesFile","created_input/PT_Files/MergedVehicles.xml");
		
		Scenario scenario0 = ScenarioUtils.createScenario(config0);
		scenario0.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
				new DefaultEnrichedTransitRouteFactory());
		ScenarioUtils.loadScenario(scenario0);				

		Controler controler = new Controler(scenario0);
		controler.addOverridingModule(new BaselineModule());
		controler.addOverridingModule(new BaselineTransitModule());
		controler.addOverridingModule(new ZurichModule());
		controler.run();
		
	}

}
