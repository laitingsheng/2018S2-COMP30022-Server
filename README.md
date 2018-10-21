
# Description
This is the Backend Server repo that provide restful API service for APP GUGUGU, which is the product for COMP30022 
IT Project. The server is hosted on Google Cloud Platform

This project use Spring Boot framework, with a mixture of Java and Kotlin in the controller. 

See File Structure section for more information about the files. 

# Prerequisite
1. java 10 or above
2. maven 3.5 or above
3. Google App Engine SDK installed
4. Google Cloud SDK installed
5. Kotlin dependency will be automaticallly installed by maven

## Install the Google App Engine SDK for Java

```
gcloud components update app-engine-java
gcloud components update
```

## install Google CLoud SDK
ensure you have installed Google Cloud SDK with this tutorial:
https://cloud.google.com/sdk/docs/quickstarts

# run the server
For both local development and Google APP Engine

set the environment variable 

GOOGLE_APPLICATION_CREDENTIALS=$(project_directory)/src/main/resources/firebase-admin-sdk.json

## Local Development
go to the root directory of this repo, ie: something/COMP30022-IT-Project-Server
```androiddatabinding
mvn appgneine:run
```

You can see the local devserver at http://localhost:8080/

## Deploy to Google App Engine

```
mvn appengine:deploy
```
The gloud address will be showed in the log. 

For our project, it is:

http://comp30022-it-project.appspot.com/

# After deploy
## To see the log after deploy

```$xslt
gcloud app logs tail -s defaul
```

or Go to https://console.cloud.google.com/logs/viewer

select GAE Application

# File Structure
To see source code, go to src/main/kotlin/comp30022/server

To see test code, go to src/test/kotlin/comp30022/server

Detailed FireStructure
```
src
├── main
│   ├── kotlin
│   │   └── comp30022
│   │       └── server
│   │           ├── Constant.java: these are constant that will be used by the program.
│   │           ├── Server.kt: the main controller for our RESTful API
│   │           ├── exception
│   │           │   ├── DbException.java 
│   │           │   └── NoGrouptoJoinException.java
│   │           ├── firebase
│   │           │   └── FirebaseDb.java:  the encapulated class for Firebase Opeartion. 
|   |           |                          (Deprecated, as firebase api itself is easy to use enough
│   │           ├── grouping: Grouping module to handle our Grouping Feature
│   │           │   ├── Group.java 
│   │           │   └── GroupAdmin.java
│   │           ├── routeplanning: Navigation moduel that suppor tour navigation feature
│   │           │   ├── RouteHash.java
│   │           │   └── RoutePlanner.java
│   │           ├── twilio.kt: All the constant that will be use by twillo module
│   │           └── util
│   │               ├── Converter.java
│   │               └── GeoHashing.java
│   ├── resources: These are non-code error we need for backend
│   │   ├── application.properties
│   │   ├── firebase-admin-sdk.json
│   │   ├── static
│   │   └── templates
│   └── webapp: contains the file for deployment on Google Cloud Platform
│       └── WEB-INF
│           └── appengine-web.xml
└── test: all test for server that will run before server launched. 
    └── kotlin
        └── comp30022
            └── server
                ├── GroupAdminTest.java: test for group control
                ├── RoutePackageTests.java: test for route
                └── UtilTests.java: test for all functions under src/main/kotlin/comp30022/server/util
```

# Author
1. Siyi Guo
2. Tingsheng Lai


