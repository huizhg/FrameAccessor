import ki.types.ds.StreamInfo;
import se.umu.cs._5dv186.a1.client.DefaultStreamServiceClient;
import se.umu.cs._5dv186.a1.client.FrameAccessor;
import se.umu.cs._5dv186.a1.client.StreamServiceClient;

import java.io.IOException;

public class Client {
    public static void printStreamInfo(StreamServiceClient client) throws IOException {
        StreamInfo[] streams = client.listStreams();
        System.out.println("found " + streams.length + " streams");

        for(StreamInfo stream : streams) {
            System.out.println("  '" + stream.getName() + "': " + stream.getLengthInFrames() + " frames, " + stream.getWidthInBlocks() + " x " + stream.getHeightInBlocks() + " blocks");
        }

    }
    /*
    // Use this main method when you use the arguments from user's input
    public static void main(String[] args) {
    if(args.length<4){
            System.out.println("Usage: user, timeout, streamName, host[] ");
        }
        String user = args[0];
        int timeout = Integer.parseInt(args[1]);
        String streamName = args[2]
        StreamServiceClient[] clients = new StreamServiceClient[args.length-3];
        int defaultFramesNum = 10;
        String[] hosts = new String[args.length-3];

        try {
            for (int i = 3; i < args.length; i++) {
                clients[i - 3] = DefaultStreamServiceClient.bind(args[i], timeout, user);
                hosts[i-3] = args[i];
            }
            printStreamInfo(clients[0]);

            MyFrameAccessor.ImpFactory factory = new MyFrameAccessor.ImpFactory();

            FrameAccessor frameAccessor = factory.getFrameAccessor(clients, streamName);

            for (int i = 0; i < defaultFramesNum; i++) {
                FrameAccessor.Frame frame = frameAccessor.getFrame(i);
                //System.out.println("get frame" + i);
            }

            ((MyFrameAccessor)frameAccessor).closeAccessor();


            FrameAccessor.PerformanceStatistics statistics = ((MyFrameAccessor)frameAccessor).getPerformanceStatistics();

            for(String host : hosts){
                System.out.println("The drop rate of service " + host + " is" + statistics.getPacketDropRate(host));
                System.out.println("The packet latency of service" + host + " is " +statistics.getPacketLatency(host) + " ms");
            }
            System.out.println("the bandwidth Utilization is " + statistics.getBandwidthUtilization() + "bps");
            System.out.println("The frame throughput is" + statistics.getFrameThroughput() + " frames per second");

        }catch (Exception e){
            e.printStackTrace();
        }


    }

     */

    public static void main(String[] args) {
        int defaultFramesNum = 3;

        String host1 = "salt.cs.umu.se";
        String host2 = "itchy.cs.umu.se";
        String streamName = "stream8";

        try {

            StreamServiceClient client1 = DefaultStreamServiceClient.bind(host1, 1000, "mrc19hzg");
            System.out.println("bind to " + host1);
            StreamServiceClient client2 = DefaultStreamServiceClient.bind(host2, 1000, "test");
            System.out.println("bind to " + host2);
            StreamServiceClient[] clients = new StreamServiceClient[]{client1, client2};

            printStreamInfo(client1);

            MyFrameAccessor.ImpFactory factory = new MyFrameAccessor.ImpFactory();

            FrameAccessor frameAccessor = factory.getFrameAccessor(clients, streamName);

            for (int i = 0; i < defaultFramesNum; i++) {
                FrameAccessor.Frame frame = frameAccessor.getFrame(i);
                //System.out.println("get frame" + i);
            }

            ((MyFrameAccessor)frameAccessor).closeAccessor();

            FrameAccessor.PerformanceStatistics statistics = frameAccessor.getPerformanceStatistics();

            System.out.println("The drop rate of service " + host1 + " is " + statistics.getPacketDropRate(host1));
            System.out.println("The drop rate of service " + host2 + " is " + statistics.getPacketDropRate(host2));
            System.out.println("The packet latency of service" + host1 + " is " +  statistics.getPacketLatency(host1) + " ms");
            System.out.println("The packet latency of service" + host2 + " is " + statistics.getPacketLatency(host2) + " ms");

            System.out.println("the bandwidth Utilization is " + statistics.getBandwidthUtilization() + " bps");
            System.out.println("The frame throughput is " + statistics.getFrameThroughput() + " frame per second");

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
