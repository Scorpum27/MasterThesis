package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;

public class NodeListToNetworkRoute{
	
	public static NetworkRoute convert(Network network, ArrayList<Node> nodeList) {
		// ArrayList<Link> linkListArray = new ArrayList<Link>(nodeList.size());
		List<Id<Link>> linkList = new ArrayList<Id<Link>>(nodeList.size()-1);
		for (int n=0; n<(nodeList.size()-1); n++) {
			// System.out.println("n= "+n);
			// System.out.println("nodeListSize= "+nodeList.size());
			for (Link l : nodeList.get(n).getOutLinks().values()) {
				if (l.getToNode() == nodeList.get(n+1)) {
					linkList.add(l.getId());
					System.out.println("Adding link "+l.getId().toString());
				}
			}
		}
		
		NetworkRoute nr = RouteUtils.createNetworkRoute(linkList, network);
		// NetworkRoute nr = null;
		return nr;
	}
	
}
