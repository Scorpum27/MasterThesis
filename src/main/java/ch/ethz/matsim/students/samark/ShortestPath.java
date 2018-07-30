package ch.ethz.matsim.students.samark;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;

public class ShortestPath {

	public static Network createNetwork(Config config, Network network, NetworkRoute networkRoute) {
		Network shortestPathNetwork = ScenarioUtils.createScenario(config).getNetwork();
		NetworkFactory shortestPathNetworkFactory = shortestPathNetwork.getFactory();
		// Link tempLink = null;
		// Node tempToNode = null;
		// Node tempFromNode = null;
		for (Id<Link> linkID : networkRoute.getLinkIds()) {
			Node tempToNode = shortestPathNetworkFactory.createNode(network.getLinks().get(linkID).getToNode().getId(), network.getLinks().get(linkID).getToNode().getCoord());
			Node tempFromNode = shortestPathNetworkFactory.createNode(network.getLinks().get(linkID).getFromNode().getId(), network.getLinks().get(linkID).getFromNode().getCoord());
			Link tempLink = shortestPathNetworkFactory.createLink(network.getLinks().get(linkID).getId(), tempFromNode, tempToNode);
			if (shortestPathNetwork.getNodes().containsKey(tempToNode.getId())==false) {
				shortestPathNetwork.addNode(tempToNode);
			}
			if (shortestPathNetwork.getNodes().containsKey(tempFromNode.getId())==false) {
				shortestPathNetwork.addNode(tempFromNode);
			}
			if (shortestPathNetwork.getLinks().containsKey(tempLink.getId())==false) {
				shortestPathNetwork.addLink(tempLink);
			}
		}
		return shortestPathNetwork;
	}
	
}
