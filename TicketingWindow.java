package ticketingsystem;
public class TicketingWindow{
	public TicketingPlus pred;
	public TicketingPlus curr;
	public TicketingWindow(TicketingPlus mypred,TicketingPlus mycurr)
	{
		this.pred=mypred;
		this.curr=mycurr;
	}
}