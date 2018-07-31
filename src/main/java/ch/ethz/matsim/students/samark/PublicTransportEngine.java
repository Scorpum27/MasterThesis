package ch.ethz.matsim.students.samark;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

public class PublicTransportEngine {
	
	public static List<TransitRouteStop> networkRouteStopsAllLinks(TransitSchedule transitSchedule, Network network, NetworkRoute networkRoute, String defaultPtMode, double stopTime, double vehicleSpeed, boolean blocksLane){
		TransitScheduleFactory transitScheduleFactory = transitSchedule.getFactory();
		List<TransitRouteStop> stopArray = new ArrayList<TransitRouteStop>();				// prepare an array for stop facilities on new networkRoute
		
		int stopCount = 0;
		double accumulatedDrivingTime = 0;
		Link lastLink = null;
		
		for (Id<Link> linkID : networkRoute.getLinkIds()) {
			Link currentLink = network.getLinks().get(linkID);
			TransitStopFacility transitStopFacility = transitScheduleFactory.createTransitStopFacility(Id.create("linkStop_"+linkID.toString(), TransitStopFacility.class), GeomDistance.coordBetweenNodes(currentLink.getFromNode(), currentLink.getToNode()), blocksLane);
			transitStopFacility.setName("CenterLinkStop_"+linkID.toString());
			transitStopFacility.setLinkId(linkID);
			stopCount++;
			if(stopCount>1) {
				accumulatedDrivingTime += (lastLink.getLength()/2+currentLink.getLength()/2)/vehicleSpeed;
			}
			double arrivalDelay = (stopCount-1)*stopTime + accumulatedDrivingTime;
			double departureDelay = (stopCount)*stopTime + accumulatedDrivingTime;		// same as arrivalDelay + 1*stopTime
			TransitRouteStop transitRouteStop = transitScheduleFactory.createTransitRouteStop(transitStopFacility, arrivalDelay, departureDelay);
			stopArray.add(transitRouteStop);
			lastLink = currentLink;
		}
		
		return stopArray;
	}

	public static TransitRoute addDeparturesToTransitRoute(TransitSchedule transitSchedule, TransitRoute transitRoute, int nDepartures, double firstDepTime, double departureSpacing) {
		double depTimeOffset = 0;
		for (int d=0; d<nDepartures; d++) {
			depTimeOffset = d*15*60;
			Departure departure = transitSchedule.getFactory().createDeparture(Id.create(transitRoute.getId().toString()+"_Departure_"+d+"_"+(firstDepTime+depTimeOffset), Departure.class), firstDepTime+depTimeOffset); // TODO specify departureX with better name
			transitRoute.addDeparture(departure);
		}
		return transitRoute;
	}
	

}
