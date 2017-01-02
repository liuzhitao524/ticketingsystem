package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicMarkableReference;


public class TicketingDS  implements TicketingSystem
{

	private int routenum,coachnum,seatnum,stationnum;

	
	private AtomicLong lastId= new AtomicLong(0);

	public  int getRouteNum()
	{
		return routenum;
	}
	public int getCoachNum()
	{
		return coachnum;
	}
	public int getSeatNum()
	{
		return seatnum;
	}
	public int getStaionNum()
	{
		return  stationnum;
	}
	
	

	private TicketingRoute[] routes;
	
	public TicketingDS()
	{
		this(5,8,100,10);
	}
	public TicketingRoute[] getRoutes()
	{
		return this.routes;
	}
	public TicketingDS(int routenum,int coachnum,int seatnum,int stationnum)
	{
		this.routenum=routenum;
		this.coachnum=coachnum;
		this.seatnum=seatnum;
		this.stationnum=stationnum;
		
		this.init();
	}
	private  void  init()
	{
		 /*routes = new TicketingRoute[routenum];
		 routes[0]=new TicketingRoute();
		 routes[0].tickets=new TicketingPlus[seatnum];
		 routes[0].tickets[0]=new TicketingPlus();
		 routes[0].tickets[0].flag=true;
		 routes[0].tickets[0].ticket=new Ticket();
		 routes[0].tickets[0].ticket.seat=10000;*/
		TicketingRoute p;
		
		routes=new TicketingRoute[routenum];
		
		for(int i=0;i<routenum;i++)
		{
			TicketingRoute routehead=new TicketingRoute(); //with head
			routehead.next=null; // head node
			routehead.routenum=0;
			routehead.coachnum=0;
			routehead.stationnum=0;
			
			routes[i]=routehead;
			p=routehead;
			for(int j=0;j<coachnum;j++)
			{
				TicketingRoute tr=new TicketingRoute();
				tr.routenum=i+1;
				tr.coachnum=j+1;
				tr.stationnum=this.stationnum;
				tr.next=null;
				tr.tickets=new TicketingPlus[seatnum];
				for(int k=0;k<seatnum;k++)
				{
					TicketingPlus tickethead=new TicketingPlus();
					
					
					tr.tickets[k]=tickethead; //with head and head contain the number of seat,flag information
					
					
					tickethead.next=new AtomicMarkableReference<TicketingPlus>(null,false);
					tickethead.ticket=new Ticket();
					
					tickethead.ticket.tid=0;         //0 meaning no passenager
					tickethead.ticket.passenger="";
					tickethead.ticket.route=0;
					tickethead.ticket.coach=0;
					tickethead.ticket.seat=k+1;
					tickethead.ticket.departure=0;
					tickethead.ticket.arrival=0;
					
					
					
				}
				p.next=tr;
				p=tr;
				
				
				
			}
			
		}
		
		
		
		
	}
	private TicketingWindow find(TicketingPlus tp,Ticket tc)
	{
		TicketingPlus pred=null,curr=null,succ=null;
		boolean[] marked={false};
		boolean snip;
		retry:while(true)
		{
			pred=tp;
			curr=pred.next.getReference();
			while(true)
			{
				succ=curr.next.get(marked);
				/* attempt to remove empty seat */
				while(marked[0])
				{
					snip=pred.next.compareAndSet(curr,succ,false,false);
					if(!snip) continue retry;
				
						curr=succ;
						succ=curr.next.get(marked);
						
				}
				if((curr.ticket.tid==tc.tid && curr.ticket.passenger==tc.passenger && curr.ticket.departure==tc.departure && curr.ticket.arrival==tc.arrival )|| succ==null)
					return new TicketingWindow(pred,curr);
				pred=curr;
				curr=succ;
			}
			
			
		}
		
		
	}
	private boolean findseat(TicketingPlus tp,Ticket tc)
	{
		TicketingPlus temp=tp.next.getReference();
		
		for(;temp!=null;temp=temp.next.getReference())
		{
			if(temp.next.isMarked()) continue;//if there is more thing to do
			else
			{
				if((tc.departure<temp.ticket.departure && tc.arrival<=temp.ticket.departure) || (tc.departure>=temp.ticket.arrival &&tc.arrival>temp.ticket.arrival))
					continue;
				else
					return  false; 
				
					
				
			}
		}
		return true;
	}
	
	public Ticket buyTicket(String passenger, int route, int departure,
			int arrival) {
		/*先初始化想买的票信息*/
		TicketingPlus tp= new TicketingPlus();
		Ticket newticket = new Ticket();
		tp.next=null;
	
		newticket.passenger=passenger;
		if(route<1 || route>this.routenum)
		{
			return null;
		}
		newticket.route=route;
		if(departure>=arrival || departure>=this.stationnum || arrival>this.stationnum) //规定只能买单一方向的票，这里departure< arrival ,random false reason
		{
			return null;
		}
		newticket.departure=departure;
		newticket.arrival=arrival;
		
		TicketingRoute trhead=routes[--route];
		TicketingRoute  temptr=trhead.next;
		TicketingPlus[] tps;
		TicketingPlus tphead;
		
		
		while(true)
		{
		
			//temptr = trhead.next;
			
			for(;temptr!=null;temptr=temptr.next)
			{
				 tps=temptr.tickets;
				
				for(int j=0;j<this.seatnum;j++)
				{
					tphead=tps[j];
					
					if(	!findseat(tphead,newticket)) //find whether there is empty ticket  base of departure and arrival
					{
						continue;
					}
					else
					{
						newticket.coach=temptr.coachnum;
						newticket.seat=tphead.ticket.seat;
						if((lastId.getAndIncrement())<0)
						{	
							lastId.set(0);//cycle restart
							
							newticket.tid=1;
						}
						else
						{
							newticket.tid=lastId.longValue(); //the information of ticket is complete
						}
						tp.ticket=newticket;
						
						TicketingPlus tphead_next=tphead.next.getReference();
						tp.next=new AtomicMarkableReference<TicketingPlus>(tphead_next,false);
						if(tphead.next.compareAndSet(tphead_next,tp,false,false))
						{
							return newticket; 
						}//else loop
						
						
						
					}
				}
			}
			if(temptr ==  null)
			{
			
				System.out.println("sorry,no more tickets");
				return null;
			}
		}
	
		
		
		
			
		
	
	}

	public int inquiry(int route, int departure, int arrival) {
		
		if(route<1 || route>this.routenum || departure >= arrival)
			return 0;
		
		int count=0;
		Ticket testTicket=new Ticket();
		testTicket.departure=departure;
		testTicket.arrival=arrival;
		TicketingRoute tr=this.routes[--route];
		for(tr=tr.next;tr!=null;tr=tr.next)
		{
			for(int i=0;i<this.seatnum;i++)
			{
				TicketingPlus tphead=tr.tickets[i];
				if(findseat(tphead,testTicket))
					++count;
				
			}
			
		}
		return count;
		
		
		
		
		
		
	}

	public boolean refundTicket(Ticket ticket) { 
		
		if(ticket.route<1 || ticket.route> this.routenum)
			return false;
		if(ticket.coach<1 || ticket.coach>this.coachnum)
			return false;
		if(ticket.seat<1 || ticket.seat> this.seatnum)
			return false;
		TicketingRoute tr=this.routes[ticket.route-1];
		
		for(tr=tr.next;tr!=null;tr=tr.next)
		{
			if(tr.coachnum==ticket.coach)
				break;
			
		}
		TicketingPlus tphead=tr.tickets[ticket.seat-1];
		boolean snip = false;
		
		while(true)
		{
			TicketingWindow window=find(tphead,ticket);
			TicketingPlus pred=window.pred;
			TicketingPlus curr=window.curr;
			if(curr.ticket.tid!=ticket.tid)
				return false;
			else{
				TicketingPlus succ=curr.next.getReference();
				//snip=curr.next.compareAndSet(succ,succ,false,true);
				snip=curr.next.attemptMark(succ, true); //logical remove
				if(!snip)
					continue;
				pred.next.compareAndSet(curr,succ,false,false); //don't care ,somebody do the physical  remove
				return true;
				
			}
		}
		
		
		
		
			
		
	}



}