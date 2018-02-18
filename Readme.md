### FollowerMaze

Build the project with `mvn clean package`
After which you can run the project `java -jar target/follower-0.0.1-SNAPSHOT.jar -Xmx1G`

### Inputs: (Optional)

The program by default listens to 
`eventListenerPort` at `9090` and `clientListenerPort` at `9099`

It can be changed by setting the environment variables `eventListenerPort` and `clientListenerPort`

##### 3rd Party Libraries used

1. Log4j2: For all the logging related functions.

