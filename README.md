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
* [Security](#security)  
* [Firewall](#firewall)  
* [Dedicated endpoint in dataApp](#endpoint)
  * [Proxy Endpoint](#proxyendpoint)
  * [Data Endpoint](#dataendpoint)
  * [WebSocket Endpoint](#websocketendpoint)
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
* [CheckSum verification](#checksumverification)
* [Description Request/Response Message](#descriptionrequestresponsemessage)
* [Payload configuration](#payloadconfig)
  * [Base64 encoded payload](#base64encodedpayload)
  * [Extract payload from response](#extractpayloadfromresponse)
* [Code Coverage](#codecoverage)

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
| SpringBoot | 2.5.14 |
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
| sshd-core | 2.11.0 |
| sshd-sftp | 2.11.0 |
| sshd-scp | 2.11.0 |
| mina-core | 2.2.3 |



---

## Security <a name="security"></a>

Security in Basic DataApp is implemented via Spring Security mechanism. This framework is responsible for login user and also for response headers.

SpringSecurity:

```
.headers().xssProtection().and().contentTypeOptions().and().frameOptions().sameOrigin()
```

Example for the response headers are:

```
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
X-Frame-Options: SAMEORIGIN
```

Whole communication is done through TLS mode only, so all endpoint in DataApp are secure.
Since /proxy endpoint is exposed to the outside world, on separate port (default 8183) there is security requirement, so that only users with credentials can initiate request. Simple in memory user storage solution is implemented to address this requirement.

In the users.properties all user credentials are stored.

```
# List of users
users.list=idsUser,user2,user3

# Credentials for each user
idsUser.password=$2a$10$MQ5grDaIqDpBjMlG78PFduv.AMRe9cs0CNm/V4cgUubrqdGTFCH3m
user2.password=$ENCODED_PASSWORD2
user3.password=$ENCODED_PASSWORD3
```


In the example, the property user.list is a list with each item separated by a comma (,) without space. You need to enter all the users you want and then give each one a specific password, which must be BCrypt encoded, you can use the next [link](https://bcrypt-generator.com/) to get encoded value.


## Firewall <a name="firewall"></a>

DataApp allows setting up HttpFirewall through Spring Security. To turn it on/off, please take a look at following property: 

```
#Firewall
application.firewall.isEnabled=true
```

If Firewall is enabled, it will read properties defined in `firewall.properties` file which easily can be modified by needs of setup.

```
#Set which HTTP header names should be allowed (if you want to allow all header names, keep it empty)
allowedHeaderNames=
#Set which values in header names should have the exact value and allowed (if want to allow any values keep it empty)
allowedHeaderValues=
#Set which HTTP methods should be allowed (if you want to allow all methods, keep it empty)
allowedMethods=GET,POST
#Set if a backslash "\" or a URL encoded backslash "%5C" should be allowed in the path or not
allowBackSlash=true
#Set if a slash "/" that is URL encoded "%2F" should be allowed in the path or not
allowUrlEncodedSlash=true
#Set if double slash "//" that is URL encoded "%2F%2F" should be allowed in the path or not
allowUrlEncodedDoubleSlash=true
#Set if semicolon is allowed in the URL (i.e. matrix variables)
allowSemicolon=true
#Set if a percent "%" that is URL encoded "%25" should be allowed in the path or not
allowUrlEncodedPercent=true
#if a period "." that is URL encoded "%2E" should be allowed in the path or not
allowUrlEncodedPeriod=true
```
***IMPORTANT:*** If you're not an expert, the strong advice is to keep values at their default values. If you decide to change values, pay special attention to `allowHeaderNames` and `allowHeaderValues`, since those set values are exclusive and considered as only values that should be present in the header.

## Dedicated endpoint in DataApp <a name="endpoint"></a>

### Proxy Endpoint <a name="proxyendpoint"></a>

Following endpoint is exposed on separate port:

```
application.proxyPort=8183
```

Reason for this is that this port will be exposed via docker configuration to the outside world, while other port will not, and will be used only internally, between ECC and DataApp services. 

This endpoint also requires basic authorization when sending request. Please check [security](#security).

Logic used to filter requests can be found in
[**it.eng.idsa.dataapp.configuration.CustomWebMvcConfigurer**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/it/eng/idsa/dataapp/configuration/CustomWebMvcConfigurer.java)

```
@PostMapping("/proxy")
	public ResponseEntity<?> proxyRequest(@RequestHeader HttpHeaders httpHeaders, @RequestBody String body,
			HttpMethod method)
```
This methods is used in both REST and WSS flows.

---

### Data endpoint <a name="dataendpoint"></a>

Dedicated endpoint for receiving request from Execution Core Container. Intended to be used internally. Since it is used internally, between ECC and Data App, does not require authorization.

```
server.port=8083
```

### WebSocket endpoint <a name="websocketendpoint"></a>

[**it.eng.idsa.dataapp.web.rest.IncomingDataAppResourceOverWs**](https://github.com/Engineering-Research-and-Development/true-connector-basic_data_app/blob/master/src/main/java/eng/idsa/dataapp/web/rest/IncomingDataAppResourceOverWs.java)

This class listens on websocket port and once property is changed (message is received) it will recreate message and continue with message handling.


## Customizing DataApp <a name="customizingdataapp"></a>

If you need to modify dataApp, you can perform such modification in 2 places: Consumer or Provider side.

### Consumer side modification <a name="consumersidemodification"></a>

Following class is used as entry point for consumer side:

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
curl --location --request POST 'https://localhost:8183/data' \
--header 'Authorization: Basic aWRzVXNlcjpwYXNzd29yZA==' \
--form 'header={
  "@context" : {
    "ids" : "https://w3id.org/idsa/core/",
    "idsc" : "https://w3id.org/idsa/code/"
  },
  "@type" : "ids:ArtifactRequestMessage",
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

Using WebSocket configuration, you can exchange files that are read from dataLake property.

```
application.dataLakeDirectory=
```

One difference, is that if following property is set to true (default value):

```
application.contract.negotiation.demo=true
```

Then file will be read from dataLake, without checking if such file (resource) is defined in connector Self Description document.

If property is set to *false*, then user must define following resource in Self Description document (using connector Self Description API), since check will be made if such resource is present, like:

```
{
      "@type" : "ids:DataResource",
      "@id" : "https://w3id.org/idsa/autogen/dataResource/08f0feff-a142-4511-8334-1d66663c385f",
      "ids:contractOffer" : [ {
        "@type" : "ids:ContractOffer",
        "@id" : "https://w3id.org/idsa/autogen/contractOffer/dfced843-998c-41c1-809e-40050f7a1b9b",
        "ids:contractStart" : {
          "@value" : "2023-04-20T07:07:02.589Z",
          "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
        },
        "ids:contractDate" : {
          "@value" : "2023-04-20T07:07:03.897Z",
          "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
        },
        "ids:provider" : {
          "@id" : "https://w3id.org/engrd/connector/provider"
        },
        "ids:permission" : [ {
          "@type" : "ids:Permission",
          "@id" : "https://w3id.org/idsa/autogen/permission/5e1f4d1e-8dca-4780-b7ca-d4e6cca409a2",
          "ids:action" : [ {
            "@id" : "https://w3id.org/idsa/code/USE"
          } ],
          "ids:description" : [ {
            "@value" : "provide-access",
            "@type" : "http://www.w3.org/2001/XMLSchema#string"
          } ],
          "ids:title" : [ {
            "@value" : "Example Usage Policy",
            "@type" : "http://www.w3.org/2001/XMLSchema#string"
          } ],
          "ids:target" : {
            "@id" : "http://w3id.org/engrd/connector/artifact/test1.csv"
          }
        } ]
      } ],
      "ids:representation" : [ {
        "@type" : "ids:TextRepresentation",
        "@id" : "https://w3id.org/idsa/autogen/textRepresentation/82f3b21a-2b8d-44a0-841f-6df48c24d091",
        "ids:created" : {
          "@value" : "2023-04-20T07:07:03.990Z",
          "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
        },
        "ids:instance" : [ {
          "@type" : "ids:Artifact",
          "@id" : "http://w3id.org/engrd/connector/artifact/test1.csv",
          "ids:creationDate" : {
            "@value" : "2023-04-20T07:07:01.903Z",
            "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
          }
        } ],
        "ids:language" : {
          "@id" : "https://w3id.org/idsa/code/EN"
        }
      } ],
      "ids:keyword" : [ {
        "@value" : "Engineering Ingegneria Informatica SpA",
        "@type" : "http://www.w3.org/2001/XMLSchema#string"
      }, {
        "@value" : "TRUEConnector",
        "@type" : "http://www.w3.org/2001/XMLSchema#string"
      } ],
      "ids:modified" : {
        "@value" : "2023-04-20T07:07:03.798Z",
        "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
      },
      "ids:created" : {
        "@value" : "2023-04-20T07:07:03.798Z",
        "@type" : "http://www.w3.org/2001/XMLSchema#dateTimeStamp"
      },
      "ids:description" : [ {
        "@value" : "Used to verify wss flow",
        "@type" : "http://www.w3.org/2001/XMLSchema#string"
      } ],
      "ids:contentType" : {
        "@id" : "https://w3id.org/idsa/code/SCHEMA_DEFINITION"
      },
      "ids:version" : "1.0.0",
      "ids:title" : [ {
        "@value" : "CSV resource",
        "@type" : "http://www.w3.org/2001/XMLSchema#string"
      } ],
      "ids:language" : [ {
        "@id" : "https://w3id.org/idsa/code/EN"
      }, {
        "@id" : "https://w3id.org/idsa/code/IT"
      } ]
    }
```

To use WSS flow on the egde and between ECC, do the following:

**Changes in DataApp**

In `application.properties` file:

```
application.verifyCheckSum=true
```
If the size of files for transfer are larger than 30Mb, please also modify the next properties in `application.properties` file:


```
#SFTP settings
application.sftp.host=localhost
application.sftp.port=2222
application.sftp.connectorId=test
application.sftp.defaultTimeoutSeconds=100
```

In `config.properties` file

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
curl --location --request POST 'https://localhost:8183/proxy' \
--header 'Authorization: Basic aWRzVXNlcjpwYXNzd29yZA==' \
--data-raw '{
    "multipart": "wss",
    "Forward-To": "wss://localhost:8086",
    "Forward-To-Internal": "wss://localhost:8887",
    "messageType": "ArtifactRequestMessage",
    "requestedArtifact" : "http://w3id.org/engrd/connector/artifact/test1.csv",
    "payload": ""
}'
```

If the files that you want to transfer are larger than 30Mb, please put next content as payload:

```
"payload": {
        "sftp": true
    }
```
---


## REST requests <a name="restrequests"></a>

### Mixed <a name="mixed"></a>

```
curl --location --request POST 'https://localhost:8183/proxy' \
--header 'Authorization: Basic aWRzVXNlcjpwYXNzd29yZA==' \
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
curl --location --request POST 'https://localhost:8183/proxy' \
--header 'Authorization: Basic aWRzVXNlcjpwYXNzd29yZA==' \
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
curl --location --request POST 'https://localhost:8183/proxy' \
--header 'Authorization: Basic aWRzVXNlcjpwYXNzd29yZA==' \
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
curl --location --request POST 'https://localhost:8183/proxy' \
--header 'Authorization: Basic aWRzVXNlcjpwYXNzd29yZA==' \
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

For demo purposes, following property

```
application.contract.negotiation.demo=true

```

Can be left as is, but in production case, it should be set to false (which will send ProcessNotificationMessage upon receiving ContractRequestMessage, which will disable automatic acceptance of contract agreement.
User can also modify code in DataApp, to externalize decision for accepting or declining contract offers.


## CheckSum verification <a name="checksumverification"></a>

 DataApp will calculate checksum, using CRC32 algorithm, and compare calculated checksum with the one from ArtifactResponseMessage.
If received self-description from provider has a checkSum value, DataApp will verify that value with the current checkSum value in Artifact Response Message.

If the values are identical, payload will be consumed, if not, the rejection message will be thrown informing the user that file integrity has been broken.

If that requested element doesn't have a checksum in the self-description, DataApp will skip the verification even if turned on.

CheckSum verification can be configured in the following properties

```
application.verifyCheckSum=true

```

***IMPORTANT: *** CheckSum verification must be turned on in WSS flow.




## Description Request/Response Message <a name="descriptionrequestresponsemessage"></a>

When receiving a Description Request Message we are preparing a response by creating a Description Response Message for the header part and putting the whole Self Description(ids:BaseConnector) or requested element from Self Description (ids:Resource) in the payload.
In both cases a GET request is sent to the ECC in order to fetch the Self Description. The following properties need to be configured and correspond the Self Description configuration from the ECC.

Following properties are used to create URL for Self Description request from Data App to ECC:

```
application.ecc.host=localhost
application.ecc.selfdescription-port=8444
```

Example for Description RequestMessage:

```
curl --location --request POST 'https://localhost:8183/proxy' \
--header 'Authorization: Basic aWRzVXNlcjpwYXNzd29yZA==' \
--header 'Content-Type: text/plain' \
--data-raw '{
    "multipart": "form",
    "Forward-To": "https://ecc-provider:8086/data",
    "messageType":"DescriptionRequestMessage",
    "requestedElement" : "https://w3id.org/idsa/autogen/textResource/01ccac17-7889-4461-bd30-b3a5aa2242a8"
}'

```
You can enable validating received Self Description document. This feature is disabled by default but it can be changed by setting following property to true:

```
application.validateSelfDescription=true
```

If it is enabled, be sure to configure DAPS interaction, since validation will be done on PublicKey element of Self Description and that field is set from DAPS keystore file.

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


## Code coverage<a name="codecoverage"></a>

 

Code coverage is checked by using Jacoco plugin.

 
![DataApp Code Coverage](doc/jacoco.jpg?raw=true "ENG DataApp Code coverage")


For more up to date information about code coverage, you can check report after you build a project. Report can be found in

 

```
target\site\jacoco\index.html
```



