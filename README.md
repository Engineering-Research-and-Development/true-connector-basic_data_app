# market4.0-data_app_test_BE
[![License: AGPL](https://img.shields.io/github/license/Engineering-Research-and-Development/true-connector-basic_data_app.svg)](https://opensource.org/licenses/AGPL-3.0)

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
    "Forward-To": "https://localhost:8890/data",
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
    "Forward-To": "https://localhost:8890/data",
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
    "Forward-To": "https://localhost:8890/data",
	"messageAsHeaders": {
        "IDS-RequestedArtifact":"http://w3id.org/engrd/connector/artifact/1",
        "IDS-Messagetype":"ids:ArtifactRequestMessage",
        "IDS-ModelVersion":"4.0.0",
        "IDS-Issued":"2021-01-15T13:09:42.306Z",
        "IDS-Id":"https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f",
        "IDS-IssuerConnector":"http://w3id.org/engrd/connector/"
        },
	"payload" : {
		"catalog.offers.0.resourceEndpoints.path":"/pet2"
		}
}'

```
For <b>REST flow</b>, multipart field should be set to one of the following values: 'mixed', 'form' or 'http-header'.<br/>
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

# Contract Negotiation - simple flow

DataApp will send ContractAgreementMessage once *ids:ContractRequestMessage* message is received as input message.</br>
Payload for this reponse (ContractAgreement) will be read from file, named contract_agreement.json. This file is located in *dataLakeDirectory*. If
you wish to send different ContractAgreement, just modify content of this file.
This way, we are simulating simple contract negotiation sequence.

# Description Request/Response Message

When receiving a Description Request Message we are preparing a response by creating a Description Response Message for the header part and putting the whole Self Description(ids:BaseConnector) or requested element from Self Description (ids:Resource) in the payload.
In both cases a GET request is sent to the ECC in order to fetch the Self Description. The following properties need to be configured and correspond the Self Description configuration from the ECC.
```
application.ecc.RESTprotocol=http|https
application.ecc.RESTport=8081|8443
```