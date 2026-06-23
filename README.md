# Advanced Programming Final Project

## Project Overview

This project was developed as part of the **Advanced Programming** course.

The project implements a local HTTP server and a browser-based dashboard for loading, running, and visualizing a computational graph.

The system allows the user to upload a configuration file, generate a computational graph, send messages to specific Topics, and view the updated Topic values in real time.

The project combines backend development, HTTP communication, configuration parsing, graph-based computation, servlets, and a dynamic HTML view.

---

## Main Features

* Local HTTP server implemented in Java
* Support for `GET` and `POST` requests
* Servlet-based architecture
* Uploading configuration files from the browser
* Building a computational graph from the uploaded configuration
* Dynamic graph visualization
* Publishing messages to specific Topics
* Displaying current Topic values in a table
* Separation between server logic, servlets, graph logic, configuration logic, and view generation
* Example configuration files for testing different agents
* Javadoc documentation for API usage

---

## Project Structure

```text
project/
│
├── src/
│   │
│   ├── Main/
│   │   ├── Main.java
│   │   ├── TestServer.java
│   │   └── package-info.java
│   │
│   ├── server/
│   │   ├── HTTPServer.java
│   │   ├── MyHTTPServer.java
│   │   └── RequestParser.java
│   │
│   ├── servlets/
│   │   ├── Servlet.java
│   │   ├── HtmlLoader.java
│   │   ├── ConfLoader.java
│   │   └── TopicDisplayer.java
│   │
│   ├── test/
│   │   ├── Agent.java
│   │   ├── BinOpAgent.java
│   │   ├── Config.java
│   │   ├── DivAgent.java
│   │   ├── GenericConfig.java
│   │   ├── Graph.java
│   │   ├── IncAgent.java
│   │   ├── MainTrain.java
│   │   ├── MainTrain2.java
│   │   ├── Message.java
│   │   ├── MulAgent.java
│   │   ├── Node.java
│   │   ├── ParallelAgent.java
│   │   ├── PlusAgent.java
│   │   ├── PowerAgent.java
│   │   ├── SqrtAgent.java
│   │   ├── Topic.java
│   │   └── TopicManagerSingleton.java
│   │
│   ├── views/
│   │   ├── HtmlGraphWriter.java
│   │   └── package-info.java
│   │
│   └── module-info.java
│
├── config_files/
│   ├── big_example.conf
│   ├── test_div.conf
│   ├── test_inc.conf
│   ├── test_mul.conf
│   ├── test_plus.conf
│   ├── test_power.conf
│   ├── test_sqrt.conf
│   └── uploaded.conf
│
├── html_files/
│   ├── form.html
│   ├── index.html
│   └── temp.html
│
├── docs/
│   └── Javadoc documentation
│
├── link.txt
│
└── README.md
```

---

## Package Description

* `Main` — contains the main entry point of the project. The `Main.java` file starts the HTTP server, registers the servlets, and keeps the server running until the user stops it.

* `server` — contains the HTTP server infrastructure:

  * `HTTPServer.java` — server interface
  * `MyHTTPServer.java` — HTTP server implementation
  * `RequestParser.java` — parses HTTP requests

* `servlets` — contains the servlet implementations used by the server:

  * `HtmlLoader.java` — loads static HTML files from the `html_files` folder
  * `ConfLoader.java` — handles configuration file upload and graph creation
  * `TopicDisplayer.java` — handles publishing messages to Topics and displaying updated Topic values
  * `Servlet.java` — servlet interface

* `test` — contains the computational graph logic, Topics, Messages, Agents, configuration classes, and example main classes.

* `views` — contains the view-generation logic. The `HtmlGraphWriter` class dynamically creates the HTML representation of the computational graph.

* `config_files` — contains example configuration files that can be uploaded through the dashboard.

* `html_files` — contains the static HTML files used by the browser dashboard.

* `docs` — contains the generated Javadoc documentation for the project.

---

## Technologies Used

* Java
* HTTP Server
* Servlets
* HTML
* CSS
* JavaScript
* Javadoc
* Git / GitHub

---

## Installation

1. Clone the repository:

```bash
git clone https://github.com/michal-shoob/AdvancedProgramming
```

2. Open the project in **Eclipse** or **VS Code**.

3. Make sure Java is installed on your computer.

4. Make sure the project uses Java 17 or a compatible Java version.

5. Compile the project.

---

## How to Run the Project

1. Run the `Main.java` file located in:

```text
src/Main/Main.java
```

The program starts a local HTTP server on port `8080`.

Example from the main flow:

```java
HTTPServer server = new MyHTTPServer(8080, 5);

server.addServlet("GET", "/publish", new TopicDisplayer());
server.addServlet("POST", "/upload", new ConfLoader());
server.addServlet("GET", "/app/", new HtmlLoader("html_files"));

server.start();
```

2. After running `Main.java`, open Google Chrome or any web browser.

3. Enter the following address:

```text
http://localhost:8080/app/index.html
```

4. The dashboard page should open in the browser.

---

## How to Use the Dashboard

### 1. Upload a Configuration File

On the left side of the page, there is an upload form.

Choose one of the example configuration files from the `config_files` folder.

Examples:

```text
config_files/test_plus.conf
config_files/test_mul.conf
config_files/test_div.conf
config_files/test_power.conf
config_files/test_sqrt.conf
config_files/big_example.conf
```

After choosing a configuration file, click:

```text
Deploy
```

The system loads the configuration file, creates the computational graph, and displays the graph in the center of the page.

---

### 2. Publish a Message to a Topic

Use the second form on the left side of the page.

Enter the Topic name and the message value.

Example:

```text
Topic name: A
Message: 5
```

Then click:

```text
Send
```

The message is published to the selected Topic.

---

### 3. View the Updated Topic Values

After sending a message, the values table is updated.

The table displays the current values of the Topics.

Each row contains:

* Topic name
* Last value of the Topic

This allows the user to check how the data flows through the computational graph.

---

## Main System Flow

The main flow of the system is:

```text
Run Main.java
        ↓
Open http://localhost:8080/app/index.html
        ↓
Upload configuration file
        ↓
Click Deploy
        ↓
Display computational graph
        ↓
Enter Topic name and Message value
        ↓
Click Send
        ↓
Display updated Topic values
```

---

## Servlets

The project uses three main servlet implementations.

### HtmlLoader

`HtmlLoader` is responsible for loading static HTML files from the `html_files` folder.

It handles requests that start with:

```text
/app/
```

Example:

```text
http://localhost:8080/app/index.html
```

If the requested file exists, it returns the HTML content.

If the file does not exist, it returns an HTML response indicating that the file was not found.

---

### ConfLoader

`ConfLoader` is responsible for handling uploaded configuration files.

It handles requests to:

```text
/upload
```

Its responsibilities are:

* Extract the uploaded file name and content
* Save the uploaded content on the server side
* Load the configuration using `GenericConfig`
* Create a `Graph` object from the configuration
* Return an HTML page that displays the computational graph

---

### TopicDisplayer

`TopicDisplayer` is responsible for publishing messages to Topics and displaying their current values.

It handles requests to:

```text
/publish
```

Its responsibilities are:

* Extract the Topic name from the HTTP request
* Extract the message value from the HTTP request
* Publish the message to the relevant Topic using the Topic Manager
* Return an HTML table with the updated Topic values

---

## View Layer

The view layer is responsible for displaying the system visually in the browser.

The static HTML files are located in the `html_files` folder:

* `index.html` — the main dashboard page
* `form.html` — contains the forms for uploading configuration files and sending messages to Topics
* `temp.html` — an empty temporary page used as the default content before a graph or table is displayed

The dashboard is divided into three main parts:

1. Left side — forms for uploading configuration files and sending messages
2. Center — graphical display of the computational graph
3. Right side — table of current Topic values

The class `HtmlGraphWriter` is responsible for dynamically generating the HTML representation of the computational graph after a configuration file is uploaded.

The graph visualization displays:

* Topics as rectangles
* Agents as circles
* Directed arrows according to the computational graph
* Different colors for Topics and Agents
* Names inside the graph nodes

---

## Example Configuration Files

The project includes several example configuration files in the `config_files` folder.

These files can be used to test different computational graphs and different agents.

Examples:

* `test_plus.conf` — tests addition logic
* `test_mul.conf` — tests multiplication logic
* `test_div.conf` — tests division logic
* `test_inc.conf` — tests increment logic
* `test_power.conf` — tests power calculation logic
* `test_sqrt.conf` — tests square-root calculation logic
* `big_example.conf` — demonstrates a larger computational graph
* `uploaded.conf` — stores an uploaded configuration file

---

## Demo Video

Demo video link:

```text
https://drive.google.com/file/d/1wCcMZfrmQDqlCpGtGZyJlWNxhCkIZ5Xy/view?usp=sharing
```

The demo video demonstrates the main features of the project:

* Opening slide with course details and submitter details
* Background explanation
* Project design explanation
* Running `Main.java`
* Opening the dashboard in the browser
* Uploading a configuration file from the `config_files` folder
* Clicking `Deploy`
* Displaying the computational graph
* Publishing messages to Topics
* Displaying updated Topic values
* Features added beyond the minimum requirements
* Summary of what we learned from the course

---

## Javadoc

The project includes full Javadoc documentation for the main API.

The Javadoc documentation is located in the `docs` folder.

To open the documentation, open:

```text
docs/index.html
```

The Javadoc is intended to help another developer understand how to reuse the HTTP server and the project API correctly.

---

## What We Learned

During the project, we practiced and improved the following skills:

* Building a local HTTP server in Java
* Working with HTTP requests and responses
* Designing a servlet-based architecture
* Separating responsibilities between packages and classes
* Loading and parsing configuration files
* Building and displaying computational graphs
* Connecting backend logic with a browser-based interface
* Dynamically generating HTML from Java code
* Writing reusable code
* Creating Javadoc documentation
* Working with Git and GitHub
* Presenting a software project clearly through a demo video

---

## Submitters

### Submitter 1

Name: Michal Yifrach Shoob
ID: 211534953
Email: Michalshoob44@gmail.com

### Submitter 2

Name: Ido Lublin 
ID: 322715020
Email: ido.lublin@gmail.com

---

## Notes

This project was built to demonstrate a complete software system that combines backend development, HTTP communication, graph-based computation, and a dynamic browser-based user interface.

The final submission is checked manually through the Git repository.

The file submitted to the course system is `link.txt`, which contains the Git repository link and the submitters' details.
