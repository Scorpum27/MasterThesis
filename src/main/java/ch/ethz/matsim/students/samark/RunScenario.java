package ch.ethz.matsim.students.samark;

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
import org.matsim.core.controler.Controler;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import ch.ethz.matsim.baseline_scenario.BaselineModule;
import ch.ethz.matsim.baseline_scenario.transit.BaselineTransitModule;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouteFactory;
import ch.ethz.matsim.baseline_scenario.zurich.ZurichModule;

public class RunScenario {
	
static public void main(String[] args) {
		Config config = ConfigUtils.loadConfig(args[0]);
		
		// config.transit().setTransitScheduleFile(null); // potentially "null", but should set TransitSchedule

		Scenario scenario = ScenarioUtils.createScenario(config);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
				new DefaultEnrichedTransitRouteFactory());
		ScenarioUtils.loadScenario(scenario);
		
		/*new TransitScheduleReader(scenario).readFile(filename);
		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(filename);

		TransitSchedule transitSchedule = scenario.getTransitSchedule();
		
		transitSchedule.addTransitLine(line);
		transitSchedule.removeTransitLine(line); 
		
		TransitScheduleFactory factory = scenario.getTransitSchedule().getFactory();
		
		TransitLine line = factory.createTransitLine(Id.create("myline", TransitLine.class));
		transitSchedule.addTransitLine(line);
		
		NetworkRoute networkROute = new LinkNetworkRouteFactory().createRoute(startLinkId, endLinkId);
		
		TransitRoute route = factory.createTransitRoute(routeId, route, stops, mode);
		line.addRoute(route);
		
		route.addDeparture(departure); */
		
		Controler controler = new Controler(scenario);

		controler.addOverridingModule(new BaselineModule());
		controler.addOverridingModule(new BaselineTransitModule());
		controler.addOverridingModule(new ZurichModule());

		controler.run();
	}
	
}


