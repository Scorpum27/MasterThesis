package ch.ethz.matsim.students.samark;

import org.matsim.api.core.v01.events.GenericEvent;
import org.matsim.api.core.v01.events.handler.GenericEventHandler;

import ch.ethz.matsim.baseline_scenario.transit.events.PublicTransitEvent;

public class ExampleHandler implements GenericEventHandler {
	public void handleEvent(GenericEvent event) {
		if (event instanceof PublicTransitEvent) {
			PublicTransitEvent ptEvent = (PublicTransitEvent) event;

			ptEvent.getTransitLineId();
		}
	}

	/**
	 * Zum Auslesen aus Events XML:
	 * 
	 * EventsManager eventsManager = EventsUtils.createEventsManager();
	 * eventsManager.addHandler(tripListener);
	 * 
	 * EventsReaderXMLv1 reader = new EventsReaderXMLv1(eventsManager);
	 * reader.addCustomEventMapper(PublicTransitEvent.TYPE, new
	 * PublicTransitEventMapper()); reader.readFile(eventsPath);
	 */
}
