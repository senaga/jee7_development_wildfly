package controller;

import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ejb.TheatreBox;
import entities.Seat;

@RequestScoped
@Named(value="poolerBean")
public class Pooler {

	@Inject
	private TheatreBox theatreBox;

	public boolean isPollingActive() {
		return areFreeSeatsAvailable();
	}

	private boolean areFreeSeatsAvailable() {
		final Optional<Seat> firstSeat = theatreBox.getSeats().stream().filter(seat -> !seat.isBooked()).findFirst();
		return firstSeat.isPresent();
	}
}
