package ch.ethz.matsim.students.samark;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;

public class Demo {

	public static void main(String[] args) {
		
	
	// %%%%% Network Creator Tester %%%%%	
		
		
		
		
		
		
		
		
	// %%%%% Config Tester %%%%%
		
		/* %%% Config Module Scanner %%%
		 * Takes config file and scans through its modules and parameters
		 * > Config
		 * 	>> ConfigGroup(come as a set of configGroups=Modules)
		 * 	 >>> Parameter(come as a set of parameterSet)
		 * 	  >>>> Values(one for each parameter in the set)
		 */
		Config config = ConfigUtils.createConfig();
		ConfigTester.scanConfigModules(config);
		
		/* %%% Config Modifier %%%
		 * Add ...
		 * Change ...
		 */
		ConfigTester.configModifier(config);
		
		
		//static Config		loadConfig(String filename, ConfigGroup... customModules) 
		
		/* %%% Config Writer %%%
		 */
		ConfigWriter configWriter = new ConfigWriter(config);
		configWriter.write("myOutput/ConfigScannerTestFile.xml");
		
		
		

		
		

	} // end of main method
} // end of Demo class








//%%%%%%%%%%%%%%%%%%%%%  Config Scanner %%%%%%%%%%%%%%%%%%%%%%%% Successful
/*		Config config = ConfigUtils.createConfig();								// in this case it is empty files and structures
		Scenario scenario = ScenarioUtils.createScenario(config);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(DefaultEnrichedTransitRoute.class,
				new DefaultEnrichedTransitRouteFactory());						// why do we need this again?
		//Network network = scenario.getNetwork();								// NetworkFactory netFac = network.getFactory();
		Iterator<Entry<String, ConfigGroup>> it = config.getModules().entrySet().iterator();
		while(it.hasNext()) {
			try {System.out.println(it.next().toString());}
			catch(RuntimeException RE) {
				System.out.println("had a runtime exception");
				continue;
				}
		}*/
		

		
//%%%%%%%%%%%%%%%%%%%%%  Generic Map Iterator %%%%%%%%%%%%%%%%%%%% Failed
/*		Map map = new HashMap<String, Long>();
     map.put("1$", new Long(10));
     map.put("2$", new Long(20));
     public static Link randomMapElementKey(Map map) {
 		int nElements = map.size();
 		Random r = new Random();
 		int randomElementNr = r.nextInt(nElements);
 		int counter = 0;
 		Set<?> set = map.entrySet();
         Iterator<?> iterator = set.iterator();
         iterator.next();
         if(iterator.hasNext()) {;
         	Map.Entry entry = (Entry) iterator.next();
             String valueClassType = entry.getValue().getClass().getSimpleName();
             String keyClassType = entry.getKey().getClass().getSimpleName();
             Class valueClass = entry.getValue().getClass();
             Class keyClass = entry.getKey().getClass();
             System.out.println("key type : "+keyClassType);
             System.out.println("value type : "+valueClassType);
     		for (keyClassType. elementKey : map.keySet()) {
     			if(counter == randomElementNr) {
     				 randomElement = elementKey;
     				return randomLink;
     			}
     			counter++;
     		}
     		System.out.println("Error: No random link has been selected.");
     		return null;
         }
         /* Field testMap = Test.class.getDeclaredField("map");
 	     testMap.setAccessible(true);
 	     ParameterizedType type = (ParameterizedType) testMap.getGenericType();
 	     Type key = type.getActualTypeArguments()[0];
 	     System.out.println("Key: " + key);
 	     Type value = type.getActualTypeArguments()[1];
 	     System.out.println("Value: " + value);
 	  	*/
