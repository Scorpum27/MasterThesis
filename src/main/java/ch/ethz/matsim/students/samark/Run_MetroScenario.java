package ch.ethz.matsim.students.samark;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.matsim.baseline_scenario.BaselineModule;
import ch.ethz.matsim.baseline_scenario.transit.BaselineTransitModule;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRoute;
import ch.ethz.matsim.baseline_scenario.transit.routing.DefaultEnrichedTransitRouteFactory;
import ch.ethz.matsim.baseline_scenario.zurich.ZurichModule;

public class Run_MetroScenario {

	public static void main(String[] args) {
		
		// store new config files here! Make extra method!
		
		Config config0 = ConfigUtils.loadConfig("zurich_1pm/zurich_config.xml"); 		
		config0.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
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
