package boundary;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import ejb.TheatreBox;
import entities.Seat;

@Model
public class TheatreInfo {
	
	@Inject
	private TheatreBox box;
	
	private Collection<Seat> seats;	
	
	@PostConstruct
	public void retrieveAllSeatsOrderedByName()	{
		seats = box.getSeats();
	}
	
	@Produces
	@Named
	public Collection<Seat> getSeats() {
		return new ArrayList<>(seats);
	}
	
	public void onMemberListChanged(@Observes(notifyObserver = Reception.IF_EXISTS)final Seat member) {
		retrieveAllSeatsOrderedByName();
	}
}
