package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.Random;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

public class RandomRouteGeneratorShortest {

	public static ArrayList<Node> createRouteBetweenNodes(Network network, Node startNode, Node endNode) {
		// System.out.println("started route generator");
		ArrayList<Node> dijkstraNodePath = DijkstraOwn_I.findShortestPath(network, startNode, endNode);
		if(dijkstraNodePath == null) {
			System.out.println("Error: No path found between start and end node! Trying a new pair of start/end nodes ... ");
			return null;
		}
		// System.out.println("To my surprise it worked ...");
		return dijkstraNodePath;
	}
	
	
	public static ArrayList<Node> createRandomRoute(Network network, int XMax, int YMax, int outerFramePercentage, int minSpacingPercentage) {
		Node startNode = createStartOrEndNode(network, XMax, YMax, outerFramePercentage);
		System.out.println("Start Node: "+startNode.getId().toString());
		Node endNode = startNode; // just for initializing
		do {
			endNode = createStartOrEndNode(network, XMax, YMax, outerFramePercentage);
			// System.out.println("Distance between node is: "+GeomDistance.calculate(startNode.getCoord(), endNode.getCoord()));
		} while(endNode.equals(startNode) || GeomDistance.calculate(startNode.getCoord(), endNode.getCoord())<=Math.sqrt(1.0*XMax*XMax+YMax*YMax)*outerFramePercentage/100);
		System.out.println("End Node: "+endNode.getId().toString());
		return createRouteBetweenNodes(network, startNode, endNode);
	}	
	
	
	public static Node createStartOrEndNode(Network network, int XMax, int YMax, int outerFramePercentage) {
		int xFrameWidth = (int) (XMax*outerFramePercentage/100) + 1 ;
		int yFrameWidth = (int) (YMax*outerFramePercentage/100) + 1 ;
		Id<Node> frameNodeID = null;
		int rXint = 0;
		int rYint = 0;
		do {
			boolean inFrameX = false;
			while(inFrameX == false) {
				Random rX = new Random();
				rXint = rX.nextInt(XMax)+1;
				if (rXint < xFrameWidth || XMax-xFrameWidth < rXint) {
					inFrameX = true;
				}
			}
			boolean inFrameY = false;
			while(inFrameY == false) {
				Random rY = new Random();
				rYint = rY.nextInt(YMax)+1;
				if (rYint < yFrameWidth || YMax-yFrameWidth < rYint) {
					inFrameY = true;
				}
			}
			int frameNodeNr = networkFiller.xyCoordToNr(rXint, rYint, XMax);
			frameNodeID = Id.createNodeId(frameNodeNr);
		} while (network.getNodes().containsKey(frameNodeID)==false);
		Node frameNode = network.getNodes().get(frameNodeID);
		// System.out.println("Chosen node is "+frameNode.getId().toString());
		return frameNode;
	}
	
}
