This web server is created from scratch. 

This skeleton project has been set-up using Gradle. As a reminder:

```bash
# Building
./gradlew build
# Testing (will fail with the initial code)
./gradlew test
# Running
./gradlew run
```

## What the code does


A simple web server was made that reads an incoming request as a byte stream. The raw input then gets converted into a high-level class. We've also written a function that translates a high-level response class into the raw HTTP format. There is a wrapper layer around the socket primitives. This is known as a framework - something you can use to build on top of. You can make a whole web application on top of this server without being concerned by the nitty-gritty details.