package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

public class NetworkCreatorImpl {

	public static Map<Id<Link>, CustomLinkAttributes> createCustomLinkMap(Network network, String fileName) {
		Map<Id<Link>, CustomLinkAttributes> customLinkMap = new HashMap<Id<Link>, CustomLinkAttributes>(
				network.getLinks().size());
		Iterator<Id<Link>> iterator = network.getLinks().keySet().iterator(); // take network and put all links in
																				// linkTrafficMap
		while (iterator.hasNext()) {
			Id<Link> thisLinkID = iterator.next();
			customLinkMap.put(thisLinkID, new CustomLinkAttributes()); // - initiate traffic with default attributes
		}

		if (fileName != null) {
			OutputTestingImpl.createNetworkFromCustomLinks(customLinkMap, network, fileName);
		}

		return customLinkMap;
	}

	public static Map<Id<Link>, CustomLinkAttributes> findLinksAboveThreshold(Network network, double threshold,
			Map<Id<Link>, CustomLinkAttributes> customLinkMapIn, String fileName) { // output is a map with all links
																					// above threshold and their traffic
																					// (number of link enter events)

		// make a custom linkMap and initialize with all network links
		Map<Id<Link>, CustomLinkAttributes> customLinkMap = copyCustomMap(customLinkMapIn);

		// remove all links below threshold
		Map<Id<Link>, CustomLinkAttributes> linksAboveThreshold = new HashMap<Id<Link>, CustomLinkAttributes>();
		Iterator<Entry<Id<Link>, CustomLinkAttributes>> thresholdIterator = customLinkMap.entrySet().iterator();
		while (thresholdIterator.hasNext()) {
			Entry<Id<Link>, CustomLinkAttributes> entry = thresholdIterator.next();
			if (threshold <= entry.getValue().getTotalTraffic()) {
				linksAboveThreshold.put(entry.getKey(), entry.getValue());
			}
		}
		double average = OutputTestingImpl.getAverageTrafficOnLinks(linksAboveThreshold);
		System.out.println("Average pt traffic on links (person arrivals + departures) is: " + average);
		System.out.println("Number of links above threshold is: " + linksAboveThreshold.size());

		if (fileName != null) {
			OutputTestingImpl.createNetworkFromCustomLinks(linksAboveThreshold, network, fileName);
		}

		return linksAboveThreshold;
	}

	public static Map<Id<Link>, CustomLinkAttributes> findMostFrequentLinks(int nMostFrequentLinks,
			Map<Id<Link>, CustomLinkAttributes> customLinkMap, Network network, String fileName) { // output is a map
																									// with all links
																									// above threshold
																									// and their traffic
																									// (number of link
																									// enter events)

		// add links if they are within top nMostFrequentlinks
		Map<Id<Link>, CustomLinkAttributes> mostFrequentLinks = new HashMap<Id<Link>, CustomLinkAttributes>(
				nMostFrequentLinks);
		int i = 0;
		for (Id<Link> linkID : customLinkMap.keySet()) {
			mostFrequentLinks.put(linkID, customLinkMap.get(linkID));
			i++;
			if (i == nMostFrequentLinks) {
				break;
			}
		}

		// add other links from customLinkMap if they have more traffic than previous
		// minimum link
		for (Id<Link> linkID : customLinkMap.keySet()) {
			Id<Link> minTrafficLinkID = minimumTrafficLink(mostFrequentLinks);
			Double minTraffic = mostFrequentLinks.get(minimumTrafficLink(mostFrequentLinks)).getTotalTraffic();
			if (customLinkMap.get(linkID).getTotalTraffic() > minTraffic
					&& mostFrequentLinks.containsKey(linkID) == false) {
				mostFrequentLinks.put(linkID, customLinkMap.get(linkID));
				mostFrequentLinks.remove(minTrafficLinkID);
			}
		}
		// calculate and display average
		double average = OutputTestingImpl.getAverageTrafficOnLinks(mostFrequentLinks);
		System.out.println("Average pt traffic on most frequent n=" + nMostFrequentLinks
				+ " links (person arrivals + departures) is: " + average);
		System.out.println("Number of most frequent links is: " + mostFrequentLinks.size());

		if (fileName != null) {
			OutputTestingImpl.createNetworkFromCustomLinks(mostFrequentLinks, network, fileName);
		}

		return mostFrequentLinks;
	}

	public static Id<Link> minimumTrafficLink(Map<Id<Link>, CustomLinkAttributes> linkSet) {
		double minTraffic = Double.MAX_VALUE;
		Id<Link> minLinkID = null;
		for (Id<Link> linkID : linkSet.keySet()) {
			if (linkSet.get(linkID).getTotalTraffic() < minTraffic) {
				minTraffic = linkSet.get(linkID).getTotalTraffic();
				minLinkID = linkID;
			}
		}
		return minLinkID;
	}

	public static Map<Id<Link>, CustomLinkAttributes> runPTStopTrafficScanner(
			PT_StopTrafficCounter myPT_StopTrafficCounter, Map<Id<Link>, CustomLinkAttributes> emptyCustomLinkMap,
			int iterationToRead, Network network, String fileName) {

		myPT_StopTrafficCounter.CustomLinkMap = copyCustomMap(emptyCustomLinkMap);
		EventsManager myEventsManager = EventsUtils.createEventsManager();
		myEventsManager.addHandler(myPT_StopTrafficCounter);
		MatsimEventsReader reader = new MatsimEventsReader(myEventsManager);
		String eventsFile = "zurich_1pm/simulation_output/ITERS/it." + iterationToRead + "/" + iterationToRead
				+ ".events.xml.gz";
		reader.readFile(eventsFile);

		if (fileName != null) {
			OutputTestingImpl.createNetworkFromCustomLinks(myPT_StopTrafficCounter.CustomLinkMap, network, fileName);
		}

		return myPT_StopTrafficCounter.CustomLinkMap;
	}

	public static Map<Id<Link>, CustomLinkAttributes> setMainFacilities(TransitSchedule transitSchedule,
			Network network, Map<Id<Link>, CustomLinkAttributes> selectedLinksIn, String fileName) {

		Map<Id<Link>, CustomLinkAttributes> selectedLinks = copyCustomMap(selectedLinksIn);

		// Go through all facilities and whenever a stop facility refers to a selected
		// link, associate that stop facility with that link
		// Check its transport mode and - if a mode exists already - associate only if
		// this transport mode has the bigger facility than the one before (rail > bus)
		// How to check transport mode of a facility:
		// - Go through all lines --> all routes --> all stops
		// - Check if stops contain the link of question (each selectedLink)
		// - if yes, assess that transportMode e.g. "rail" in transitRoute.transitMode
		// and return the mode for the selected link!

		LinkLoop: for (Id<Link> selectedLinkID : selectedLinks.keySet()) {
			for (TransitLine transitLine : transitSchedule.getTransitLines().values()) {
				for (TransitRoute transitRoute : transitLine.getRoutes().values()) {
					for (TransitRouteStop transitRouteStop : transitRoute.getStops()) {
						if (transitRouteStop.getStopFacility().getLinkId() == selectedLinkID) {
							String mode = transitRoute.getTransportMode();
							// System.out.println("Mode on detected transit stop facility is |"+mode+"|");
							if (mode == "rail") {
								CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
								updatedAttributes.setDominantMode(mode);
								updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
								selectedLinks.put(selectedLinkID, updatedAttributes);
								// System.out.println("Added mode: "+mode);
								continue LinkLoop; // if mode is rail, we set the default to rail bc it is most dominant
													// and move to next link (--> this link is completed)
							} else if (mode == "tram") {
								CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
								updatedAttributes.setDominantMode(mode);
								updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
								selectedLinks.put(selectedLinkID, updatedAttributes);
								// System.out.println("Added mode: "+mode);

							} else if (mode == "bus") {
								if (selectedLinks.get(selectedLinkID).getDominantMode() == null
										|| selectedLinks.get(selectedLinkID).getDominantMode() == "funicular") {
									CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
									updatedAttributes.setDominantMode(mode);
									updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
									selectedLinks.put(selectedLinkID, updatedAttributes);
									// System.out.println("Added mode: "+mode);
								}
							} else if (mode == "funicular") {
								if (selectedLinks.get(selectedLinkID).getDominantMode() == null) {
									CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
									updatedAttributes.setDominantMode(mode);
									updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
									selectedLinks.put(selectedLinkID, updatedAttributes);
									// System.out.println("Added mode: "+mode);
								}
							} else {
								System.out.println("Did not recognize mode: " + mode + ", but adding it anyways...");
								CustomLinkAttributes updatedAttributes = selectedLinks.get(selectedLinkID);
								updatedAttributes.setDominantMode(mode);
								updatedAttributes.setDominantStopFacility(transitRouteStop.getStopFacility());
								selectedLinks.put(selectedLinkID, updatedAttributes);
							}

						}
					}
				}
			}
		}

		if (fileName != null) {
			OutputTestingImpl.createNetworkFromCustomLinks(selectedLinks, network, fileName);
		}
		return selectedLinks;
	}

	public static Map<Id<Link>, CustomLinkAttributes> findLinksWithinBounds(
			Map<Id<Link>, CustomLinkAttributes> customLinkMap, Network network, Coord networkCenterCoord,
			double minRadiusFromCenter, double maxRadiusFromCenter, String fileName) {

		Map<Id<Link>, CustomLinkAttributes> feasibleLinks = copyCustomMap(customLinkMap);
		double distanceFromCenter = 0.0;
		Iterator<Id<Link>> linkIterator = feasibleLinks.keySet().iterator();
		while (linkIterator.hasNext()) {
			Id<Link> thisLinkID = linkIterator.next();
			// calculate distance with FromNode;
			distanceFromCenter = GeomDistance.calculate(network.getLinks().get(thisLinkID).getFromNode().getCoord(),
					networkCenterCoord);
			if (distanceFromCenter < minRadiusFromCenter || distanceFromCenter > maxRadiusFromCenter) {
				linkIterator.remove();
			}
		}
		System.out.println("Size is: " + feasibleLinks.size());
		if (fileName != null) {
			OutputTestingImpl.createNetworkFromCustomLinks(feasibleLinks, network, fileName);
		}

		return feasibleLinks;
	}

	public static Network createMetroNetworkFromCandidates(Map<Id<Link>, CustomLinkAttributes> customLinkMap,
			double maxNewMetroLinkDistance, Network mergerNetwork, String fileName) {
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		Network newNetwork = scenario.getNetwork();
		NetworkFactory networkFactory = newNetwork.getFactory();

		// Initiate all nodes of facility location (in customLinkMap) with their names
		// as from mergerNetwork
		Node newNode;
		Link newLink;
		Map<Id<Node>, Id<Link>> metroNodeLinkReferences = new HashMap<Id<Node>, Id<Link>>(customLinkMap.size());
		for (Id<Link> linkID : customLinkMap.keySet()) {
			newNode = networkFactory.createNode(Id.createNodeId("MetroNodeLinkRef_" + linkID.toString()),
					customLinkMap.get(linkID).dominantStopFacility.getCoord());
			metroNodeLinkReferences.put(newNode.getId(), linkID);
			System.out.println("New node is called: " + newNode.getId().toString());
			newNetwork.addNode(newNode);
		}

		// Create links in network --> for every node:
		for (Node thisNode : newNetwork.getNodes().values()) {
			for (Node otherNode : newNetwork.getNodes().values()) {
				if (thisNode == otherNode) {
					continue;
				}
				// add NEW links (with appropriate naming method) to all nodes within a specific
				// radius
				else if (GeomDistance.betweenNodes(thisNode, otherNode) < maxNewMetroLinkDistance) {
					newLink = networkFactory.createLink(
							Id.createLinkId(thisNode.getId().toString() + "_" + otherNode.getId().toString()), thisNode,
							otherNode);
					newNetwork.addLink(newLink);
				}
				// add NEW links if refLink of other facility node was on a next link to the
				// link of this facility (an outLink of toNode of this node's refLink)
				else if (mergerNetwork.getLinks().get(metroNodeLinkReferences.get(thisNode.getId())).getToNode()
						.getOutLinks().containsKey(metroNodeLinkReferences.get(otherNode.getId()))) {
					newLink = networkFactory.createLink(
							Id.createLinkId(thisNode.getId().toString() + "_" + otherNode.getId().toString()), thisNode,
							otherNode);
					newNetwork.addLink(newLink);
				}
			}

		}

		if (fileName != null) {
			NetworkWriter networkWriter = new NetworkWriter(newNetwork);
			networkWriter.write(fileName);
		}

		return newNetwork;
	}

	public static Map<Id<Link>, CustomLinkAttributes> copyCustomMap(Map<Id<Link>, CustomLinkAttributes> customMap) {
		Map<Id<Link>, CustomLinkAttributes> customMapCopy = new HashMap<Id<Link>, CustomLinkAttributes>();
		for (Entry<Id<Link>, CustomLinkAttributes> entry : customMap.entrySet()) {
			customMapCopy.put(entry.getKey(), entry.getValue());
		}
		return customMapCopy;
	}

	// REMEMBER: New nodes are named "MetroNodeLinkRef_"+linkID.toString()
	public static ArrayList<NetworkRoute> createInitialRoutes(Network newMetroNetwork,
			Map<Id<Link>, CustomLinkAttributes> links_MetroTerminalCandidates, int nRoutes, double minTerminalDistance,
			String fileName) {

		ArrayList<NetworkRoute> networkRouteArray = new ArrayList<NetworkRoute>();

		// make nRoutes new routes
		Id<Node> terminalNode1 = null;
		Id<Node> terminalNode2 = null;
		OuterNetworkRouteLoop:
		for (int routeNr = 0; routeNr < nRoutes; routeNr++) {

			// choose two random terminals
			Id<Link> randomTerminalLinkId1 = getRandomLink(links_MetroTerminalCandidates.keySet());
			terminalNode1 = Id.createNodeId("MetroNodeLinkRef_" + randomTerminalLinkId1.toString());
			if (newMetroNetwork.getNodes().keySet().contains(terminalNode1) == false) {
				System.out.println("Terminal node 1 is not featured in new network: ");
			}
			int safetyCounter = 0;
			int iterLimit = 10000;
			do {
				Id<Link> randomTerminalLinkId2 = getRandomLink(links_MetroTerminalCandidates.keySet());
				terminalNode2 = Id.createNodeId("MetroNodeLinkRef_" + randomTerminalLinkId2.toString());
				safetyCounter++;
				if (safetyCounter == iterLimit) {
					System.out.println("Oops no second terminal node found after " + iterLimit
							+ " iterations. Please lower minTerminalDistance!");
					continue OuterNetworkRouteLoop;
				}
			} while (GeomDistance.calculate(newMetroNetwork.getNodes().get(terminalNode1).getCoord(),
					newMetroNetwork.getNodes().get(terminalNode2).getCoord()) < minTerminalDistance
					&& safetyCounter < iterLimit);

			if (newMetroNetwork.getNodes().keySet().contains(terminalNode2) == false) {
				System.out.println("Terminal node 2 is not featured in new network: ");
			}

			// Find Djikstra --> nodeList
			ArrayList<Node> nodeList = DijkstraOwn_I.findShortestPathVirtualNetwork(newMetroNetwork, terminalNode1,
					terminalNode2);
				//System.out.println("Node list of network route is: " + nodeList.toString());
			List<Id<Link>> linkList = nodeListToNetworkLinkList(newMetroNetwork, nodeList);
				//System.out.println("Link list of network route is: " + linkList.toString());
			NetworkRoute networkRoute = RouteUtils.createNetworkRoute(linkList, newMetroNetwork);
			System.out.println("The new networkRoute is: " + networkRoute.toString());
			networkRouteArray.add(networkRoute);
		}

		// Store all new networkRoutes in a separate network file for visualization
		Network routesNetwork = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
		NetworkFactory networkFactory = routesNetwork.getFactory();
		for (NetworkRoute nR : networkRouteArray) {
			List<Id<Link>> routeLinkList = new ArrayList<Id<Link>>();
			routeLinkList.add(nR.getStartLinkId());
			routeLinkList.addAll(nR.getLinkIds());
			routeLinkList.add(nR.getEndLinkId());
			for (Id<Link> linkID : routeLinkList) {
				Node tempToNode = networkFactory.createNode(newMetroNetwork.getLinks().get(linkID).getToNode().getId(),
						newMetroNetwork.getLinks().get(linkID).getToNode().getCoord());
				Node tempFromNode = networkFactory.createNode(
						newMetroNetwork.getLinks().get(linkID).getFromNode().getId(),
						newMetroNetwork.getLinks().get(linkID).getFromNode().getCoord());
				Link tempLink = networkFactory.createLink(newMetroNetwork.getLinks().get(linkID).getId(), tempFromNode,
						tempToNode);
				if (routesNetwork.getNodes().containsKey(tempToNode.getId()) == false) {
					routesNetwork.addNode(tempToNode);
				}
				if (routesNetwork.getNodes().containsKey(tempFromNode.getId()) == false) {
					routesNetwork.addNode(tempFromNode);
				}
				if (routesNetwork.getLinks().containsKey(tempLink.getId()) == false) {
					routesNetwork.addLink(tempLink);
				}
			}
		}

		NetworkWriter initialRoutesNetworkWriter = new NetworkWriter(routesNetwork);
		String initialNetworkRoutes = fileName;
		initialRoutesNetworkWriter.write(initialNetworkRoutes);

		return networkRouteArray;
	}

	public static Id<Link> getRandomLink(Set<Id<Link>> linkSet) {
		Random rand = new Random();
		int rInt = rand.nextInt(linkSet.size());
		int linkCount = 0;
		for (Id<Link> linkID : linkSet) {
			if (linkCount == rInt) {
				// System.out.println("Returning random Id<Link> "+linkID);
				return linkID;
			}
			linkCount++;
		}
		System.out.println("Something strange happened. Returning /null/ ...");
		return null;
	}

	public static Id<Node> getRandomNode(Set<Id<Node>> nodeSet) {
		Random rand = new Random();
		int nInt = rand.nextInt(nodeSet.size());
		int nodeCount = 0;
		for (Id<Node> nodeID : nodeSet) {
			if (nodeCount == nInt) {
				System.out.println("Returning Id<Link> " + nodeID);
				return nodeID;
			}
			nodeCount++;
		}
		System.out.println("Something strange happended. Returning /null/ ...");
		return null;
	}

	public static List<Id<Link>> nodeListToNetworkLinkList(Network network, ArrayList<Node> nodeList) {
		List<Id<Link>> linkList = new ArrayList<Id<Link>>(nodeList.size() - 1);
		for (int n = 0; n < (nodeList.size() - 1); n++) {
			for (Link l : nodeList.get(n).getOutLinks().values()) {
				if (l.getToNode() == nodeList.get(n + 1)) {
					linkList.add(l.getId());
				}
			}
		}
		return linkList;
	}

}
