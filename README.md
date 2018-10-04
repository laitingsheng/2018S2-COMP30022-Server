### requirements
java 10
maven > 3.5

### Local Development
go to the root directory of this repo, ie: something/COMP30022-IT-Project-Server
maven spring-boot:run

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

### Install the Google App Engine SDK for Java

```
gcloud components update app-engine-java
gcloud components update
```

