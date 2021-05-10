# market4.0-data_app_test_BE


## Dedicated endpoint in dataApp

```
@RequestMapping("/proxy")
public ResponseEntity<?> proxyRequest(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response)
```
This methods is used in both REST and WSS flows.


## WebSocket file exchange

To use wss flow on the egde and between ecc, do the following:

In dataApp property file:

```
application.dataLakeDirectory=
```
Use application.dataLakeDirectory= property to pint where files are located that needs to be exchanged over wss.

In ECC property file to use Web Socket between ECC:

```
application.openDataAppReceiver=https://localhost:9000/incoming-data-app/routerBodyBinary
application.openDataAppReceiverRouter=mixed
application.eccHttpSendRouter=mixed

# Enable WebSocket over Https -> Disable Idscp2 to use!
application.websocket.isEnabled=true
#Enable WS communication channel from/to DataAPP
application.dataApp.websocket.isEnabled=true

application.idscp2.isEnabled=false

```

In ECC property file to use IDSCP2 between ECC:

```
application.openDataAppReceiver=https://localhost:9000/incoming-data-app/routerBodyBinary
application.openDataAppReceiverRouter=mixed
application.eccHttpSendRouter=mixed

application.websocket.isEnabled=false
#Enable WS communication channel from/to DataAPP
application.dataApp.websocket.isEnabled=true

application.idscp2.isEnabled=true

```

For <b>WSS flow</b>, multipart should be set to 'wss', and requestedArtifact should have value of the file/artifact that is requested over wss.

**Forward-To-Internal** - this property will make wss connection on A-endpoint on Sender; we use 8887 port, same like for http communication, since in wss config, it will be disabled.<br />
wss://localhost:8887

**Forward-To** - regular forward to usage - forwarding to receiver ECC, using application.idscp.server.port value<br />
wss://localhost:8086

```
curl --location --request POST 'https://localhost:8083/proxy' \
--data-raw '{
    "multipart": "wss",
    "Forward-To": "wss://localhost:8086",
    "Forward-To-Internal": "wss://localhost:8887",
    "requestedArtifact" : "test1.csv"
}'
```
## REST requests

### Mixed

```
curl --location --request POST 'https://localhost:8083/proxy' \
--header 'fizz: buzz' \
--header 'Content-Type: text/plain' \
--data-raw '{
    "multipart": "mixed",
    "Forward-To": "https://localhost:8887/data",
    "messageType":"ArtifactRequestMessage",
    "requestedArtifact": "http://w3id.org/engrd/connector/artifact/test1.csv",
	 "payload" : {
		"catalog.offers.0.resourceEndpoints.path":"/pet2"
    }
}'

```

### Form

```
curl --location --request POST 'https://localhost:8083/proxy' \
--header 'fizz: buzz' \
--header 'Content-Type: text/plain' \
--data-raw '{
    "multipart": "form",
    "Forward-To": "https://localhost:8887/data",
    "messageType":"ArtifactRequestMessage",
    "requestedArtifact": "http://w3id.org/engrd/connector/artifact/test1.csv",
	 "payload" : {
		"catalog.offers.0.resourceEndpoints.path":"/pet2"
    }
}'

```

### Http-header

```
curl --location --request POST 'https://localhost:8083/proxy' \
--header 'fizz: buzz' \
--header 'Content-Type: text/plain' \
--data-raw '{
    "multipart": "http-header",
    "Forward-To": "https://localhost:8887/data",
    "messageType":"ArtifactRequestMessage",
    "requestedArtifact": "http://w3id.org/engrd/connector/artifact/test1.csv",
	 "payload" : {
		"catalog.offers.0.resourceEndpoints.path":"/pet2"
    }
}'

```
For <b>REST flow</b>, multipart field should be set to one of the following values: 'mixed', 'form' or 'http-header'.<br/>
Based on multipart type, and messageType, dataApp will create dedicated message, and send request to connector. At the moment, 2 messages are supported - 'ArtifactRequestMessage' and 'ContractAgreementMessage'.
Configuration regarding A-endpoint for data consumer is located in property file:

```
application.ecc.protocol=https
application.ecc.host=localhost
application.ecc.port=8887
application.ecc.mix-context=/incoming-data-app/multipartMessageBodyBinary
application.ecc.form-context=/incoming-data-app/multipartMessageBodyFormData
application.ecc.header-context=/incoming-data-app/multipartMessageHttpHeader
```

Following properties are used to construct A-endpoint URL, http or https.

# Contract Negotiation - simple flow

DataApp will send ContractAgreementMessage once *ids:ContractRequestMessage* message is received as input message.</br>
Payload for this reponse (ContractAgreement) will be read from file, named contract_agreement.json. This file is located in *dataLakeDirectory*. If
you wish to send different ContractAgreement, just modify content of this file.
This way, we are simulating simple contract negotiation sequence.

