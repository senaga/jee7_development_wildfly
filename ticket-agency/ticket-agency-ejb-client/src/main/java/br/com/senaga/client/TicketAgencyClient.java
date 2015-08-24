package br.com.senaga.client;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import boundary.TheatreBookerRemote;
import boundary.TheatreInfoRemote;
import exception.NoSuchSeatException;
import exception.NotEnoughMoneyException;
import exception.SeatBookedException;

public class TicketAgencyClient {

	private static final Logger logger = Logger.getLogger(TicketAgencyClient.class.getName());
	
	private final Context context;
	private TheatreInfoRemote theatreInfo;
	private TheatreBookerRemote theatreBooker;
	private	final List<Future<String>> lastBookings = new ArrayList<>();		
	
	public static void main(String[] args) throws Exception {
		Logger.getLogger("org.jboss").setLevel(Level.SEVERE);
		Logger.getLogger("org.xnio").setLevel(Level.SEVERE);
		new TicketAgencyClient().run();
	}

	public TicketAgencyClient() throws NamingException {
		final Properties jndiProperties = new Properties();
		jndiProperties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		this.context = new InitialContext(jndiProperties);
	}

	private enum Command {
		BOOK, BOOK_ASYNC, MAIL, LIST, MONEY, QUIT, RELOAD, ASYNC_FORGET, INVALID;
		public static Command parseCommand(String stringCommand) {
			try {
				return valueOf(stringCommand.trim().toUpperCase());
			} catch (IllegalArgumentException iae) {
				return INVALID;
			}
		}
	}

	private void run() throws NamingException {
		this.theatreInfo = lookupTheatreInfoEJB();
		this.theatreBooker = lookupTheatreBookerEJB();
		while (true) {
			showWelcomeMessage();
			final String stringCommand = IOUtils.readLine("> ");
			final Command command = Command.parseCommand(stringCommand);
			switch (command) {
			case BOOK:
				handleBook();
				break;
			case BOOK_ASYNC:
				handleBookAsync();
				break;				
			case LIST:
				handleList();
				break;
			case MONEY:
				handleMoney();
				break;
			case MAIL:
				handleMail();
				break;				
			case ASYNC_FORGET:
				handleAsyncFireForget();
				break;				
			case QUIT:
				handleQuit();
				break;
			case RELOAD:
				reload();
				break;				
			default:
				logger.warning("Unknown	command	" + stringCommand);
			}
		}
	}

	private void handleBook() {
		int seatId;
		try {
			seatId = IOUtils.readInt("Enter SeatId: ");
		} catch (NumberFormatException e1) {
			logger.warning("Wrong SeatId format!");
			return;
		}
		try {
			final String retVal = theatreBooker.bookSeat(seatId);
			System.out.println(retVal);
		} catch (SeatBookedException | NotEnoughMoneyException | NoSuchSeatException e) {
			logger.warning(e.getMessage());
			return;
		}
	}

	private void handleList() {
		logger.info(theatreInfo.printSeatList());
	}
	
	private void reload() {
		theatreInfo.reconfigCache();
		logger.info("Reload");		
	}
	
	private	void handleBookAsync() {
		String text = IOUtils.readLine("Enter SeatId: ");
		int	seatId;
		try	{
			seatId = Integer.parseInt(text);
		} catch	(NumberFormatException	e1)	{
			logger.warning("Wrong seatId format!");
			return;
		}
		lastBookings.add(theatreBooker.bookSeatAsync(seatId));
		logger.info("Booking issued. Verify your mail!");
	}
	
	private void handleMail() {
		boolean displayed = false;
		final List<Future<String>> notFinished = new ArrayList<>();
		for (Future<String> booking: lastBookings) {
			if (booking.isDone()) {
				try {
					final String result = booking.get();
					logger.info("Mail received: " + result);
					displayed = true;
				} catch (InterruptedException | ExecutionException e) {
					logger.warning(e.getMessage());
				}
			} else {
				notFinished.add(booking);
			}
		}
		lastBookings.retainAll(notFinished);
		if (!displayed) {
			logger.info("No mail received!");
		}
	}
	
	private void handleAsyncFireForget() {
		try {
			int seatId = IOUtils.readInt("Enter SeatId: ");
			theatreBooker.bookSeatAsyncFireAndForget(seatId);
			logger.info("Async Fire-And-Forget");			
		} catch (NumberFormatException e1) {
			logger.warning("Wrong SeatId format!");
		} catch (SeatBookedException | NotEnoughMoneyException | NoSuchSeatException e) {
			logger.warning(e.getMessage());
		}
	}

	private void handleMoney() {
		final int accountBalance = theatreBooker.getAccountBalance();
		logger.info("You have: " + accountBalance + " money left.");
	}

	private void handleQuit() {
		logger.info("Bye");
		System.exit(0);
	}

	private	TheatreInfoRemote lookupTheatreInfoEJB() throws NamingException {
		return (TheatreInfoRemote)context.lookup("ejb:/ticket-agency-ejb//TheatreInfo!boundary.TheatreInfoRemote");
	}

	private	TheatreBookerRemote	lookupTheatreBookerEJB() throws NamingException	{
		return (TheatreBookerRemote)context.lookup("ejb:/ticket-agency-ejb//TheatreBooker!boundary.TheatreBookerRemote?stateful");
	}

	private void showWelcomeMessage() {
		System.out.println("Theatre	booking	system");
		System.out.println("=====================================");
		System.out.println("Commands: book, book_async, list, money, reload, async_forget, mail, quit");
	}
}
