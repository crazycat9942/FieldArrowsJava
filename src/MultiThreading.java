public class MultiThreading extends Thread
{
    Panel panel;
    int minI;
    int minJ;
    int maxI;
    int maxJ;
    public MultiThreading(Panel initPanel, int initMinI, int initMinJ, int initMaxI, int initMaxJ)
    {
        panel = initPanel;
        minI = initMinI;
        minJ = initMinJ;
        maxI = initMaxI;
        maxJ = initMaxJ;
    }
    public void run()
    {
        try
        {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    System.out.println(e);
                }
            });
            //double rand = Math.random();
            //System.out.println("thread " + rand + " started");
            panel.recursiveDraw(minI, minJ, maxI, maxJ);
            panel.latch.countDown();
            //System.out.println("thread " + rand + " finished");
        }
        catch(Throwable e){System.out.println(e);}
    }
}
