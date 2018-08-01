package ch.ethz.matsim.students.samark;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class DemandEngine {

	public static Population createNewDemand(Scenario scenario, Network network, double networkSize, int nNewPeople, String populationPrefix) {
		double maxDistanceToKindergarten = networkSize/3;
		double minDistanceToKindergarten = 0.0;

		Population population = scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();
		
	// for nNewPeople people	
		for (int p=1; p<=nNewPeople; p++) {																
			Id<Person> newPersonID = Id.createPersonId(populationPrefix+"Person_"+p);
			Person newPerson = populationFactory.createPerson(newPersonID);
			if (population.getPersons().containsKey(newPersonID)) {
				population.removePerson(newPersonID);
			}
			population.addPerson(newPerson);
			Plan newPlan = populationFactory.createPlan();
			newPerson.addPlan(newPlan);
			
		// make home activity for current person // TODO merge both of these
			Link homeLink = randomLinkGenerator(network);
	System.out.println("Link name is: "+homeLink.getId().toString());
	System.out.println("Link toNode coords are: "+homeLink.getToNode().getCoord().toString());
	System.out.println("Link fromNode coords are: "+homeLink.getFromNode().getCoord().toString());
			Coord homeCoord = linkToRandomCoord(homeLink);
	System.out.println("Between coords are: "+homeCoord.toString());
			double minHomeStay = 6.0*60*60;
			double leaveTimeFrame = 2.0*60*60;
			createAndAddHomeActivityToPlan(population, newPlan, "home", homeLink, homeCoord, minHomeStay, leaveTimeFrame);			

		// make kindergarten activity for person
			// TODO merge both of these
			if(new Random().nextDouble() < 0.2) {
				Link kindergartenLink = confinedLinkGenerator(newPerson, network, networkSize, homeCoord, minDistanceToKindergarten, maxDistanceToKindergarten);
				Coord kindergartenCoord = linkToRandomCoord(homeLink);
				double dropOffDuration =15*60;
				// TODO make a map where all persons with kindergarten kids are stored in order to know if they have tp fetch them again!
				// TODO create leg to kindergarten
				
				createAndAddActivityToPlan(population, newPlan, "kindergarten", kindergartenLink, kindergartenCoord, dropOffDuration);			
			}
			
		// make work activity for current person
			double minDistanceToWork = networkSize/4;
			double maxDistanceToWork = networkSize;
			Link workLink = confinedLinkGenerator(newPerson, network, networkSize, homeCoord, minDistanceToWork, maxDistanceToWork);	
		}
		return scenario.getPopulation();
	}
	
	public static void createAndAddActivityToPlan(Population population, Plan plan, String actName, Link actLink, Coord actCoord, double duration) {
		Activity activity = population.getFactory().createActivityFromCoord(actName, actCoord);
		activity.setLinkId(actLink.getId());
		Random r = new Random();
		activity.setEndTime(duration);
		plan.addActivity(activity);
	}
	
	public static Link randomLinkGenerator(Network network) {
		int nLinks = network.getLinks().size();
		Random r = new Random();
		int randomLinkNr = r.nextInt(nLinks);
		int counter = 0;
		for (Link link : network.getLinks().values()) {
			if(counter == randomLinkNr) {
				Link randomLink = link;
				return randomLink;
			}
			counter++;
		}
		System.out.println("Error: No random link has been selected.");
		return null;
	}
	
	public static Link confinedLinkGenerator(Person person, Network network, double networkSize, Coord homeCoord, double minDistanceToNextActivity, double maxDistanceToNextActivity) {
		Link nextActivityLink;
		do {
			nextActivityLink = randomLinkGenerator(network);
		} while(distanceBetweenCoords(homeCoord, linkToRandomCoord(nextActivityLink)) < minDistanceToNextActivity || maxDistanceToNextActivity > distanceBetweenCoords(homeCoord, linkToRandomCoord(nextActivityLink)));
		return nextActivityLink;
	}
	
	public static double distanceBetweenCoords(Coord coord1, Coord coord2) {
		return Math.sqrt((coord1.getX()-coord2.getX())*(coord1.getX()-coord2.getX())+(coord1.getY()-coord2.getY())*(coord1.getY()-coord2.getY()));
	}
	
	
	public static Coord linkToRandomCoord(Link link) {
		double xStart = link.getFromNode().getCoord().getX();
		double yStart = link.getFromNode().getCoord().getY();
		double xEnd = link.getToNode().getCoord().getX();
		double yEnd = link.getToNode().getCoord().getY();
		Random rand = new Random();
		double xBetween = xStart + rand.nextDouble()*(xEnd-xStart);
		double yBetween = yStart + rand.nextDouble()*(yEnd-yStart);
		return new Coord(xBetween, yBetween);
	}
	
	public static void createAndAddHomeActivityToPlan(Population population, Plan plan, String activityName, Link homeLink, Coord homeCoord, double minHomeStay, double leaveTimeFrame){
		Activity homeActivity = population.getFactory().createActivityFromCoord(activityName, homeCoord);
		homeActivity.setLinkId(homeLink.getId());
		Random r = new Random();
		homeActivity.setEndTime(minHomeStay+r.nextDouble()*leaveTimeFrame);		// homeActivity.setEndTime(6.0*60*60+r.nextInt(2*60*60));
		plan.addActivity(homeActivity);	
		// return null;
	}
	
		
// TODO
	/*
	 // possibility to make a method activityGenerator(probability of an activity, name of activity etc.)
	
	// make home activity for person
		// Link homeLink = activityLinkGenerator(Person person, double minDistanceToNextActivity, double networkSize, double maxDistanceToNextActivity);
		// add to plan: activityGenerator(Person person, Activity activity, Link actlink, double actDuration);
	// if (0.2<= new math.random())		// add kindergarten drop-off activity with 20min drop-off time for 20% of people
		// Link homeLink = activityLinkGenerator(Person person, double networkSize, double minDistanceToNextActivity, double maxDistanceToNextActivity)
		// add to plan: activityGenerator(Person person, Activity activity, Link actlink, double actDuration);
	// add work activity (8h) for 90% of people		// add shopping activity for 30% of people
		// Link workLink = activityLinkGenerator(Person person, double networkSize, double minDistanceToNextActivity, double maxDistanceToNextActivity)
		// add to plan: activityGenerator(Person person, Activity activity, Link actlink, double actDuration);
	// if (plan includes kindergarten activity)
		// Link homeLink = activityLinkGenerator(Person person, double networkSize, double minDistanceToNextActivity, double maxDistanceToNextActivity)
		// add to plan: activityGenerator(Person person, Activity activity, Link actlink, double actDuration);
	// go home for all people
		// add to plan: activity with Person person, actHome, homeCoord, double startTime;


	// choose random links for activities (assume coordinates of activity on center of link)
		// Link actLink = activityLinkGenerator(Person person, double networkSize, double minDistanceToNextActivity, double maxDistanceToNextActivity);
		// Coord actCoord = randomCoordOnLink(Link);
	*/


	
	public static Link randomLinkSelector(Network leanedNetwork) {
		// choose random link from link list
	}
	
	int newPeople;
	Network network;
	Population population;
	
	public CreateExtraDemand(Network networkInput, Population population, int newPeople) {
		this.network = networkInput; 		// load network: load network to get nodes and links for potential workplaces
		this.newPeople = newPeople;
		this.population = population;
	}
	
	public Link randomLinkSelector(Network network) {
		int nLinks = network.getLinks().size();
		System.out.println("Amount of links: "+nLinks);				
		Random r = new Random();
		int l = r.nextInt(nLinks)+1; // don't have a node 0 and need to access also node 100, not only node 99
		System.out.println("Getting link number: "+l);
		Id<Link> randomLinkID = Id.createLinkId(l);
		System.out.println("Getting link ID: "+Id.createLinkId(l));		
		Link randomLink = network.getLinks().get(randomLinkID);
		System.out.println("Selected random node is: "+ randomLink);
		return randomLink;
	}
	
	public Node randomNodeSelector(Network network) {
		int nNodes = network.getNodes().size();
		System.out.println("Amount of nodes: "+nNodes);				
		Random r = new Random();
		int n = r.nextInt(nNodes)+1; // don't have a node 0 and need to access also node 100, not only node 99
		System.out.println("Getting node number: "+n);
		Id<Node> randomNodeID = Id.createNodeId(n);
		System.out.println("Getting node ID: "+Id.createNodeId(n));		
		Node randomNode = network.getNodes().get(randomNodeID);
		System.out.println("Selected random node is: "+ randomNode);
		return randomNode;
	}
	
	public Link outLinkSelector(Node preNode) {
		int nOutlinks = preNode.getOutLinks().size();
		Set<Id<Link>> outlinkIDs = preNode.getOutLinks().keySet();
		Random r = new Random();
		int nOut = r.nextInt(nOutlinks);
		int n = 0;
		for(Id<Link> outlinkID : outlinkIDs) {
			if (n==nOut) {
				System.out.println("Selected random link ID is: " + outlinkID.toString());
				return preNode.getOutLinks().get(outlinkID);
			}
			n++;
		}
		System.out.println("%%%%%%%%%%%%%%% Returning Null %%%%%%%%%%%%%%%%");
		return null;
	}
	
	public Node outLinkToNode(Link outLink) {
		return outLink.getToNode();
	}
	
	
	public void run() {
		PopulationFactory populationFactory = population.getFactory();
		System.out.println(populationFactory.toString());
		for (int p=1; p<this.newPeople+1; p++) {
			int intPersonID = 100+p;
			System.out.println("PersonID: "+intPersonID);
			Person person = populationFactory.createPerson(Id.createPersonId(intPersonID));
			Plan plan = populationFactory.createPlan();
			
			// make random work and home coordinates
			Link homeLink = randomLinkSelector(network);
			Node homeNode = outLinkToNode(homeLink);
			Coord homeCoord = homeNode.getCoord();
			Link workLink = outLinkSelector(homeNode);
			Node workNode = outLinkToNode(workLink);
			Coord workCoord = workNode.getCoord();
			
			//make home activity with end time and add to plan
			Activity home = populationFactory.createActivityFromCoord("h", homeCoord);
			home.setLinkId(homeLink.getId());
			Random r = new Random();
			home.setEndTime(6.0*60*60+r.nextInt(2*60*60));
			plan.addActivity(home);
			
			// make work leg by car and add to plan
			Leg toWorkLeg = populationFactory.createLeg("toWork");
			toWorkLeg.setMode("car");
			toWorkLeg.setTravelTime(5*60 + new Random().nextInt(15*60));
			LinkedList<Id<Link>> routeLinkIds = new LinkedList<Id<Link>>();
			routeLinkIds.add(homeLink.getId());
			routeLinkIds.add(workLink.getId());			
			NetworkRoute networkRoute = RouteUtils.createNetworkRoute(routeLinkIds, network);
			toWorkLeg.setRoute(networkRoute); // TODO
			plan.addLeg(toWorkLeg);
			
			// make work activity and add to plan
			Activity work = populationFactory.createActivityFromCoord("w", workCoord);
			work.setLinkId(workLink.getId());
			work.setEndTime(home.getEndTime() + toWorkLeg.getTravelTime() + 4*60*60);
			plan.addActivity(work);
			
			person.addPlan(plan);
			population.addPerson(person);							// Add persons to existing population
		}
		
		PopulationWriter pw = new PopulationWriter(population);		// Write new population to new file >> change config after that to new network name!
		pw.write("scenarios/equil/plans200.xml");
	}
	
	

	public static void main(String[] args) {
		
		Config config = ConfigUtils.loadConfig( "scenarios/equil/config.xml" ) ;
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Network network = scenario.getNetwork();
		Population population = scenario.getPopulation();
		int nNewPeople = 100;
		System.out.println(population.getPersons().toString());
		System.out.println("initiation Complete %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		CreateExtraDemand createNewPeople = new CreateExtraDemand(network, population, nNewPeople);
		createNewPeople.run();
	}
}
