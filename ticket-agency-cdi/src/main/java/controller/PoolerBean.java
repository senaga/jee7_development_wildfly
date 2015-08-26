package controller;

import java.io.Serializable;
import java.util.Optional;

import javax.enterprise.inject.Model;
import javax.inject.Inject;

import ejb.TheatreBox;
import entities.Seat;

@Model
public class PoolerBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private TheatreBox theatreBox;

	public boolean getPollingActive() {
		System.out.println("Pooling");
		return areFreeSeatsAvailable();
	}

	private boolean areFreeSeatsAvailable() {
		final Optional<Seat> firstSeat = theatreBox.getSeats().stream().filter(seat -> !seat.isBooked()).findFirst();
		return firstSeat.isPresent();
	}
}
