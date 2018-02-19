# FollowerMaze
Build the project with `mvn clean package`
After which you can run the project `java -jar target/follower-0.0.1-SNAPSHOT.jar -Xmx1G`

# Inputs: (Optional)
The program by default listens to 
`eventListenerPort` at `9090` and `clientListenerPort` at `9099`

It can be changed by setting the environment variables `eventListenerPort` and `clientListenerPort`

##### 3rd Party Libraries used

1. Log4j2: For all the logging related functions.
2. Mockito: For mocking socket calls in unit tests

#### Performance test

**Run 1**: (DEFAULT) 

	10000000 Events
	
	1000 numberOfUsers 
	
	100 maxEventSourceBatchSize

**425 seconds** 


**Run 2** :  (Exceeding Batch size ->1 )

	10000000 Events
	
	1000 numberOfUsers 
	
	1000 maxEventSourceBatchSize

**394 Seconds**

**Run 3** :  (Exceeding Batch size ->2 )

	10000000 Events
	
	1000 numberOfUsers 
	
	5000 maxEventSourceBatchSize

**421 Seconds** 


**Run 4** : (Maxing Number of default users * 10)

	10000000 Events
	
	10000 numberOfUsers 
	
	10000 maxEventSourceBatchSize

**7937 seconds**

**Run 5**:(Maxing Number of default concurrency * 10 and also Increasing the batchsize)

	10000000 Events
	
	maxEventSourceBatchSize=30000 
	
	numberOfUsers=1000 
	
	concurrencyLevel=1000

**790 SECONDS**.

**Run 6**: (Maxing the concurrency to default * 100)

	10000000 Events
	
	maxEventSourceBatchSize=30000 
	
	numberOfUsers=1000 
	
	concurrencyLevel=10000

**802 SECONDS**

**Run 7**: (Variance between user and concurrency)

	10000000 Events
	
	maxEventSourceBatchSize=50000 
	
	numberOfUsers=100
	
	concurrencyLevel=10000

**591 SECONDS**

#### Developer overview :

The implementation is built using observer pattern. 
`FollowerMaze` is the actual start point call by the `main` method. 
It has two main functionality `startup` and `shutdown`.

Its started in 3 threads: 

* `readInputstreamAndEnqueue`
	
	Waits for the clients to be connected and 
	registers each with the observable.

* `eventProducer`

	Watches the min priority queue for the next message to 
	be processed and increments the message count.

* `waitForClientsAndSubscribe`
	
	Reads the stream incoming from the event socket 
	and on input, Processes the input and enqueues

