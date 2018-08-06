package ch.ethz.matsim.students.samark;


import java.util.Iterator;
import java.util.Map.Entry;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;


public class ConfigTester {

	// Config > ConfigGroup(come as a set of configGroups=Modules) > Parameter(come as a set of parameterSet) > Values(one for each parameter in the set)
	public static void scanConfigModules(Config config) {
		Iterator<Entry<String, ConfigGroup>> it = config.getModules().entrySet().iterator();
		while(it.hasNext()) {
			try {System.out.println(it.next().toString());}
			catch(RuntimeException RE) {
				System.out.println("had a runtime exception");
				continue;
				}
		}
	}
	
	
	// Create and add new modules
		public static void configModifier(Config config) {
			
			System.out.println("Creating a new configModule ... ");
			ConfigGroup myConfigModule1 = new ConfigGroup("myConfigModule1");
			myConfigModule1.addParam("SpeedFactor", "Highspeed_100");
			myConfigModule1.addParam("Strategy", "Drive_Fast");
			
			ConfigGroup myConfigModule2 = new ConfigGroup("myConfigModule2");
			myConfigModule2.addParam("SpeedFactor2", "Lowspeed_50");
			myConfigModule2.addParam("Strategy2", "Drive_Slow");
			
			System.out.println("Name: "+myConfigModule1.getName().toString());
			System.out.println("Parameters: "+myConfigModule1.getParams().entrySet().toString());
			System.out.println("ParameterSets: "+myConfigModule1.getParameterSets().entrySet().toString());

			myConfigModule1.addParameterSet(myConfigModule2);
			System.out.println("Added new module: "+myConfigModule2.getName().toString());
			
			System.out.println("Name: "+myConfigModule1.getName().toString());
			System.out.println("Parameters: "+myConfigModule1.getParams().entrySet().toString());
			System.out.println("ParameterSets: "+myConfigModule1.getParameterSets().entrySet().toString());
			
			config.addModule(myConfigModule1);
			if(config.getModules().containsKey(myConfigModule1.getName().toString())) {
				config.getModules().remove(myConfigModule1.getName().toString());
				System.out.println("Had to remove "+myConfigModule1.getName().toString());
				config.addModule(myConfigModule1);
			}
			config.addModule(myConfigModule2);

		}
	
}
