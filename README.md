### requirements
java 10
maven > 3.5

### Local Development
set the environment variable 

GOOGLE_APPLICATION_CREDENTIALS=$(project_directory)/src/main/resources/firebase-admin-sdk.json

go to the root directory of this repo, ie: something/COMP30022-IT-Project-Server
```androiddatabinding
mvn appgneine:run
```

You can see the local devserver at http://localhost:8080/

## Deploy to Google App Engine
ensure you have installed Google Cloud SDK with this tutorial:
https://cloud.google.com/sdk/docs/quickstarts

```
mvn appengine:deploy
```

### To see the log after deploy

```$xslt
gcloud app logs tail -s defaul
```

or Go to https://console.cloud.google.com/logs/viewer

select GAE Application

### Install the Google App Engine SDK for Java

```
gcloud components update app-engine-java
gcloud components update
```

