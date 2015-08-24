package boundary;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateful;

import exception.NoSuchSeatException;
import exception.NotEnoughMoneyException;
import exception.SeatBookedException;
import singleton.TheatreBox;

@Stateful
@Remote(TheatreBookerRemote.class)
@AccessTimeout(value = 5, unit = TimeUnit.MINUTES)
public class TheatreBooker implements TheatreBookerRemote {	
	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 2L;	
	
	private	static final Logger logger = Logger.getLogger(TheatreBooker.class.getName());	
	
	@EJB
	private	TheatreBox theatreBox;
	private	int	money;

	@PostConstruct
	public void createCustomer() {
		this.money = 100;
	}
	
	@Override
	public int getAccountBalance()	{
		return money;
	}	
	
	@Override
	public String bookSeat(int seatId) throws SeatBookedException, NotEnoughMoneyException, NoSuchSeatException {
		final int seatPrice	= theatreBox.getSeatPrice(seatId);
		if	(seatPrice > money)	{
			throw new NotEnoughMoneyException("You don’t have enough money to buy this " + seatId + " seat!");
		}
		theatreBox.buyTicket(seatId);
		money = money - seatPrice;
		logger.info("Seat " + seatId +" booked.");
		return "Seat booked.";
	}
	
	@Asynchronous
	@Override
	public void bookSeatAsyncFireAndForget(int seatId) throws NotEnoughMoneyException, NoSuchSeatException, SeatBookedException {
		logger.info("call bookSeatAsyncFireAndForget.");
		bookSeat(seatId);
	}
	
	@Asynchronous
	@Override	
	public Future<String> bookSeatAsync(int	seatId) {
		try	{
			Thread.sleep(10000);
			bookSeat(seatId);
			return new AsyncResult<>("Booked seat: " + seatId + ". Money left: " + money);
		} catch (NoSuchSeatException | SeatBookedException | NotEnoughMoneyException | InterruptedException e) {
			return new AsyncResult<>(e.getMessage());
		}
	}
}
