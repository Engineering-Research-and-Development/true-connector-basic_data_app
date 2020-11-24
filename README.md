# market4.0-data_app_test_BE


## WebSocket file exchange

To use wss flow on the egde and between ecc, do the following:

In dataApp property file:

```
application.websocket.isEnabled=true
application.dataLakeDirectory=
```
Use application.dataLakeDirectory= property to pint where files are located that needs to be exchanged over wss.

In ECC property file:

```
application.openDataAppReceiver=https://localhost:8083/incoming-data-app/routerBodyBinary
application.openDataAppReceiverRouter=mixed
application.eccHttpSendRouter=mixed

# Enable WebSocket over Https -> Disable Idscp to use!
application.websocket.isEnabled=true
#Enable WS communication channel from/to DataAPP
application.dataApp.websocket.isEnabled=true

application.idscp.isEnabled=false

```

There is dedicated endpoint in dataApp

```
@PostMapping("/artifactRequestMessage")
@ResponseBody
public String requestArtifact(@RequestHeader("Forward-To-Internal") String forwardToInternal,
			@RequestHeader("Forward-To") String forwardTo, @RequestParam String requestedArtifact,
			@Nullable @RequestBody String payload)
```
That handles incomming requests, creates ArtifactRequestMessage, with requestedArtifact property set to requested artifact query param in following pattern

```
"http://mdm-connector.ids.isst.fraunhofer.de/artifact/" + requestedArtifact
```

This URL will be used on Provider data app, to read resource from file system (file name), create ArifactResponseMessage with payload base64 encoded file content, and send response over wss back to Data Consumer.


In postman, use following configuration:

![WSS Postman configuration](doc/postman_wss.JPG?raw=true "How to configure postman to wxchange files over wss")

**Forward-To-Internal** - this property will make wss connection on A-endpoint on Sender; we use 8887 port, same like for http communication, since in wss config, it will be disabled.

**Forward-To** - regular forward to usage - forwarding to receiver ECC, using application.idscp.server.port value
