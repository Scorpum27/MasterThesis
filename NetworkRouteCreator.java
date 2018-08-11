package ch.ethz.matsim.students.samark;

import java.util.ArrayList;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.population.routes.NetworkRoute;

public class NetworkRouteCreator {

	public static NetworkRoute create(Network networkThin, int XMax, int YMax, int outerFramePercentage, int minSpacingPercentage) {
		
		ArrayList<Node> routeNodeList = new ArrayList<Node>();
		do{
			routeNodeList = RandomRouteGeneratorShortest.createRandomRoute(networkThin, XMax, YMax, outerFramePercentage, minSpacingPercentage); 		// makes random starting points in network in outer network regions
		} while(routeNodeList==null);
		
				// iterate through nodes on resulting list
				/* System.out.println("routeNodeListLength is: "+routeNodeList.size());	
				ListIterator<Node> netRouteIter = routeNodeList.listIterator();
				while(netRouteIter.hasNext()) {
					System.out.println("Current node is: "+netRouteIter.next().getId().toString());	
				} */
		
		NetworkRoute networkRoute = NodeListToNetworkRoute.convert(networkThin, routeNodeList);			// convert from node list format to network route by connecting the corresponding links
		return networkRoute;	
	}
	
}
