import se.umu.cs._5dv186.a1.client.DefaultStreamServiceClient;
import se.umu.cs._5dv186.a1.client.FrameAccessor;
import se.umu.cs._5dv186.a1.client.StreamServiceClient;

import java.io.IOException;

public class Client {
    public static void main(String[] args) {
        String host1 = "salt.cs.umu.se";
        String host2 = "itchy.cs.umu.se";
        String streamName = "stream4";

        int framesNum = 1;
        try {

            StreamServiceClient client1 = DefaultStreamServiceClient.bind(host1, 1000, "test");
            System.out.println("bind to " + host1);
            StreamServiceClient client2 = DefaultStreamServiceClient.bind(host2, 1000, "test");
            System.out.println("bind to " + host2);
            StreamServiceClient[] clients = new StreamServiceClient[]{client1, client2};

            MyFrameAccessor.ImpFactory factory = new MyFrameAccessor.ImpFactory();

            FrameAccessor frameAccessor = factory.getFrameAccessor(clients, streamName);

            for (int i = 0; i < framesNum; i++) {
                FrameAccessor.Frame frame = frameAccessor.getFrame(i);
                System.out.println("get frame" + i);
            }

            ((MyFrameAccessor)frameAccessor).closeAccessor();

           FrameAccessor.PerformanceStatistics statistics = ((MyFrameAccessor)frameAccessor).getPerformanceStatistics();
            System.out.println("the bandwidth Utilization is " + statistics.getBandwidthUtilization());
            System.out.println("The drop rate of service " + host1 + "is" + statistics.getPacketDropRate(host1));
            System.out.println("The drop rate of service " + host2 + "is" + statistics.getPacketDropRate(host2));

        }catch (Exception e){
            e.printStackTrace();
        }



    }
}
