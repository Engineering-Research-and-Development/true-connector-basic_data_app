# Basic DataApp

[![License: AGPL](https://img.shields.io/github/license/Engineering-Research-and-Development/true-connector-basic_data_app.svg)](https://opensource.org/licenses/AGPL-3.0)

* Open-source project designed by ENG. It represents a trivial data application for generating and consuming data on top of the ECC component.

---


## Table of Contents

* [Building dataApp](#buildingdataapp)
  * [Requirements](#requirements)
  * [Solution 1](#solution1)
  * [Solution 2](#solution2)
  * [Creating docker image](#creatingdockerimage)
  * [Component overview](#componentoverview)
* [Dedicated endpoint in dataApp](#proxyendpoint)
* [Customizing DataApp](#customizingdataapp)
  * [Consumer side modification](#consumersidemodification)
  * [Provider side modification](#providersidemodification)
  * [Testing DataApp Provider endoint](#testingdataappproviderendoint)
* [WebSocket file exchange](#websocketfileexchange)
* [REST requests](#restrequests)
  * [Mixed](#mixed)
  * [Form](#form)
  * [Http-header](#httpheader)
* [Broker interaction](#brokerinteraction)
  * [Register/Update connector to Broker](#registeupdateconnectortobroker)
  * [Unregister/passivate connector to Broker](#unregisterpassivateconnectortobroker)
  * [Query broker](#querybroker)
* [Contract Negotiation - simple flow](#contractnegotiationsimpleflow)
* [Description Request/Response Message](#descriptionrequestresponsemessage)
* [Payload configuration](#payloadconfig)
  * [Base64 encoded payload](#base64encodedpayload)
  * [Extract payload from response](#extractpayloadfromresponse)

---


## Building dataApp <a name="buildingdataapp"></a>

**Requirements:** <a name="requirements"></a>

 `Java11` `Apache Maven`
 
To build dataApp you will have to do one of the following:

**Solution 1** <a name="solution1"></a>

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


**Solution 2** <a name="solution2"></a>

 * Clone [Multipart Message Library](https://github.com/Engineering-Research-and-Development/true-connector-multipart_message_library) 
 * Once this project is cloned, run `mvn clean install`
 * Clone [WebSocket Message Streamer](https://github.com/Engineering-Research-and-Development/true-connector-websocket_message_streamer)
 * Once this project is cloned, run `mvn clean install`

This will install 2 internal libraries that are needed by DataApp project.

After that you can run `mvn clean package` in the root of the dataApp project, to build it.


**NOTE:** If you proceed with Solution 2, pay attention to Multipart Message Library and WebSocket Message Streamer versions in pom.xml files, and check if the same versions are used in DataApp pom.xml, if not modify them according to ones from cloned repositories.

### Creating docker image <a name="creatingdockerimage"></a>

Once you build dataApp, if required, you can build docker image, by executing following command, from terminal, inside the root of the project:

```
docker build -t some_tag .
```

### Component overview <a name="componentoverview"></a>

Basic DataApp is build using Java11, and use following libraries:

| Component | Version |
| --- | --- |
| [Multipart Message Library](https://github.com/Engineering-Research-and-Development/true-connector-multipart_message_library) | 1.0.17 |
| [Websocket Message Streamer](https://github.com/Engineering-Research-and-Development/true-connector-websocket_message_streamer) | 1.0.17 |
| [Information model](https://github.com/International-Data-Spaces-Association/InformationModel) | 4.2.7 | 
| SpringBoot | 2.2.5.RELEASE |
| Tomcat | 9.0.27 |
| Maven | 3.6.3 |
| com.squareup.okhttp3 | 3.4.17 |
| apache.commons commons-text | 1.10.0 |  
| logback | 1.2.3 |
| com.h2database | 1.4.200 |
| javax.validation validation-api | 2.0.1 |
| org.apache.httpcomponents httpmime | 4.5.1 |
| com.googlecode.json-simple | 1.1.1 |
| com.googlecode.gson | 2.8.6 |
| org.jacoco | 0.8.8 |

---


## Dedicated endpoint in DataApp <a name="proxyendpoint"></a>

```
@RequestMapping("/proxy")
public ResponseEntity<?> proxyRequest(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody String body, HttpMethod method, HttpServletRequest request,
			HttpServletResponse response)
```
This methods is used in both REST and WSS flows.

---


## Customizing DataApp <a name="customizingdataapp"></a>

If you need to modify dataApp, you can perform such modification in 2 places: Consumer or Provider side.

### Consumer side modification <a name="consumersidemodification"></a>

Following class is used as entry point for consumer side:

[**it.eng.idsa.dataapp.web.rest.ProxyController**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/web/rest/ProxyController.java)

This class is the entry point of [proxy request](#proxyendpoint). Business logic for creating request is delegated to:

[**it.eng.idsa.dataapp.service.ProxyServiceImpl**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/service/impl/ProxyServiceImpl.java)

This class wraps up logic for creating proper IDS Message, and proper request, based on the configuration (mixed, form or header) and sends request to consumer ECC.

Once response is received, it will just log request. If you need to do something else with the response - check one of the following methods:

* **handleResponse(ResponseEntity<String> resp, MultipartMessage mm) (REST Flow)**
* **handleWssResponse(MultipartMessage mm) (WSS Flow)**

### Provider side modification <a name="providersidemodification"></a>

For making modification when dataApp is in provider role, one of the following controllers can be used as starting point

* [**it.eng.idsa.dataapp.web.rest.DataControllerBodyBinary (REST Flow)**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/web/rest/DataControllerBodyBinary.java) 

* [**it.eng.idsa.dataapp.web.rest.DataControllerBodyForm (REST Flow)**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/web/rest/DataControllerBodyForm.java)

* [**it.eng.idsa.dataapp.web.rest.DataControllerHttpHeader (REST Flow)**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/web/rest/DataControllerHttpHeader.java)

* [**it.eng.idsa.dataapp.web.rest.IncomingDataAppResourceOverWs (WSS Flow)**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/web/rest/IncomingDataAppResourceOverWs.java)


Depending on the configuration, REST(mixed, form, header) or Web Socket Flow.

This class (controller) is an entry point in Provider part of the dataApp, and it will receive request from Provider ECC.
Depending on the IDS Message received it will execute predefined logic in Message Handlers, and that should not be changed. Only modification should be made when ArtifactRequestMessage is received - when creating response payload. Code of interest can be found in following class:

 [**it.eng.idsa.dataapp.handler.ArtifactMessageHandler**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/handler/ArtifactMessageHandler.java)

The entry method in this class is:

**it.eng.idsa.dataapp.handler.ArtifactMessageHandler.handleMessage(Message message, Object payload)**

Based in the type of flow (REST or WSS) the next methods are point of interest:

* **it.eng.idsa.dataapp.handler.ArtifactMessageHandler.handleRestFlow(Message message) (REST flow)**

Current example has 2 payloads, one small - json representing "John Doe" and other - big payload, that has several hundred lines of text. For your use case, you can simplify it and just handle *if (requestHeader instanceof ArtifactRequestMessage)* use case. 

What is needed is to modify
**private String createResponsePayload()** method and provide logic that will fit your needs. You can add here part to read from some DB, call API, read file from filesystem, anything you need.

* **it.eng.idsa.dataapp.handler.ArtifactMessageHandler.handleWssFlow(Message message) (WSS flow)**

What is needed is to modify
**private String readFile(String requestedArtifact, Message message)** method and provide logic that will fit your needs. You can add here part to read from some DB, call API, read file from filesystem, anything you need.

Beside previously mentioned ArtifactMessageHandler, in total there are 4 messsage handlers: <a name="handlers"></a>

* [ArtifactMessageHandler](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/handler/ArtifactMessageHandler.java)
* [ContractAgreementMessageHandler](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/handler/ContractAgreementMessageHandler.java)
* [ContractRequestMessageHandler](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/handler/ContractRequestMessageHandler.java)
* [DescriptionRequestMessageHandler](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/handler/DescriptionRequestMessageHandler.java)

In case of adding new types of Message Handlers, the next steps should be taken:

1. Create new class which must extend [**DataAppMessageHandler**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/handler/DataAppMessageHandler.java)
2. Add new case for new handler in method **createMessageHandler(Class<? extends Message> clazz)** which can be found in [**MessageHandlerFactory**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/handler/MessageHandlerFactory.java)

When adding a new type of Message handler, advice is to use [**DataAppExceptionHandler**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/web/rest/exceptions/DataAppExceptionHandler.java) for exception handling, which handles all type of IDS Message Rejection Reasons.

### Testing DataApp Provider endoint <a name="testingdataappproviderendoint"></a>

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
  "ids:modelVersion" : "4.2.7",
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

---


## WebSocket file exchange <a name="websocketfileexchange"></a>

To use WSS flow on the egde and between ECC, do the following:

**Changes in DataApp**

In application.properties file:

```
application.dataLakeDirectory=
```
Use application.dataLakeDirectory= property to pint where files are located that needs to be exchanged over wss.

```
application.ecc.wss-port=8098
```
Use application.ecc.wss-port= property to set the ECC Sender WSS port.

In config.properties file

```
server.ssl.key-store=/home/user/etc/ssl-server.jks
```

Use server.ssl.key-store= property to provide full path to ssl-server.jks file stored locally, eg. `/home/user/etc/ssl-server.jks`


**Changes in ECC**

In ECCs application.properties file both in RECEIVER and SENDER to use Web Socket between ECC:

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
    "messageType": "ArtifactRequestMessage",
    "requestedArtifact" : "http://w3id.org/engrd/connector/artifact/test1.csv"
}'
```

---


## REST requests <a name="restrequests"></a>

### Mixed <a name="mixed"></a>

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

### Form <a name="form"></a>
 
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

### Http-header <a name="httpheader"></a>

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
Based on multipart type, and messageType, dataApp will create dedicated message in [Message handlers](#handlers), and send request to connector. 

```
application.ecc.protocol=https
application.ecc.host=localhost
application.ecc.port=8887
application.ecc.mix-context=/incoming-data-app/multipartMessageBodyBinary
application.ecc.form-context=/incoming-data-app/multipartMessageBodyFormData
application.ecc.header-context=/incoming-data-app/multipartMessageHttpHeader
```

Following properties are used to construct A-endpoint URL, http or https.

---


## Broker interaction <a name="brokerinteraction"></a>

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

### Register/Update connector to Broker <a name="registeupdateconnectortobroker"></a>

```
{
    "multipart": "http-header",
    "Forward-To": "https://broker.ids.isst.fraunhofer.de/infrastructure",
    "messageType":"ConnectorUpdateMessage",
}
```

### Unregister/passivate connector to Broker  <a name="unregisterpassivateconnectortobroker"></a>

```
{
    "multipart": "http-header",
    "Forward-To": "https://broker.ids.isst.fraunhofer.de/infrastructure",
    "messageType":"ConnectorUnavailableMessage",
}
```

### Query broker <a name="querybroker"></a>

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

---


## Contract Negotiation - simple flow <a name="contractnegotiationsimpleflow"></a>

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
User can also modify code in DataApp, to externalize decision for accepting or declining contract offers.

## Description Request/Response Message <a name="descriptionrequestresponsemessage"></a>

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

Remark: requestedElement field can be omitted. In that case, description response will contain whole self description document; otherwise it will contain just the part for requested element.

---


## Payload configuration <a name="payloadconfig"></a>

### Base64 encoded payload <a name="base64encodedpayload"></a>

If you want the demo response from Data App to be Base64 encoded then set the following property to true:

```
application.encodePayload=true
```

### Extract payload from response <a name="extractpayloadfromresponse"></a>

If you want the sender side Data App to extract only the payload from the received response set the following property to true:

```
application.extractPayloadFromResponse=true
```