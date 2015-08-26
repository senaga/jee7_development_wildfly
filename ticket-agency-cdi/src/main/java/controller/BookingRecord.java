package controller;

import javax.enterprise.event.Observes;
import javax.faces.bean.ViewScoped;
import javax.inject.Named;

import entities.Seat;

@Named
@ViewScoped
public class BookingRecord {
	
	private int bookedCount = 0;

	public int getBookedCount() {
		return bookedCount;
	}

	public void bookEvent(@Observes Seat bookedSeat) {
		bookedCount++;
	}
}
