# Basic DataApp

[![License: AGPL](https://img.shields.io/github/license/Engineering-Research-and-Development/true-connector-basic_data_app.svg)](https://opensource.org/licenses/AGPL-3.0)

## Building dataApp

**Requirement:**

 `Java11` `Apache Maven`
 
To build dataApp you will have to do one of the following:

**Solution 1**

 * Clone [Multipart Message Library](https://github.com/Engineering-Research-and-Development/true-connector-multipart_message_library) 
 * Once this project is cloned, run mvn clean install
 * Clone [WebSocket Message Streamer](https://github.com/Engineering-Research-and-Development/true-connector-websocket_message_streamer)
 * Once this project is cloned, run mvn clean install

This will install 2 internal libraries that are needed by DataApp project.

After that you can run mvn clean package in the root of the dataApp project, to build it.

---

**Solution 2**

Use provided libraries on GitHub Package. To do so, you will have to modify Apache Maven settings.xml file like following:

Add in servers section:

```xml
<servers>
  <server>
    <id>github</id> 
    <username>some_username</username>
    <password>{your GitHub Personal Access Token}</password> 
  </server>
</servers>
```

How to get GH PAT, you can check following [link](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)

### Creating docker image

Once you build dataApp, if required, you can build docker image, by executing following command, from terminal, inside the root of the project:

```
docker build -t some_tag .
```

## Dedicated endpoint in dataApp <a name="proxyendpoint"></a>

```
@RequestMapping("/proxy")
public ResponseEntity<?> proxyRequest(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response)
```
This methods is used in both REST and WSS flows.


## Customizing DataApp

If you need to modify dataApp, you can perform such modification in 2 places: consumer side or provider.

### Consumer side modification

Following class is used as entry point for consumer side:

**it.eng.idsa.dataapp.web.rest.ProxyController**

This class is the entry point of [proxy request](#proxyendpoint). Business logic for creating request is delegated to:

**it.eng.idsa.dataapp.service.ProxyServiceImpl**

This class wraps up logic for creating proper IDS Message, and proper request, based on the configuration (mixed, form or header) and sends request to consumer ECC.

Once response is received, it will just log request. If you need to do something else with the response - check following method:

**handleResponse(ResponseEntity<String> resp, MultipartMessage mm)**

### Provider side modification

For making modification when dataApp is in provider role, one of the following controllers can be used as starting point

**it.eng.idsa.dataapp.web.rest.DataControllerBodyBinary**

**it.eng.idsa.dataapp.web.rest.DataControllerBodyForm**

**it.eng.idsa.dataapp.web.rest.DataControllerHttpHeader**

Depending on the configuration, mixed, form or header.

This class (controller) is an entry point in Provider part of the dataApp, and it will receive request from Provider ECC.
Depending on the IDS Message received it will execute predefined logic, and that should not be changed. Only modification should be made when ArtifactRequestMessage is received - when creating response payload. Code of interest can be found in following method:

**MessageUtil.it.eng.idsa.dataapp.util.createResponsePayload(Message requestHeader, String payload)**

```
else if (requestHeader instanceof ArtifactRequestMessage && isBigPayload(((ArtifactRequestMessage) requestHeader).getRequestedArtifact().toString())) {
	return encodePayload == true ? encodePayload(BigPayload.BIG_PAYLOAD.getBytes()) : BigPayload.BIG_PAYLOAD;
	}
	return  encodePayload == true ? encodePayload(createResponsePayload().getBytes()) : createResponsePayload();
			
```
Current example has 2 payloads, one small - json representing "John Doe" and other - big payload, that has several hundred lines of text. For your use case, you can simplify it and just handle *if (requestHeader instanceof ArtifactRequestMessage)* use case. 

What is needed is to modify
**private String createResponsePayload()** method and provide logic that will fit your needs. You can add here part to read from some DB, call API, read file from filesystem, anything you need.

### Testing DataApp Provider endoint

During development process, you can use following curl command (or import it in postman) to test custom logic you are working on. Provided example curl assumes you are using *form* configuration.

```
curl --location --request POST 'https://localhost:8083/data' \
--form 'header={
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:ArtifactRequestMessage\",
  "@id" : "https://w3id.org/idsa/autogen/artifactRequestMessage/a55ed1d5-576d-4a90-b7b2-2606d5a7905c",
  "ids:requestedArtifact" : {
    "@id" : "http://w3id.org/engrd/connector/artifact/1"
  },
  "ids:modelVersion" : "4.1.0",
  "ids:issued" : {
    "@value" : "2022-11-02T14:22:06.935Z",
    "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
  },
  "ids:issuerConnector" : {
    "@id" : "http://w3id.org/engrd/connector/consumer"
  },
  "ids:recipientConnector" : [ ],
  "ids:senderAgent" : {
    "@id" : "http://sender.agent/sender"
  },
  "ids:recipientAgent" : [ ],
  "ids:securityToken" : {
    "@type" : "ids:DynamicAttributeToken",
    "@id" : "https://w3id.org/idsa/autogen/dynamicAttributeToken/b49b7382-73a7-4838-a3d0-ad8d2ee2c5a4",
    "ids:tokenValue" : "DummyTokenValue",
    "ids:tokenFormat" : {
      "@id" : "https://w3id.org/idsa/code/JWT"
    }
  },
  "ids:transferContract" : {
    "@id" : "https://w3id.org/idsa/autogen/contractAgreement/155b87e3-5d4b-48ee-bcb2-24a1be346bbe"
  }
}' \
--form 'payload=ABC'

```

In payload you can provide any data that is needed for your backend system: DB query parameters, filter used in REST API call...

## WebSocket file exchange

To use wss flow on the egde and between ecc, do the following:

In dataApp property file:

```
application.dataLakeDirectory=
```
Use application.dataLakeDirectory= property to pint where files are located that needs to be exchanged over wss.

```
application.ecc.wss-port=8098
```
Use application.ecc.wss-port= property to set the ECC Sender WSS port.

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
    "transferContract": "https://w3id.org/idsa/autogen/contract/6a8e32ff-71bc-49e9-80b5-087126c0d7b0",
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
    "transferContract": "https://w3id.org/idsa/autogen/contract/6a8e32ff-71bc-49e9-80b5-087126c0d7b0",
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
    "transferContract": "https://w3id.org/idsa/autogen/contract/6a8e32ff-71bc-49e9-80b5-087126c0d7b0",
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
Payload for this response (ContractAgreement) will be fetch from Execution Core Container.

Following properties are used to create URL for Self Description request:

```
application.ecc.host=
application.ecc.RESTprotocol=
application.ecc.RESTport=
```

For demo purposes, following property

```
application.contract.negotiation.demo=true

```

Can be left as is, but in production case, it should be set to false (which will send ProcessNotificationMessage upon receiving ContractRequestMessage, which will disable automatic acceptance of contract agreement.
User can also modify code in dataApp, to externalize decision for accepting or declining contract offers.

# Broker interaction

For broker interaction, example requests are listed below:
You can choose different multiparts - mixed, form or http-header - this is how will DataApp send request to Execution Core Container.

Flow is following: dataApp will create request, and send it to Execution Core Container, on dedicated endpoints. Those endpoints are configured in property file:

```
application.ecc.broker-register-context=/selfRegistration/register
application.ecc.broker-update-context=/selfRegistration/update
application.ecc.broker-delete-context=/selfRegistration/delete
application.ecc.broker-passivate-context=/selfRegistration/passivate
application.ecc.broker-querry-context=/selfRegistration/query
```


NOTE: Broker might support (at the moment) only mixed/form, so double check how connector is configured to send request to destination B-endpoint.

## Register/Update connector to Broker

```
{
    "multipart": "http-header",
    "Forward-To": "https://broker.ids.isst.fraunhofer.de/infrastructure",
    "messageType":"ConnectorUpdateMessage",
}
```

## Unregister/passivate connector to Broker

```
{
    "multipart": "http-header",
    "Forward-To": "https://broker.ids.isst.fraunhofer.de/infrastructure",
    "messageType":"ConnectorUnavailableMessage",
}
```

## Query broker

Payload is used to pass query to the Broker

```
{
    "multipart": "http-header",
    "Forward-To": "https://broker.ids.isst.fraunhofer.de/infrastructure",
    "messageType":"QueryMessage",
	 "payload" : "SELECT ?connectorUri WHERE \{ ?connectorUri a ids:BaseConnector . \}"
}

```

Curl command:

```
curl --location --request POST 'https://localhost:8083/proxy' \
--header 'fizz: buzz' \
--header 'Content-Type: text/plain' \
--data-raw '{
    "multipart": "http-header",
    "Forward-To": "https://ecc-provider:8086/data",
    "Forward-To-Internal": "wss://ecc-consumer:8887",
    "messageType":"QueryMessage",
    "requestedArtifact": "http://w3id.org/engrd/connector/artifact/test1.csv",
    "transferContract": "https://w3id.org/idsa/autogen/contract/6a8e32ff-71bc-49e9-80b5-087126c0d7b0",
	"payload" : "SELECT ?connectorUri WHERE \{ ?connectorUri a ids:BaseConnector . \}"
}'
```
# Description Request/Response Message

When receiving a Description Request Message we are preparing a response by creating a Description Response Message for the header part and putting the whole Self Description(ids:BaseConnector) or requested element from Self Description (ids:Resource) in the payload.
In both cases a GET request is sent to the ECC in order to fetch the Self Description. The following properties need to be configured and correspond the Self Description configuration from the ECC.
```
application.ecc.RESTprotocol=http|https
application.ecc.RESTport=8081|8443
```


Example for Description RequestMessage:

```
curl --location --request POST 'https://localhost:8083/proxy' \
--header 'Content-Type: text/plain' \
--data-raw '{
    "multipart": "form",
    "Forward-To": "https://ecc-provider:8086/data",
    "messageType":"DescriptionRequestMessage",
    "requestedElement" : "https://w3id.org/idsa/autogen/textResource/01ccac17-7889-4461-bd30-b3a5aa2242a8"
}'

```

# Base64 encoded payload

If you want the demo response from Data App to be Base64 encoded then set the following property to true:

```
application.encodePayload=true
```

# Extract payload from response

If you want the sender side Data App to extract only the payload from the received response set the following property to true:

```
application.extractPayloadFromResponse=true
```

Remark: requestedElement field can be omitted. In that case, description response will contain whole self description document; otherwise it will contain just the part for requested element.
