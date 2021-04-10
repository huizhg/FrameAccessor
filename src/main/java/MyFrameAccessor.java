import ki.types.ds.Block;
import ki.types.ds.StreamInfo;
import se.umu.cs._5dv186.a1.client.FrameAccessor;
import se.umu.cs._5dv186.a1.client.StreamServiceClient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

public class MyFrameAccessor implements FrameAccessor {
    private StreamServiceClient[] clients;
    private StreamInfo currentStream;
    private PerformanceStatistics performanceStatistics;



    public MyFrameAccessor(StreamServiceClient[] clients, StreamInfo stream){
        this.clients = clients;
        this.currentStream = stream;


    }
    public StreamInfo getStreamInfo(){
        return currentStream;
    }

    public Frame getFrame(int frameID) throws SocketTimeoutException {
        Frame frame = new ImpFrame(frameID);
        int blockWidth = this.currentStream.getWidthInBlocks();
        int blockHeight = this.currentStream.getHeightInBlocks();
        for (int i = 0; i < blockWidth; i++) {
            for (int j = 0; j < blockHeight; j++) {
                //Todo
            }

        }



        return null;
    }

    public class GetBlockRunnable implements Runnable{
        private ServiceRecorder serviceRecorder;
        private StreamServiceClient client;
        private String streamName;
        private int frameID;
        private int blockX;
        private int blockY;

        public GetBlockRunnable(ServiceRecorder serviceRecorder, StreamServiceClient client, String streamName, int frameID, int blockX, int blockY){
            this.serviceRecorder = serviceRecorder;
            this.client = client;
            this.streamName = streamName;
            this.frameID = frameID;
            this.blockX = blockX;
            this.blockY = blockY;

        }

        public void run() {
            try {
                serviceRecorder.incBlockCount();
                long t1 = System.currentTimeMillis();
                client.getBlock(streamName,frameID,blockX,blockY);
                long t2 = System.currentTimeMillis();
                int latency = (int) (t2-t1);
                serviceRecorder.addLatency(latency);
            } catch (SocketTimeoutException e) {
                serviceRecorder.incDropCount();
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }


    public PerformanceStatistics getPerformanceStatistics() {
        return this.performanceStatistics;
    }

    public class ImpFrame implements Frame{
        private Block[][] block;
        private int frameID;

        public ImpFrame(){

        }
        public ImpFrame(int frameID){
            this.frameID = frameID;
        }


        public ImpFrame(int frameID, Block[][] block){
            this.frameID = frameID;
            this.block = block;
        }
        public Block getBlock(int blockX, int blockY) throws SocketTimeoutException {
            return this.block[blockX][blockY];
        }
        public int getFrameID(){
            return frameID;
        }



    }


    public class ImpFactory implements Factory{

        public FrameAccessor getFrameAccessor(StreamServiceClient streamServiceClient, String streamName) {
            StreamServiceClient[] clients = new StreamServiceClient[]{streamServiceClient};
            try {
                 StreamInfo[] streamInfos = clients[0].listStreams();
                for (StreamInfo eachStream : streamInfos) {

                    if(eachStream.getName().equals(streamName)){
                        return new MyFrameAccessor(clients,eachStream);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        public FrameAccessor getFrameAccessor(StreamServiceClient[] streamServiceClients, String streamName) {
            try {
                StreamInfo[] streamInfos = streamServiceClients[0].listStreams();
                for (StreamInfo eachStream : streamInfos) {
                    if(eachStream.getName().equals(streamName)){
                        return new MyFrameAccessor(streamServiceClients,eachStream);
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static class ServiceRecorder{
        private int blockCount;
        private int dropCount;
        private ArrayList<Integer> latency;

        private ServiceRecorder(){
            latency = new ArrayList<>();
        }


        public ArrayList<Integer> getLatency() {
            return latency;
        }

        public void  addLatency(int latency){
            this.latency.add(latency);
        }

        public int getBlockCount() {
            return blockCount;
        }

        public int getDropCount() {
            return dropCount;
        }

        public void incBlockCount(){
            this.blockCount ++;
        }
        public void incDropCount(){
            this.dropCount ++;
        }


    }

    public static class ImpPerformanceStatics implements PerformanceStatistics{
        private HashMap<String, ServiceRecorder> performance;
        private long runningTime;
        private int framesNum;

        public ImpPerformanceStatics(HashMap<String, ServiceRecorder> performance, long runningTime, int framesNum){
            this.performance = performance;
            this.runningTime = runningTime;
            this.framesNum = framesNum;
        }


        // Not need to implement getLinkBandWidth
        public double getLinkBandwidth(String hostName) { return 0; }

        public double getPacketDropRate(String hostName) {
            ServiceRecorder serviceRecorder = performance.get(hostName);

            int totalBlockNum = serviceRecorder.getBlockCount();
            int dropPacketNum = serviceRecorder.getDropCount();
            return dropPacketNum *1.0/totalBlockNum;
        }

        public double getPacketLatency(String hostName) {
            ServiceRecorder serviceRecorder = performance.get(hostName);
            ArrayList<Integer> latency = serviceRecorder.getLatency();
            return latency.stream().mapToInt(val ->val).average().orElse(0.0);
        }

        public double getFrameThroughput() {
            return 1.0*framesNum/runningTime;
        }

        public double getBandwidthUtilization() {
            int bitsPerBlock = 3*8*16*16; // One block contains 16*16 pixels, one pixel has 3 bytes R,G,B fields.
            int totalBlockNum = 0;
            int totalDropNum = 0;
            int receivedBlockNum;
            for(String hostName : performance.keySet()){
                ServiceRecorder serviceRecorder = performance.get(hostName);
                totalBlockNum += serviceRecorder.getBlockCount();
                totalDropNum += serviceRecorder.getDropCount();
            }
            receivedBlockNum = totalBlockNum - totalDropNum;

            return 1000.0*receivedBlockNum*bitsPerBlock/runningTime; // The runningTime's unit is millisecond.
        }
    }
}
