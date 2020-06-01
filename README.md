# Assignment 1

The purpose of this assignment is to introduce the student to high-level concurrent network programming and performance analysis of complex distributed systems.
In this assignment, the students are given the task of implementing a simple client that pulls data (pixel blocks) for a streaming video application, and (from the client's point of view) analyze the performance of the system. The video data is provided by a distributed video application consisting of a set of streaming services with interfaces for accessing video data in pixel blocks. To facilitate analysis of the system, a strictly hierarchical format for the video data is used.

- a (video) _stream_ consists of a set of temporally ordered (video) _frames_,
- a _frame_ consists of a set of spatially ordered (pixel) _blocks_, and
- a _block_ consists of a set of spatially ordered (16x16) _pixels_.

Each service hosts a set of streams (identified by name), and each stream is homogeneous in format (all frames and blocks are the same size within a stream). To simplify reasoning about system performance trade-offs pixel blocks are transmitted in raw format (i.e. no video streaming or compression formats are used) in the assignment application. Note that this is an unrealistic artificial simplification that is unlikely to be found in real video streaming applications.

A simple framework that abstracts low-level system communication details is provided to allow the student to focus on the analysis parts of the assignment. The framework exposes a set of client stubs that can be used to communicate with services by exchanging UDP-based messages in a request-response pattern. The client stubs provide a very simple API that exposes a set of data types and methods to retrieve video stream data from a host:

```
class Pixel
{
  byte r;              // red
  byte g;              // green
  byte b;              // blue
}

class Block
{
  Pixel[] pixels;      // 16x16 pixel block
}

class StreamInfo
{
  String name;         // stream name, non-empty string
  int lengthInFrames;  // #frames in stream
  int widthInBlocks;   // frame width (in blocks)
  int heightInBlocks;  // frame height (in blocks)
}


public interface StreamServiceClient
{
  public String getHost ();

  public StreamInfo[] listStreams ()
    throws IOException, SocketTimeoutException;

  public Block getBlock (String stream, int frame, int blockX, int blockY)
    throws IOException, SocketTimeoutException;
}
```

To instantiate the client stubs, use the `bind()` method in the `DefaultStreamServiceClient` class:

```
// host     = the name / ip address of the server to bind to
// timeout  = socket timeout in milliseconds
// username = student username (see explanation below)
StreamServiceClient client = DefaultStreamServiceClient.bind(host,timeout,username);
```

Use of the interfaces is demonstrated in an example Java client. The example can be run with a shell command similar to this

`java -cp "5dv186a1.jar" se.umu.cs._5dv186.a1.client.ExampleClient itchy.cs.umu.se 1000 test`

(where `itchy.cs.umu.se` is the host name, `1000` is the socket timeout in milliseconds, and `test` should be replaced with the student's username). Stream servers are installed on the following hosts:

```
itchy.cs.umu.se
scratchy.cs.umu.se
salt.cs.umu.se
peppar.cs.umu.se
```

For convenience, a service host list can be accessed through

`String[] hosts = StreamServiceDiscovery.SINGLETON.findHosts();`

This API is implemented in communication stubs in a provided UDP-based communication framework that is aimed to hide (some) low-level communication details and raise the abstraction level of the system communication. Note that while the API defines blocking methods, it relies on an unreliable communication model, and users of the handed-out API (i.e. the students) need to manage issues arising from packet drops, e.g., handling packet resends using threading and invocation timeouts.

As noted in the interface, students are to provide their CS username as a parameter when instantiating and using the provided communication stubs. This is required as the provided application services contain individually configurable network quality rate limitations for link latencies and packet drop rates that are used to simulate variations in network link qualities.

The task of the student is to (using the provided communication stubs) implement video service clients and experimentally estimate the following performance metrics:

| Metric                                          | Level       | Unit                    |
|-------------------------------------------------|-------------|-------------------------|
| (UDP) packet drop rate (per service)            | transport   | percentage (%)          |
| (average) packet latency (per service)          | transport   | milliseconds (ms)       |
| (average) frame throughput                      | application | frames per second (fps) |
| bandwidth utilization (total network footprint) | application | bits per second (bps)   |

Student solutions shall implement a provided `FrameAccessor` interface (all referenced interfaces in the file), as well as construct a factory class that can be used to instantiate the interface (hint: one factory class per `FrameAccessor` implementation makes life easier). The factory classes shall implement the `FrameAccessor.Factory` interface and need to have a publicly accessible default constructor (a public constructor with no parameters).

A note on exceptions: The `FrameAccessor` interface is designed to support multiple different implementation patterns, and due to this, all potentially data-fetching methods list exceptions in the interface. However, this does not mean that a particular `FrameAccessor` implementation is expected to use data fetching methods (i.e. the underlying `StreamSocketClient.getBlock()`) in each of these methods - in fact, most `FrameAccessor` implementations will not.

Students are to implement two types of service clients: sequential and parallel; and to estimate and analyze the system performance in each mentioned dimension for both types of clients. The particular patterns of the parallel implementation are left to the student as part of the exercise, and the student is encouraged to experiment with different levels of parallelism in their communication patterns. Note: as the supplied video services contain individually configured network link simulators, students are likely to get varied results and values for these metrics.

## Report

For this assignment, the student is not required to write a full assignment report, but the core metrics described above need to be analyzed and presented (including a description of how they are measured and evaluated). Identify key performance indicators from the metrics above (or others that you define) and analyze how these are affected by use of parallel data transfers. In your report, be sure to include an analysis of the effect of packet drop rates on latency and throughput, and a discussion of how this effect can be mediated. Also discuss how the use of video compression techniques would impact the performance metrics analyzed in the assignment (e.g., discuss which ones would be affected and how).

## Extra Credit

For extra credit, implement a buffer that caches the first 100 frames of a stream and investigate and illustrate jitter (variations) in system latency and throughput over time. How long does it take to fill the buffer to a) 50%? b) 80%? c) 95%? d) 99%? 100%? Identify and reason about the (potential) presence of outliers in your data and make suggestions for how the client system can be improved. Analyze and discuss the impact of network-level performance limitations on application-level qualities of service (e.g., video frame rate), and exemplify how application-aware implementation techniques, e.g., buffering and pre-caching of data, can be used to hide the impact of network performance limitations in video streaming systems. Suggest a user-level metric for perceived quality of service.

## Development Environment

For this assignment, students are provided with a UDP-based communication framework. All necessary files to start working with the assignment are available in the Cambro resources folder for the assignment.

The student may very easily import the framework through maven by attaching the following snippets to their pom:

```
<repositories>
    <repository>
        <id>5dv186</id>
        <url>https://people.cs.umu.se/~axelj/5dv186a1</url>
    </repository>
</repositories>


<dependencies>
    <dependency>
        <groupId>se.umu.cs</groupId>
        <artifactId>5dv186a1</artifactId>
        <version>1.2</version>
    </dependency>
</dependencies>
```

## Submission

The assignment is to be solved in groups of two (or three in an exceptional case). 
You should submit the solution using [labres](https://webapps.cs.umu.se/labresults/v2/handin.php?courseid=435). Make sure to add the names of your group members when submitting.

You will submit your solution as a single JAR file containing all code needed to run your solution. Note that it must be runnable on the department's computer systems, and compiled with Java 1.8.

When submitting on labres, an automatic test suite will be run to make sure that your solution is runnable. In order for the test to work, your must provide a text file named `fqns.txt` that specifies the fully qualified class names (class names including full package names, e.g., `se.umu.cs.5dv186.a1.username.MyFrameAccessorFactory`) of your `FrameAccessor.Factory` implementation class(es). For solutions with more than one factory class, specify one fully qualified class name per line in the file.

Required files:

```
5dv186a1_usernames.jar
fqns.txt
report_usernames.pdf
```

Where `usernames` should be replaced with the usernames of all group members, separated by underscores.

## Deadline

See the Examination tab on Cambro for assignment deadlines.
