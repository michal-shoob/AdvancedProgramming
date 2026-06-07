# Advanced Programming Project – Exercise 6

## Overview

This project implements a local HTTP server and a browser-based dashboard for loading, displaying, and interacting with a computational graph.

The system allows the user to upload a configuration file, generate and display the corresponding computational graph, send messages to specific topics, and view the latest value of each topic in a table.

The project is part of the Advanced Programming course and builds on previous exercises, including the custom HTTP server, servlet mechanism, topic manager, agents, generic configuration loading, and graph creation.

---

## Quick Start

To run the project:

1. Open the project in Eclipse.

2. Run the main file:

```text
src/Main/Main.java
```

3. After running the file, the console should display:

```text
Server started -> http://localhost:8080/app/index.html
Press Enter to stop...
```

4. Copy the following URL and open it in a browser:

```text
http://localhost:8080/app/index.html
```

5. The main dashboard will open. From this page, the user can:

   * Upload a configuration file using the `Deploy` button.
   * View the generated computational graph in the center panel.
   * Send messages to topics.
   * View the latest topic values in the table on the right.

6. To stop the server, return to the Eclipse console and press `Enter`.

---

## Project Structure

```text
advancedProgramming6/
├── src/
│   ├── Main/
│   │   └── Main.java
│   │
│   ├── server/
│   │   ├── HTTPServer.java
│   │   ├── MyHTTPServer.java
│   │   └── RequestParser.java
│   │
│   ├── servlets/
│   │   ├── Servlet.java
│   │   ├── ConfLoader.java
│   │   ├── HtmlLoader.java
│   │   └── TopicDisplayer.java
│   │
│   ├── test/
│   │   ├── Agent.java
│   │   ├── BinOpAgent.java
│   │   ├── Config.java
│   │   ├── GenericConfig.java
│   │   ├── Graph.java
│   │   ├── IncAgent.java
│   │   ├── Message.java
│   │   ├── Node.java
│   │   ├── ParallelAgent.java
│   │   ├── PlusAgent.java
│   │   ├── Topic.java
│   │   └── TopicManagerSingleton.java
│   │
│   └── views/
│       └── HtmlGraphWriter.java
│
├── html_files/
│   ├── index.html
│   ├── form.html
│   └── temp.html
│
├── config_files/
│   └── uploaded.conf
│
└── README.md
```

---

## Main Features

### 1. Local HTTP Server

The project includes a custom HTTP server implemented in Java.

The server supports:

* Registering servlets according to HTTP method and URI.
* Handling `GET` and `POST` requests.
* Serving multiple clients using a thread pool.
* Loading static HTML files.
* Handling requests for missing files.
* Closing server resources properly.

The server is started from `Main.java`:

```java
HTTPServer server = new MyHTTPServer(8080, 5);

server.addServlet("GET", "/publish", new TopicDisplayer());
server.addServlet("POST", "/upload", new ConfLoader());
server.addServlet("GET", "/app/", new HtmlLoader("html_files"));

server.start();
System.in.read();
server.close();
```

---

### 2. Browser-Based User Interface

The user interface is located in the `html_files` folder, which is placed outside the `src` folder.

The main page is:

```text
html_files/index.html
```

The page is divided into three main sections:

1. **Left Panel – Controls**
   Contains two forms:

   * A form for uploading a configuration file.
   * A form for sending a message to a topic.

2. **Center Panel – Graph View**
   Displays the computational graph generated from the configuration file.

3. **Right Panel – Topic Values**
   Displays a table containing the latest value of each topic.

After running `Main.java`, the dashboard can be opened in the browser at:

```text
http://localhost:8080/app/index.html
```

---

## Servlets

### TopicDisplayer

`TopicDisplayer` handles `GET` requests to:

```text
/publish
```

Example request:

```text
http://localhost:8080/publish?topic=X&message=5
```

This servlet performs the following actions:

* Extracts the topic name from the `topic` parameter.
* Extracts the message value from the `message` parameter.
* Publishes the message to the matching topic using `TopicManagerSingleton`.
* Returns a valid HTTP response containing an HTML table with two columns:

  * Topic name.
  * The latest value of the topic.

---

### ConfLoader

`ConfLoader` handles `POST` requests to:

```text
/upload
```

The request is sent by the configuration upload form.

This servlet performs the following actions:

* Extracts the uploaded file name and content from the HTTP request.
* Saves the configuration content on the server side.
* Loads the configuration using `GenericConfig`.
* Creates a `Graph` object from the configuration.
* Uses `HtmlGraphWriter` to generate an HTML representation of the graph.
* Returns a valid HTTP response containing the generated graph view.

If the configuration file is invalid, the system returns a clear error page instead of crashing.

For example, uploading an invalid configuration such as:

```text
hello
bad
config
```

returns an error message such as:

```text
Configuration Error
Failed to instantiate agent: hello
```

---

### HtmlLoader

`HtmlLoader` handles `GET` requests for every URI that starts with:

```text
/app/
```

Example requests:

```text
http://localhost:8080/app/index.html
http://localhost:8080/app/form.html
http://localhost:8080/app/temp.html
```

This servlet performs the following actions:

* Extracts the requested HTML file name from the URI.
* Loads the file from the HTML files directory.
* Returns a valid HTTP response containing the file content.
* If the requested file does not exist, returns an HTML error page.

For example, opening:

```text
http://localhost:8080/app/not_exists.html
```

returns:

```text
404 - Not Found
File not found: not_exists.html
```

The HTML folder name is passed to `HtmlLoader` through its constructor:

```java
new HtmlLoader("html_files")
```

This avoids using a hard-coded path inside the class and allows the folder name to be changed easily.

---

## View Layer

The dynamic view layer is implemented by:

```text
views.HtmlGraphWriter
```

This class contains the static method:

```java
public static List<String> getGraphHTML(Graph graph)
```

The method receives a `Graph` object and returns a list of strings representing a complete HTML page.

The generated graph view includes:

* Drawing the graph using HTML Canvas and JavaScript.
* Displaying topics as rectangles.
* Displaying agents as circles.
* Using different colors for topics and agents.
* Displaying node names inside the shapes.
* Drawing directed arrows between graph nodes.
* Displaying topic values when available.

The graph is generated dynamically after a configuration file is uploaded through `ConfLoader`.

---

## Configuration Example

Example of a valid configuration file:

```text
test.PlusAgent
X, Y
Z1
test.IncAgent
Z1
Z2
test.PlusAgent
Z1, W
Z3
test.PlusAgent
Z2, Z3
Result
test.IncAgent
Result
FinalResult
```

This configuration represents the following computation:

```text
Z1 = X + Y
Z2 = Z1 + 1
Z3 = Z1 + W
Result = Z2 + Z3
FinalResult = Result + 1
```

For the input:

```text
X = 5
Y = 3
W = 10
```

The expected output is:

```text
Z1 = 8
Z2 = 9
Z3 = 18
Result = 27
FinalResult = 28
```

---

## More Complex Configuration Example

A larger configuration with several computation branches was also tested:

```text
test.PlusAgent
A, B
S1
test.IncAgent
S1
S2
test.PlusAgent
C, D
S3
test.IncAgent
S3
S4
test.PlusAgent
S2, S4
S5
test.PlusAgent
E, F
S6
test.IncAgent
S6
S7
test.PlusAgent
S5, S7
Result
test.IncAgent
Result
FinalResult
```

For the input:

```text
A = 1
B = 2
C = 3
D = 4
E = 5
F = 6
```

The expected output is:

```text
S1 = 3
S2 = 4
S3 = 7
S4 = 8
S5 = 12
S6 = 11
S7 = 12
Result = 24
FinalResult = 25
```

This example was used to verify that the system also works correctly with a larger and more complex computational graph.

---

## How to Run

1. Open the project in Eclipse.

2. Make sure the `html_files` folder is located outside the `src` folder.

3. Run the following file:

```text
src/Main/Main.java
```

4. The console should print:

```text
Server started -> http://localhost:8080/app/index.html
Press Enter to stop...
```

5. Open the following URL in a browser:

```text
http://localhost:8080/app/index.html
```

6. Use the system:

   * Choose a configuration file.
   * Click `Deploy`.
   * View the graph in the center panel.
   * Send values to topics through the form.
   * View the updated values in the table on the right.

7. To stop the server, return to the Eclipse console and press `Enter`.

---

## Manual Tests Performed

The following manual tests were performed:

* Opened the main dashboard through `/app/index.html`.
* Loaded a valid configuration file.
* Displayed the computational graph.
* Sent values to topics.
* Verified a complete calculation up to `FinalResult`.
* Loaded a more complex configuration file.
* Tested a missing HTML file.
* Tested an invalid configuration file.
* Verified that the server runs on port `8080`.
* Verified that pressing `Enter` in the console stops the server.

---

## Error Handling and Edge Cases

### Missing HTML File

If the user requests an HTML file that does not exist, the system returns an error page instead of crashing.

Example:

```text
http://localhost:8080/app/not_exists.html
```

Returns:

```text
404 - Not Found
File not found: not_exists.html
```

### Invalid Configuration File

If the configuration file is invalid, the system displays a clear error message.

Example invalid file:

```text
hello
bad
config
```

Returns:

```text
Configuration Error
Failed to instantiate agent: hello
```

### Topic Not Included in the Loaded Graph

If a message is sent to a topic that is not part of the loaded configuration, the topic may appear in the values table but will not appear in the graph itself. This is because the graph represents only the loaded configuration.

---

## Design and Package Organization

The project is divided into several packages, each with a clear responsibility.

### `server`

Contains the HTTP server infrastructure:

* `HTTPServer`
* `MyHTTPServer`
* `RequestParser`

### `servlets`

Contains the classes that handle HTTP requests:

* `Servlet`
* `TopicDisplayer`
* `ConfLoader`
* `HtmlLoader`

### `views`

Contains the dynamic view generation layer:

* `HtmlGraphWriter`

### `test`

Contains the computational graph model:

* `Topic`
* `Message`
* `Agent`
* `PlusAgent`
* `IncAgent`
* `Graph`
* `Node`
* `GenericConfig`
* `TopicManagerSingleton`

---

## SOLID Principles

### Single Responsibility Principle

Each class has a focused responsibility:

* `MyHTTPServer` manages the server, accepts client connections, and dispatches requests to the matching servlet.
* `RequestParser` parses an HTTP request into a structured request object.
* `HtmlLoader` loads static HTML files.
* `TopicDisplayer` publishes messages to topics and generates the topic values table.
* `ConfLoader` loads configuration files and creates the graph.
* `HtmlGraphWriter` generates the graph visualization.

### Open/Closed Principle

The server can be extended by adding new servlets without changing the server implementation.

New agents can also be added by implementing new agent classes and referencing them in configuration files.

### Dependency Injection

`HtmlLoader` receives the HTML folder name through its constructor:

```java
new HtmlLoader("html_files")
```

This makes the class independent of a fixed hard-coded path and allows the folder to be changed easily.

---

## Notes

The graph visualization displays topics as rectangles and agents as circles. When several agents have the same type, they may be displayed according to their agent type, while the calculations and topic values are still performed according to the loaded configuration.

---

## Submitters

Name: Michal Yifrach Shoob
ID: 211534954


Name: Ido Lublin
ID: 

