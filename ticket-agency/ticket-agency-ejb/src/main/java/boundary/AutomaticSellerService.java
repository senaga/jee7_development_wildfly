package boundary;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TimerService;

import entities.Seat;
import exception.NoSuchSeatException;
import exception.SeatBookedException;
import singleton.TheatreBox;

@Stateless
public class AutomaticSellerService {

	private	static final Logger	logger = Logger.getLogger(AutomaticSellerService.class.getName());
	
	@EJB
	private	TheatreBox theatreBox;
	
	@Resource
	private	TimerService timerService;
	
	@Schedule(hour = "*", minute = "*/1", persistent = false)
	public void automaticCustomer() throws NoSuchSeatException {
		final Optional<Seat> seatOptional = findFreeSeat();
		if (!seatOptional.isPresent()) {
			cancelTimers();
			logger.info("Scheduler gone!");
			return; // No more seats
		}		
		final Seat seat = seatOptional.get();
		try	{
			theatreBox.buyTicket(seat.getId());
		} catch (SeatBookedException e) {
			// do nothing, user booked this seat in the meantime
		}
		logger.info("Somebody just booked seat number " + seat.getId());
	}
	
	private	Optional<Seat> findFreeSeat() {
		final Collection<Seat> list = theatreBox.getSeats();
		return list.stream()
				.filter(seat -> !seat.isBooked())
				.findFirst();
	}

	private	void cancelTimers() {
		for	(javax.ejb.Timer timer: timerService.getTimers()) {
			timer.cancel();
		}
	}
}
