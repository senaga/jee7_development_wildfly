package br.com.senaga.client;
import java.util.Properties;
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

	public static void main(String[] args) throws Exception {
		Logger.getLogger("org.jboss").setLevel(Level.SEVERE);
		Logger.getLogger("org.xnio").setLevel(Level.SEVERE);
		new TicketAgencyClient().run();
	}

	private final Context context;
	private TheatreInfoRemote theatreInfo;
	private TheatreBookerRemote theatreBooker;

	public TicketAgencyClient() throws NamingException {
		final Properties jndiProperties = new Properties();
		jndiProperties.setProperty(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
		this.context = new InitialContext(jndiProperties);
	}

	private enum Command {
		BOOK, LIST, MONEY, QUIT, RELOAD, ASYNC_FORGET, INVALID;
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
			case ASYNC_FORGET:
				handleAsyncFireForget();
				break;
			case BOOK:
				handleBook();
				break;
			case LIST:
				handleList();
				break;
			case MONEY:
				handleMoney();
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
		System.out.println("Commands: book, list, money, reload, async_forget, quit");
	}
}
