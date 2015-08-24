package boundary;

import exception.NoSuchSeatException;
import exception.NotEnoughMoneyException;
import exception.SeatBookedException;

public interface TheatreBookerRemote {
	public String bookSeat(int seatId) throws SeatBookedException, NotEnoughMoneyException, NoSuchSeatException;
	public void bookSeatAsyncFireAndForget(int seatId) throws NotEnoughMoneyException, NoSuchSeatException, SeatBookedException;
	public int getAccountBalance();	
}
