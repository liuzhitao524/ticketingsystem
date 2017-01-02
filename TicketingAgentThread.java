package ticketingsystem;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public class TicketingAgentThread implements Runnable
{

	private long ntotalBuyTime;
	private long ntotalRefundTime;
	private long ntotalInquiryTime;
	private TicketingDS tds;
	
	private boolean outputFlag = false;
	//private int totalTestNum  ;
	private AtomicInteger totalNum ; // test number
	private ThreadLocal<Integer> threadLocalInt; //每个线程当前拿到的测试数；








	public long getNtotalBuyTime() {
		return ntotalBuyTime;
	}


	public void setNtotalBuyTime(long ntotalBuyTime) {
		this.ntotalBuyTime = ntotalBuyTime;
	}


	public long getNtotalRefundTime() {
		return ntotalRefundTime;
	}


	public void setNtotalRefundTime(long ntotalRefundTime) {
		this.ntotalRefundTime = ntotalRefundTime;
	}


	public long getNtotalInquiryTime() {
		return ntotalInquiryTime;
	}


	public void setNtotalInquiryTime(long ntotalInquiryTime) {
		this.ntotalInquiryTime = ntotalInquiryTime;
	}


	public void setThreadLocal(ThreadLocal<Integer> threadLocalInt)
	{
		this.threadLocalInt = threadLocalInt;
	}

	
	public void setTotalNum(AtomicInteger totalNum)
	{
		this.totalNum = totalNum;
	}
	public AtomicInteger getTotalNum()
	{
		return totalNum;
	}
	
	
	public TicketingAgentThread(TicketingDS tds)
	{
		this.tds=tds;
	}
	public void setOutputFlag(boolean flag)
	{
		outputFlag = flag;
		
	}
	
	public void run() 
	{
		
		String name = "test";
		Random randomRoute = new Random();
		Random randomDeparture = new Random();
		Random randomArrival = new Random();
		Random randomCoach = new Random();
		int routeNum = tds.getRouteNum();
		int  stationNum = tds.getStaionNum();
		int coachNum = tds.getCoachNum();
		
		
		do{
			if(totalNum.intValue()>0)
			{
				threadLocalInt.set(totalNum.getAndDecrement());
			}
			else
			{
				return ;
			}
			//System.out.println(Thread.currentThread().getId()+"\t"+"num:"+threadLocalInt.get());
			
			if(threadLocalInt.get()%10<6)
			{
				
				long ninquiryStartTime = System.nanoTime();
				//if departure > arrival return 0
			int ticketNumbyInquiry = tds.inquiry(randomRoute.nextInt(routeNum)+1, randomDeparture.nextInt(stationNum)+1, randomArrival.nextInt(stationNum)+1);
		
				ntotalInquiryTime += System.nanoTime()-ninquiryStartTime;
				if(outputFlag) //control whether to print inquiry information
				{
					System.out.println("ticketNumbyInquiry: " + ticketNumbyInquiry);
				}
				
				
			}else if(threadLocalInt.get()%10<9)
			{
				
				
			
				
				long nbuyStartTime = System.nanoTime();
				//if departure  > arrival return null
				Ticket t  = tds.buyTicket(name,randomRoute.nextInt(routeNum)+1,randomDeparture.nextInt(stationNum)+1, randomArrival.nextInt(stationNum)+1);
				ntotalBuyTime += System.nanoTime() - nbuyStartTime;
				if(outputFlag) //whether to print buy ticket information
				{
					if(t != null)
					{
						System.out.println(Thread.currentThread().getId()+"\t Sucess buy ticket of localInt\t"+threadLocalInt.get()+"\t t.tid\t"+t.tid+"\t t.route\t"+t.route+"\t t.coach\t"+t.coach+"\t t.seat\t"+t.seat+"\t t.departure\t"+t.departure+"\t t.arrival\t"+t.arrival);
						
						
					}
				}
			}
			
			
			else  //refund ticket 
			{ 
				
				Ticket ticket = new Ticket();
				ticket.tid = new Random().nextLong();
				ticket.passenger = name;
				ticket.route = randomRoute.nextInt(routeNum)+1;
				ticket.coach = randomCoach.nextInt(coachNum)+1;
				ticket.departure = randomDeparture.nextInt(stationNum)+1;
				ticket.arrival = randomArrival.nextInt(stationNum)+1;
			
				long nrefundStartTime = System.nanoTime();
				
				
			
				
			
				
				boolean flag = tds.refundTicket(ticket);
				ntotalRefundTime += System.nanoTime() - nrefundStartTime;
	
				
				if(outputFlag)
				{
					if(flag == true)
					{
						System.out.println(Thread.currentThread().getId()+"\t Refund ticket sucess\t"+"\t ticket.tid\t"+ticket.tid+"\t ticket.route\t"+ticket.route+"\t ticket.coach\t"+ticket.coach+"\t t.seat\t"+ticket.seat+"\t ticket.departure\t"+ticket.departure+"\t ticket.arrival\t"+ticket.arrival);
					}
				}
				
			}
		
		}while(threadLocalInt.get() > 0);
		
	
	}
	
}