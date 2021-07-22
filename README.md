This assignment will focus on creating your own web server from scratch. During the introductory weeks you have made programs that handle user input from the command line. Conceptually, a web server isn't all that much different. In a web server, the user doesn't interact with your application directly. Instead, the user input will reach your application via the internet. For you as a programmer, that will mean that you won't use `System.in` as input, but a so-called socket. Using low-level primitives like this, we will build our own web server framework.

This skeleton project has been set-up using Gradle. As a reminder:

```bash
# Building
./gradlew build
# Testing (will fail with the initial code)
./gradlew test
# Running
./gradlew run
```

## Step 1: listening loop

This will focus on the general concepts of a web server. In this repository, we have created a simple stub. The code is documented rather thoroughly, which should aid you in understanding what the role of every cog in the machinery is. Roughly, the basic premise is:

```bash
1. Ensure that your program listens to TCP communications on a specified port.
2. Repeat infinitely:
     a. Receive an incoming connection on a socket.
     b. Handle the message in a background thread.
```

These steps are found in the `main` method. The application is started and prepares the program to receive calls from the internet. The program listens to incoming messages on port 9090. An incoming connection is assigned to a socket (basically a phone wire). The socket can be used to send messages back and forth. The basics are configured and your application can now communicate with other computers. Generally, your computer and network prevents other computers from connecting to it using a firewall. The firewall is configured conservatively in order to reduce the attack surface of your machine.

## Step 2: Basic communication

We will now look at the message handling. In the initial code, this boils down to

```bash
1. Read the incoming message line by line. Repeat until we receive an empty line:
     a. Read a line from the socket's input stream.
     b. Echo the line to standard output (console).
2. Send a simple message to the client by writing it to the socket's output stream.
```

By implementing these two steps, we accomplished very basic communication between a client and your server. You can read the code to look at the implementation details. After receiving a request, we will print all received information to the console. Also, the response we sent is received by the client. You can manually verify this by running the program. Your server is reachable on `localhost:9090` (this machine, port 9090). Later on we will see that the response we send is not conform the [HTTP protocol](https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol). This implies that the average client will not accept the answer of your web server and show an error page instead of our reply. You can use [Postman](https://www.getpostman.com/) or [curl](https://curl.haxx.se/) to show the reply.

## Assignment

The global assignment is to extend the web server. The web server should conform to the HTTP protocol. You will build an abstraction layer on top of the socket primitives. This will enable us to reason about the various data types, like headers, HTTP has, without concerning the formatting of the original request. We will build a class that reads the incoming message and transforms it into an HTTP request object. Likewise, we want to define our answer as an HTTP response object, which can format itself to be written to the socket's output.

### Assignment 1: HTTP request

A functioning web server should first and foremost be able to interpret the incoming request message. The information in the request message can then decide what application logic (methods) will be invoked. This principle is known as 'routing': determining which path resolves to which method. An [HTTP request message](https://tools.ietf.org/html/rfc7230#section-3) is formatted like:

```xml
<http method> <resource path> <http version>
<header parameter1>: <header parameter1 value>
<header parameter2>: <header parameter2 value>
…
<header parameterN>: <header parameterN value>
[empty line]
<body line1>
…
<body lineN>
```

The first line consists of the [HTTP method](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods), the requested resource path (`/foo` in `localhost:9090/foo`) and the HTTP version. This line determines a hefty chunk of how the server should respond. For this assignment, we will assume the relatively simple HTTP/1.1 protocol. In HTTP/1.1 the client sends a request and the server sends a response - that's it. [HTTP/2](https://daniel.haxx.se/http2/) allows multiplex connections, in which the server can initiate another message, a so-called server push. This can be used for real time blogs, chat rooms or simply to reduce the amount of requests a browser needs to initiate. As of February 2021, HTTP/3 is in an "internet draft" state with multiple different prototype implementations.

Back to our HTTP/1.1 implementation. The first block after the first line are the so-called headers. These are request metadata that a client (or the server itself, as we will see when building the response) can send with the request. The internet, mainly HTTP/1.1, is stateless - in principle a web server has no means to know who sent the request and if that person already made several requests. One way of identifying yourself is the well-known cookie. Cookies are a standardized header that contain bite-sized pieces of information so that the server can identify the client. A client can also include technical limitations ("hey, I'm using Internet Explorer") or user preferences ("please give me the Dutch translation") in their headers.

The headers are _always_ followed by a blank line. This way, the server can know that all headers arrived. The blank line is also when the initial program stops reading the request. If there is one, the body of the request message (usually only with the PUT/POST method) follows after the blank line. The structure of the body is determined by the one announced as [MIME type](https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types) in the header (e.g. `application/json`).

The eagle-eyed among you will notice that in the HTTP method, the resource path and the HTTP version do not allow spaces. The line endings are Windows (`\r\n`).

An example:

```
GET /books HTTP/1.1
Host: localhost:9090
User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; nl; rv:1.9.0.11) Gecko/20100101 Firefox/40
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: nl,en-us;q=0.7,en;q=0.3
Accept-Encoding: gzip,deflate
Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
Keep-Alive: 300
Connection: keep-alive

```

An interface for the HTTP request is in the repository. This interface contains the high-level methods returning above information. Write a class that implements this interface (`class Foo implements Request`). If you do this, you'll notice some compiler errors. Implementing an interface required the class to implement all methods that are defined in the interface. Write an implementation for the first four methods. The other two methods can suffice with a dummy implementation - the real implementation will be made in the third assignment. Consider how the class can best encapsulate the information required to implement the interface.

If you parsed the request, you can modify the response. For example, you can reply "You did an HTTP GET request and you requested the following resource: /books", reflecting the incoming message.

### Assignment 2: HTTP response

The next step is to format the web server response. So far we just replied with a simple line of text. Not all clients can handle this properly. The second assignment is to reply with a formally correct HTTP response. Much like the HTTP request, the response also has a set structure:

```xml
<http version> <response status code> <brief description of the status code> 
<header parameter1>: <header parameter1 value>
<header parameter2>: <header parameter2 value>
…
<header parameterN>: <header parameterN value>
[empty line]
<body line1>
…
<body lineN>
```

Generally, the response is structured roughly the same as the request. The response has a first line, a set of headers, a blank line and optionally a body. The first line explains if and how the server was able to handle the request. Examples of status codes are 200 OK, 404 not found, 500 internal server error. There are [many more](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes). For this assignment, you can pin the HTTP version to HTTP/1.1.

The following example is from an Apache web server:

```
HTTP/1.1 200 OK
Date: Tue, 21 Jul 2015 11:02:13 GMT
Server: Apache/2.4.16 (Unix) OpenSSL/1.0.2d PHP/5.4.45
Connection: close
Content-Type: text/html; charset=UTF-8
Content-Length: 90

<html>
<body>
You did an HTTP GET request.<br/>
Requested resource: /books
</body>
</html>
```

This example contains a response with a body - generally all responses contain a body. When the server replies with a body, the headers `Content-Type` (MIME type) and `Content-Length` (measured in bytes) *must* be supplied and correct. Otherwise, clients will deny the response. `Connection: close` means that the server closes the socket after the reply has been sent. If the client wants to initiate another request, they need to open another connection to do so. `Date` supplies the server date in [RFC1123 format](https://www.ietf.org/rfc/rfc1123.txt). The `Server` header can contain arbitrary text ("Galloping fabulous unicorns" is a valid server name) and is mainly used as identification. Theoretically, the client can determine what features are supported on said server and browsers can work around known bugs. In practice however, this isn't really used. The other way around is more common: the server serves a different page to users of Internet Explorer or `myexampleapp.com/download` takes you directly to the download page for your operating system.

You will find an HTTP response interface inside of the repository. The interface defines the characteristics for the HTTP response. Implement this interface in a new class. Also make sure that the response is properly formatted and written to the socket's output stream. If everything is implemented correctly, you will see the text "You did an HTTP GET request..." in your browser. You won't see the headers on the screen even though your browser will use them to interpret the response. You can look in your favorite browser's development tools (usually F12 opens the developer tools). You can track the network request (possibly after refreshing the page while the tools are opened) and inspect every detail of the response.

### Assignment 3: Request parameters

You implemented the minimal viable HTTP request in the first assignment. The format is parsed into a typed object. Sometimes a clients wants to supply additional information to the server. For example, a search query in your favourite search engine. This isn't really a part of the metadata, but an actual part of the request itself. One way of sending these kinds of information is URL parameters. This is the only way to send data using a GET request. It looks like:

```
GET /search?hl=nl&q=java HTTP/1.1
…
```

The requested resource path is still `/search`. Two parameters are supplied: `hl` and `q` and their respective values `nl` and `java`. The URL can be generalised to

```
<resource path>?<parameter1 name>=<parameter1 value>&…&<parameterN name>=<parameterN value>
```

In an HTTP POST request there are multiple ways to send data. You can still use URL parameters. You can also use the body of the request. The body can contain data in an arbitrary format. However, to ensure client and server understand each other, the applied format is specified in the `Content-Type` header. Aside from offering more options when posting data in the body, it is also more thoroughly encrypted using HTTPS (SSL). A complete POST request looks like:

```
POST /search HTTP/1.1
…
Content-Length: 12
Content-Type: application/x-www-form-urlencoded

hl=nl&q=java
```

Implement the (URL) parameter methods in your Request class. Make sure that the implementation can both handle GET and POST requests. Echo the received parameters to the response, for example:

```
You did an http GET request and you requested the following resource: /books


The following header parameters where passed:
Host: localhost:9090
Connection: keep-alive
User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/530.5 (KHTML, like Gecko) Chrome/2.0.172.37 Safari/530.5
Cache-Control: max-age=0
…

The following parameters were passed:
Titel: SCWCD Exam Study Kit – Java Web Component Developer Certification
Publisher: Manning

```

You can use [Postman](https://www.getpostman.com/) or curl to test your POST request.

## Conclusion

You just made a simple web server that reads an incoming request (as a byte stream). The raw input then gets converted into a high-level class, which we developers can understand. You've also written a function that translates a high-level response class into the raw HTTP format. You've made a wrapper layer around the socket primitives. This is known as a framework - something you and other developers can use to build on top of. You can make a whole web application on top of this server without being concerned by the nitty-gritty details.

It's a fun challenge to modify your web server slightly so that you can serve the website you've made during the front-end course. Instead of hard-coding your response, you can also read files dynamically from the file system based on the requested resource. With a few lines of code, you have a fully functioning website. For now that will be relatively static - there is no code or behaviour that allows you to edit and persist data. The final step would be to allow your web server to call certain Java methods based on the resource path (i.e. `/api/attractions` could call `new AttractionRepository().findAll()`).

In future courses, you will use a web server framework made, and most importantly, battle-tested by others. However, it's just code, just like you've written today.

