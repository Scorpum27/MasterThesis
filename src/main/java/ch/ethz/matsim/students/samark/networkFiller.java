package ch.ethz.matsim.students.samark;

import java.util.Random;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;

public interface networkFiller {

public static Network fill(int xmax, int ymax, Network networkIn) {
	Network network = fillNodes(xmax, ymax, networkIn);
	network = fillNeighboringLinks(xmax, ymax, network);
	network = addGeometricLinkLengths(network);
	return network;
}

public static Network fillNodes(int xmax, int ymax, Network networkIn)	{
	NetworkFactory NF = networkIn.getFactory();
	for (int y=1; y<=ymax; y++) {
		for (int x=1; x<=xmax; x++) {
			Coord tempCoord = new Coord(x,y);
			int nodeNr = xyCoordToNr((int)tempCoord.getX(),(int)tempCoord.getY(), xmax);
			Node newNode = NF.createNode(Id.createNodeId(nodeNr), tempCoord);				// naming of nodes .. fill in from left to right and bottom to top
			networkIn.addNode(newNode);														// add all nodes to network
			// System.out.println("Added node "+newNode.getId().toString()+" to network.");
		}
	}
	return networkIn;
}
 
public static Network fillNeighboringLinks(int xmax, int ymax, Network networkIn) {				// connects all nodes of perfect network with links and returns new network with all links
	 																								// only in perfect network
	 for (int y=1; y<=ymax; y++) {
			for (int x=1; x<=xmax; x++) {
				
				Node centerNode = networkIn.getNodes().get(Id.createNodeId(xyCoordToNr(x, y, xmax)));
				
				 int nodeX = (int) centerNode.getCoord().getX();
				 int nodeY = (int) centerNode.getCoord().getY();
			
				 int askIfNodeRight = xyCoordToNr(nodeX+1, nodeY, xmax);
				 // System.out.println("right node ID is: "+Id.createNodeId(askIfNodeRight).toString());
				 // System.out.println("network contains node ID above?: "+networkIn.getNodes().containsKey(Id.createNodeId(askIfNodeRight)));
				 // System.out.println("exist method output: "+ exist(networkIn.getNodes().containsKey(Id.createNodeId(askIfNodeRight))));
				 if (0 < nodeX+1 && nodeX+1 <= xmax && 0 < nodeY && nodeY <= ymax) {
					 Link rightLink = networkIn.getFactory().createLink(Id.createLinkId(centerNode.getId().toString() + "_" + askIfNodeRight), centerNode, networkIn.getNodes().get(Id.createNodeId(askIfNodeRight)));
					 networkIn.addLink(rightLink);
					 // System.out.println("Added right link "+rightLink.getId().toString()+" to network.");
				 }	 
				 int askIfNodeTop = xyCoordToNr(nodeX, nodeY+1, xmax);
				 if (0 < nodeX && nodeX <= xmax && 0 < nodeY+1 && nodeY+1 <= ymax) {
					 Link topLink = networkIn.getFactory().createLink(Id.createLinkId(centerNode.getId().toString() + "_" + askIfNodeTop), centerNode, networkIn.getNodes().get(Id.createNodeId(askIfNodeTop)));
					 networkIn.addLink(topLink);
					 // System.out.println("Added top link "+topLink.getId().toString()+" to network.");
				 }	 
				 int askIfNodeLeft = xyCoordToNr(nodeX-1, nodeY, xmax);
				 if (0 < nodeX-1 && nodeX-1 <= xmax && 0 < nodeY && nodeY <= ymax) {
					 Link leftLink = networkIn.getFactory().createLink(Id.createLinkId(centerNode.getId().toString() + "_" + askIfNodeLeft), centerNode, networkIn.getNodes().get(Id.createNodeId(askIfNodeLeft)));
					 networkIn.addLink(leftLink);
					 // System.out.println("Added left link "+leftLink.getId().toString()+" to network.");
				 }	 
				 int askIfNodeBottom = xyCoordToNr(nodeX, nodeY-1, xmax);
				 if (0 < nodeX && nodeX <= xmax && 0 < nodeY-1 && nodeY-1 <= ymax) {
					 Link bottomLink = networkIn.getFactory().createLink(Id.createLinkId(centerNode.getId().toString() + "_" + askIfNodeBottom), centerNode, networkIn.getNodes().get(Id.createNodeId(askIfNodeBottom)));
					 networkIn.addLink(bottomLink);
					 // System.out.println("Added bottom link "+bottomLink.getId().toString()+" to network.");
				 }
	 
			}
	 }
	 
	 Network networkOut = networkIn;
	 return networkOut;
}

public static Network addGeometricLinkLengths(Network network) {
	for (Id<Link> linkID : network.getLinks().keySet()) {
		double linkLength = GeomDistance.calculate(network.getLinks().get(linkID).getFromNode().getCoord(), network.getLinks().get(linkID).getToNode().getCoord());
		network.getLinks().get(linkID).setLength(linkLength);
		// System.out.println("Length of link "+linkID.toString()+" is "+linkLength+".");
	}
	return network;
}


public static Network thin(Network network, int XMax, int YMax, int removalPercentage, boolean saveAsFile) {
	Network nwThin = network;
	int networkNodesNumber = nwThin.getNodes().size();
	int nodeAmountRemove = (int) (networkNodesNumber*removalPercentage/100);
	System.out.println("Removing a total of "+nodeAmountRemove+" nodes");
	for (int n=1; n<=nodeAmountRemove; n++) {
		
		Id<Node> nodeToRemoveID = null;
		do  {
			Random r = new Random();
			int nodeNrToRemove = r.nextInt(networkNodesNumber)+1;
			nodeToRemoveID = Id.createNodeId(nodeNrToRemove);
		} while (nwThin.getNodes().containsKey(nodeToRemoveID) == false);

		// System.out.println("Chose to thin around node "+nodeToRemoveID.toString());
		for (Id<Link> inLink : nwThin.getNodes().get(nodeToRemoveID).getInLinks().keySet()) {
			nwThin.removeLink(inLink);
			// System.out.println("Removing inLink "+inLink.toString());
		}
		for (Id<Link> outLink : nwThin.getNodes().get(nodeToRemoveID).getOutLinks().keySet()) {
			nwThin.removeLink(outLink);
			// System.out.println("Removing outLink "+outLink.toString());
		}
		nwThin.removeNode(nodeToRemoveID);
		// System.out.println("Removed node "+nodeToRemoveID.toString()+" and its links");

	}
	if (saveAsFile) {
		String filepath = "myInput/Networks/Network_"+XMax+"x"+YMax+"_"+removalPercentage+"PercentLean.xml";
		NetworkWriter nwT = new NetworkWriter(nwThin);
		nwT.write(filepath);
		System.out.println("Saved new (thinned) network as "+filepath);
	}
	
	// Display all (remaining) nodes of current desired network
		/* n = 1;
		 * for (Id<Node> nID : nwThin.getNodes().keySet()) {
		 * System.out.println("Remaining node "+n+" is: "+nID.toString());
		 * n++;}
		 */
	
	return nwThin;
}

public static void writeToFile(int XMax, int YMax, Network network) {
	NetworkWriter nw = new NetworkWriter(network);
	String filepath = "myInput/Networks/Network_"+XMax+"x"+YMax+"_RAW.xml";
	nw.write(filepath);
}


public static boolean exist(boolean containsKey) {
	if (containsKey == true) {
		return true;
	}
	else {
		return false;
	}
}

public static int xyCoordToNr(int x, int y, int xmax) {
	return (int) ((y-1)*xmax + x);
}
	
public static Coord nrToCoord(int nr, int xmax, int ymax) {
	double x = nr % ymax;
	double y = (nr-x) / xmax;
	Coord xyCoord = new Coord(x,y);
	return xyCoord;
}

}


