package ticketingsystem;

import java.util.concurrent.atomic.AtomicInteger;

public class Test
{
	
	

	public static void main(String[] args) throws InterruptedException 
	{
		 int NUM_OF_THREAD = 0;
		 
		TicketingDS tds = new TicketingDS(5,10,100,10);
		 int testNum = 10000;
		
		TicketingAgentThread tbt=new TicketingAgentThread(tds);
		tbt.setTotalNum(new AtomicInteger(testNum));
		tbt.setThreadLocal(new ThreadLocal<Integer>());
		tbt.setOutputFlag(false); //whether to print information about tickets
		
		
		//Scanner inputNumThread = new Scanner(System.in);
		if(args.length > 2  || args.length < 1 )
		{
			System.out.println("Usage java Test <ThreadNum>");
			System.exit(1);
		}
		
		NUM_OF_THREAD = Integer.parseInt(args[0]);
		System.out.println(NUM_OF_THREAD);
		//inputNumThread.close();
		Thread[] threads = new Thread[NUM_OF_THREAD];
		for(int i = 0 ;i < NUM_OF_THREAD ; i++)
		{
			threads[i] = new Thread(tbt);
		}
		long startTime = System.currentTimeMillis();
		for(int j = 0 ;j < NUM_OF_THREAD ;j++)
		{
			threads[j].start();
		}
		for(int k = 0 ; k < NUM_OF_THREAD; k++)
		{
			threads[k].join();
		}
		long endTime = System.currentTimeMillis();
		
		
		
		
	
		
		
		
	
		
		System.out.println("startTime: "+startTime);
		System.out.println("endTime: " +endTime+"\t total taken millonsTime: "+ (endTime-startTime));
		
		System.out.println("Throughout:"+(float) testNum/(endTime-startTime));
		System.out.println("total nano time of buy Ticket method:  " + tbt.getNtotalBuyTime());
		System.out.println("total nano time of refund ticket method: " + tbt.getNtotalRefundTime());
		System.out.println("total  nano time of inquiry ticket method: "+ tbt.getNtotalInquiryTime());
	}
	
	

		
		

	
}