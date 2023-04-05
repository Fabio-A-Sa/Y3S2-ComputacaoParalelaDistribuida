# Compile
javac TimeServer.java
javac TimeClient.java

# Execute server
java TimeServer 8000

# Create clients
java TimeClient 8000
java TimeClient 8000
java TimeClient 8000

# Remove binaries
rm *.class