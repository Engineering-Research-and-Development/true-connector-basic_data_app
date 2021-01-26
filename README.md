# market4.0-data_app_test_BE


## WebSocket file exchange

To use wss flow on the egde and between ecc, do the following:

In dataApp property file:

```
application.dataLakeDirectory=
```
Use application.dataLakeDirectory= property to pint where files are located that needs to be exchanged over wss.

In ECC property file:

```
application.openDataAppReceiver=https://localhost:9000/incoming-data-app/routerBodyBinary
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
@RequestMapping("/proxy")
public ResponseEntity<?> proxyRequest(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response)
```
This methods is used in both REST and WSS flows.

Following CURL command matches the request

```
curl --location --request POST 'https://localhost:8083/proxy' \
--header 'Forward-To: https://localhost:8890/data' \
--header 'Forward-To-Internal: wss://localhost:8887' \
--header 'fizz: buzz' \
--header 'Content-Type: text/plain' \
--data-raw '{
    "multipart": "mixed",
    "requestedArtifact" : "test1.csv",
	 "message": {
	  "@context" : {
		"ids" : "https://w3id.org/idsa/core/"
	  },
	  "@type" : "ids:ArtifactRequestMessage",
	  "@id" : "https://w3id.org/idsa/autogen/artifactRequestMessage/76481a41-8117-4c79-bdf4-9903ef8f825a",
	  "ids:issued" : {
		"@value" : "2020-11-25T16:43:27.051+01:00",
		"@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
	  },
	  "ids:modelVersion" : "4.0.0",
	  "ids:issuerConnector" : {
		"@id" : "http://w3id.org/engrd/connector/"
	  },
	  "ids:requestedArtifact" : {
	   "@id" : "http://w3id.org/engrd/connector/artifact/1"
	  }
	},
	"payload" : {
		"catalog.offers.0.resourceEndpoints.path":"/pet2"
		},
    "messageAsHeaders": {
        "IDS-RequestedArtifact":"http://w3id.org/engrd/connector/artifact/1",
        "IDS-Messagetype":"ids:ArtifactRequestMessage",
        "IDS-ModelVersion":"4.0.0",
        "IDS-Issued":"2021-01-15T13:09:42.306Z",
        "IDS-Id":"https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f",
        "IDS-IssuerConnector":"http://w3id.org/engrd/connector/"
        }
}'
```


For <b>REST flow</b>, multipart field should be set to one of the following values: 'mixed', 'form' or 'http-header'.
In case of mixed or form flow, 'message' and 'payload' parts are used to construct request and forward to ECC connector A-endpoint using Forward-To header value.<br />
In case of http-header flow, messageAsHeaders and payload are used to construct http-header like request and forward it to ECC A-endpoint.

Configuration regarding A-endpoint for data consumer is located in proeprty file:

```
application.ecc.protocol=https
application.ecc.host=localhost
application.ecc.port=8887
application.ecc.mix-context=/incoming-data-app/multipartMessageBodyBinary
application.ecc.form-context=/incoming-data-app/multipartMessageBodyFormData
application.ecc.header-context=/incoming-data-app/multipartMessageHttpHeader
```

Following properties are used to construct A-endpoint URL, http or https.

For <b>WSS flow</b>, multipart should be set to 'wss', and requestedArtifact should have value of the file/artifact that is requested over wss.

Headers required:<br />
**Forward-To-Internal** - this property will make wss connection on A-endpoint on Sender; we use 8887 port, same like for http communication, since in wss config, it will be disabled.<br />
wss://localhost:8887

**Forward-To** - regular forward to usage - forwarding to receiver ECC, using application.idscp.server.port value<br />
wss://localhost:8086

