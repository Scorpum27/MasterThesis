package ch.ethz.matsim.students.samark;

public class Demo {

	public static void main(String[] args) {
		
	
	// %%%%% Network Route Creator Tester %%%%%	
		
		/*Network routesNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		NetworkFactory nf = routesNetwork.getFactory();
		Node n1 = nf.createNode(Id.createNodeId("node1"), new Coord(0.0, 0.0));
		Node n2 = nf.createNode(Id.createNodeId("node2"), new Coord(1.0, 0.0));
		Node n3 = nf.createNode(Id.createNodeId("node3"), new Coord(2.0, 0.0));
		Node n4 = nf.createNode(Id.createNodeId("node4"), new Coord(3.0, 0.0));
		routesNetwork.addNode(n1);
		routesNetwork.addNode(n2);
		routesNetwork.addNode(n3);
		routesNetwork.addNode(n4);
		Link l1 = nf.createLink(Id.createLinkId("link1"), n1, n2);
		Link l2 = nf.createLink(Id.createLinkId("link2"), n2, n3);
		Link l3 = nf.createLink(Id.createLinkId("link3"), n1, n4);
		Link l4 = nf.createLink(Id.createLinkId("link4"), n4, n3);
		routesNetwork.addLink(l1);
		routesNetwork.addLink(l2);
		routesNetwork.addLink(l3);
		routesNetwork.addLink(l4);
		ArrayList<Id<Link>> linkList = new ArrayList<Id<Link>>();
		for (int i =1; i<1+4; i++) {
			linkList.add(Id.createLinkId("link"+i));
		}
		ArrayList<Id<Link>> linksBetween = new ArrayList<Id<Link>>(linkList.size()-2);
		for (int i = 0; i<linksBetween.size(); i++) {
			linksBetween.add(linkList.get(i+1));
		}		
		NetworkRoute nr = RouteUtils.createNetworkRoute(linkList, routesNetwork);
		System.out.println(nr.getLinkIds().toString());
		System.out.println(nr.toString());
		*/
		
		
		
		
	// %%%%% Config Tester %%%%%
		
		/* %%% Config Module Scanner %%%
		 * Takes config file and scans through its modules and parameters
		 * > Config
		 * 	>> ConfigGroup(come as a set of configGroups=Modules)
		 * 	 >>> Parameter(come as a set of parameterSet)
		 * 	  >>>> Values(one for each parameter in the set)
		 */
		/*Config config = ConfigUtils.createConfig();
		ConfigTester.scanConfigModules(config);*/
		
		/* %%% Config Modifier %%%
		 * Add ...
		 * Change ...
		 */
		//ConfigTester.configModifier(config);
		
		
		//static Config		loadConfig(String filename, ConfigGroup... customModules) 
		
		/* %%% Config Writer %%%
		 */
		/*ConfigWriter configWriter = new ConfigWriter(config);
		configWriter.write("myOutput/ConfigScannerTestFile.xml");*/
		
		
		

		
		

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
