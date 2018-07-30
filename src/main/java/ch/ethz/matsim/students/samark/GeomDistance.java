package ch.ethz.matsim.students.samark;

import org.matsim.api.core.v01.Coord;

public class GeomDistance {

	public static double calculate(Coord coord1, Coord coord2) {
		double distance = Math.sqrt((coord1.getX()-coord2.getX())*(coord1.getX()-coord2.getX())+(coord1.getY()-coord2.getY())*(coord1.getY()-coord2.getY()));
		return distance;
	}
}