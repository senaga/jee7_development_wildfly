package boundary;

import exception.NoSuchSeatException;
import exception.NotEnoughMoneyException;
import exception.SeatBookedException;

public interface TheatreBookerRemote {
	public String bookSeat(int seatId) throws SeatBookedException, NotEnoughMoneyException, NoSuchSeatException;
	
	public	int	getAccountBalance();	
}
