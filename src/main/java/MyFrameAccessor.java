import ki.types.ds.Block;
import ki.types.ds.StreamInfo;
import se.umu.cs._5dv186.a1.client.FrameAccessor;
import se.umu.cs._5dv186.a1.client.StreamServiceClient;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class MyFrameAccessor implements FrameAccessor {
    private StreamServiceClient[] clients;
    private StreamInfo currentStream;
    private ImpPerformanceStatics performanceStatistics;
    private HashMap<String, ServiceRecorder> performance = new HashMap<>();
    private ExecutorService executorService;
    private int framesCount;
    private long startTime;
    private long endTime;



    public MyFrameAccessor(StreamServiceClient[] clients, StreamInfo stream){
        this.clients = clients;
        this.currentStream = stream;
        for(StreamServiceClient client:clients){
            performance.put(client.getHost(),new ServiceRecorder());
        }
        performanceStatistics = new ImpPerformanceStatics(performance);
        executorService = Executors.newFixedThreadPool(clients.length);
        startTime = System.currentTimeMillis();
    }
    public int getFramesCount(){
        return framesCount;
    }

    public StreamInfo getStreamInfo(){
        return currentStream;
    }

    public Frame getFrame(int frameID) throws SocketTimeoutException {
        ImpFrame frame = new ImpFrame(frameID);
        int blockWidth = this.currentStream.getWidthInBlocks();
        int blockHeight = this.currentStream.getHeightInBlocks();
        for (int i = 0; i < blockWidth; i++) {
            for (int j = 0; j < blockHeight; j++) {

                // Arguments for the constructor of GetBlock
                // ServiceRecorder serviceRecorder, StreamServiceClient client, String streamName, int frameID, int blockX, int blockY

                int clientIndex = j%clients.length;
                ServiceRecorder  serviceRecorder = this.performance.get(clients[clientIndex].getHost());
                StreamServiceClient client = clients[clientIndex];

                GetBlock task = new GetBlock(serviceRecorder, client, this.currentStream.getName(), frameID, i,j);
                try{
                    Future<Block> block = executorService.submit(task);
                    if(block.get()!=null){
                        frame.setBlock(i,j,block.get());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
        this.framesCount++;
        return frame;
    }
    public void closeAccessor(){
        executorService.shutdown();

        endTime = System.currentTimeMillis();

        long runningTime = endTime-startTime;
        performanceStatistics.update(runningTime,framesCount);


    }



    public static class GetBlock implements Callable<Block> {
        private ServiceRecorder serviceRecorder;
        private StreamServiceClient client;
        private String streamName;
        private int frameID;
        private int blockX;
        private int blockY;

        public GetBlock(ServiceRecorder serviceRecorder, StreamServiceClient client, String streamName, int frameID, int blockX, int blockY){
            this.serviceRecorder = serviceRecorder;
            this.client = client;
            this.streamName = streamName;
            this.frameID = frameID;
            this.blockX = blockX;
            this.blockY = blockY;

        }
        @Override
        public Block call() throws Exception {
            try {
                serviceRecorder.incBlockCount();
                long t1 = System.currentTimeMillis();
                Block block = client.getBlock(streamName,frameID,blockX,blockY);
                long t2 = System.currentTimeMillis();
                int latency = (int) (t2-t1);
                serviceRecorder.addLatency(latency);
                return block;
            } catch (SocketTimeoutException e) {
                serviceRecorder.incDropCount();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;

        }
    }


    public PerformanceStatistics getPerformanceStatistics() {
        return this.performanceStatistics;
    }

    public static class ImpFrame implements Frame{
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
        public Block getBlock(int blockX, int blockY){
            return this.block[blockX][blockY];
        }

        public void setBlock(int x, int y, Block block) {
            this.block[x][y] = block;
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

        public ImpPerformanceStatics(HashMap<String, ServiceRecorder> performance){
            this.performance = performance;
        }

        public void update(long runningTime, int framesNum){
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
