package ch.ethz.matsim.students.samark;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class TesterInfraGenerator {


	static public void main(String[] args) {
	
/* TESTED
	// load & create configuration and scenario
		Config config = ConfigUtils.createConfig();								// in this case it is empty files and structures
		Scenario scenario = ScenarioUtils.createScenario(config);
*/
		
/* TESTED
	// load, create & process network	
		final int XMax = 10;													// set network size in West-to-East
		final int YMax = 5;														// set network size in South-to-North
		
		Network network = scenario.getNetwork();								// NetworkFactory netFac = network.getFactory();
		network = networkFiller.fill(XMax, YMax, network);						// Fill up network with nodes between XMax and YMax to make a perfect node grid.
																				// These nodes can be used as potential stop locations in a perfect and uniform network.
		NetworkWriter nw = new NetworkWriter(network);
		String filepath = "myOutput/network_"+XMax+"x"+YMax+".xml";
		nw.write(filepath);
*/



		
/*
	// load, create & process pt network
*/	
		
		
		
/*
	// create population by means of population factory
		Population population = scenario.getPopulation();
		PopulationFactory popFac = population.getFactory();
*/
		
	}

}
