import ki.types.ds.Block;
import ki.types.ds.StreamInfo;
import se.umu.cs._5dv186.a1.client.FrameAccessor;
import se.umu.cs._5dv186.a1.client.StreamServiceClient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImpFrameAccessor implements FrameAccessor {

    private StreamServiceClient[] streamServiceClients;
    private StreamInfo streamInfo;
    private int numOfClients;
    private  ExecutorService threadPool;


    public ImpFrameAccessor(StreamServiceClient[] streamServiceClients, StreamInfo streamInfo){
        this.streamServiceClients = streamServiceClients;
        this.streamInfo = streamInfo;
        numOfClients = streamServiceClients.length;
        ExecutorService threadPool = Executors.newFixedThreadPool(numOfClients);
        // ExecutorService threadPool = new ThreadPoolExecutor(numOfClients , numOfClients, 3, TimeUnit.SECONDS );

    }


    @Override
    public StreamInfo getStreamInfo() throws IOException, SocketTimeoutException {
        return this.streamInfo;
    }

    @Override
    public Frame getFrame(int frameID) throws IOException, SocketTimeoutException {
        Frame frame = new ImpFrame(frameID);
        int frameWidth = streamInfo.getWidthInBlocks();
        int frameHeight = streamInfo.getHeightInBlocks();
        for (int i = 0; i < frameWidth; i++) {
            for (int j = 0; j < frameHeight; j++) {
              //TODO: Need to be finished. (How to assign threads to clients )
                //  threadPool.execute(new GetBlock());

            }

        }

    return frame;
    }

    @Override
    public PerformanceStatistics getPerformanceStatistics() {
        return null;
    }

    public class ImpFactory implements Factory {

        public ImpFactory(){

        }

        @Override
        public FrameAccessor getFrameAccessor(StreamServiceClient streamServiceClient, String stream) {
            return null;
        }

        @Override
        public FrameAccessor getFrameAccessor(StreamServiceClient[] streamServiceClients, String stream) {
            return null;
        }
    }
    private class GetBlock implements Runnable{
        private StreamServiceClient client;
        private int frame;
        private int blockX;
        private int blockY;
        private String streamName;

        GetBlock(StreamServiceClient client, String streamName, int frame, int blockX, int blockY){
            this.client = client;
            this.frame = frame;
            this.blockX = blockX;
            this.blockY = blockY;
            this.streamName = streamName;

        }

        @Override
        public void run() {
            try {
                long t1 = System.currentTimeMillis();
                client.getBlock(streamName, frame, blockX, blockY);
                long t2 = System.currentTimeMillis();
                System.out.println("block received from server in " + (t2 - t1) + " ms");
                //TODO
                // Increment the count num of getBlock
            } catch (SocketTimeoutException e){
                //TODO increment the drop block count


            } catch (IOException var18) {
                System.out.println("socket timeout");
            }

        }
    }


    public class ImpPerformanceStatistics implements PerformanceStatistics {

        // No need to implement the getLinkBandwidth method.
        @Override
        public double getLinkBandwidth(String s) {
            return 0;
        }

        @Override
        public double getPacketDropRate(String s) {
            return 0;
        }

        @Override
        public double getPacketLatency(String s) {
            return 0;
        }

        @Override
        public double getFrameThroughput() {
            return 0;
        }

        @Override
        public double getBandwidthUtilization() {
            return 0;
        }
    }


    public class ImpFrame implements Frame {
        Block[][] blocks;
        int frameID;

        public ImpFrame(int frameID){
            this.frameID = frameID;
        }


        @Override
        public Block getBlock(int blockX, int blockY) throws IOException, SocketTimeoutException {
            return blocks[blockX][blockY];
        }

        public int getFrameID(){
            return frameID;
        }

    }
}