# Changelog
All notable changes to this project will be documented in this file.

## [0.3.7] - 2023-11-16

### Changed

 - cosign version upgrade with alpine image; must include --insecure-ignore-tlog flag

## [0.3.6] - 2023-11-13

### Changed

 - Added cosign permission for folder (Dockerfile)

## [0.3.5] - 2023-10-30

### Changed

 - CertificationCheck now uses Cosign to verify if running version is signed

## [0.3.4] - 2023-10-13

### Changed

 - Made optional logic for validating self description document

## [0.3.3] - 2023-10-11

### Added
 - logic to validate SelfDescription in response flow

### Changed

 - Fix mapping in ProxyController
 
### Removed

 - Swagger UI
 
## [0.3.2] - 2023-10-03

### Added

 - HttpFirewall
 - firewall.properties file for managing Firewall
 - Property application.firewall.isEnabled for turning on HttpFirewal
 - Authentication Listeners for logging successful or failed login attempts
 - XSS protection in Spring Security
 
### Changed

 - Spring Boot version upgraded from 2.2.5.RELEASE to 2.5.14
 
 
## [0.3.1] - 2023-07-25

### Changed

 - Hardcoded to use TLS communication only
 - Property application.security.username deleted, instead the fixed value idsUser is used
 - Dockerfile improvements
 - SSL certificate update
 
## [0.3.0] - 2023-07-17

### Changed

 - Add Certification check
 - Add checkSum verification
 - Property application.verifyCheckSum required for checkSum verification

  
## [0.2.8] - 2023-06-20

### Changed

 - Fixed docker ci GitHub Action
 - Fixed check if requestedArtifact is blank when creating ArtifactRequestMessage

## [0.2.7] - 2023-06-19

### Removed

 - removed internal self description logic; now using default self description endpoint on ecc

## [0.2.6] - 2023-04-24

### Added

 - Property application.ecc.selfdescription-port required for getting the Self Description from ecc
 - Security basic Authorization for /proxy endpoint
 - New "proxy" port (application.proxyPort) and logic for filtering "proxy" vs "non proxy" requests based on port
 - when creating docker image, it requires GH tag to exists; bind java and docker versions

### Removed

 - Property application.ecc.wss-port no longer needed for WSS flow
 - Property application.ecc.selfdescription-context=/internal/sd ; now hard-coded
 
### Changed
 
 - improved logic when extracting and decoding payload in response flow; fixed Postman timeout


## [0.2.2] - 2023-04-06

### Added
 - Support for storing different type of objects in HashMap stored in ThreadLocal 
 - Support for GitHub Workflow (release process, docker image signing)
 - Property application.ecc.wss-port mandatory for WSS flow
 
 
### Changed
 - WSS flow now supports use of Message handlers
 - Distinguished REST and WSS flow in message handlers, with additional checks related to requested element
 - Cleaned unused code after refactoring
 - Multipart message library upgraded to 1.0.17
 - Websocket library upgraded to 1.0.17
 - Infomodel dependency now managed via Multipart Library


## [0.2.1-SNAPSHOT] - 2023-02-14

### Added

 - Jacoco plugin support for evaluating test code coverage in DataApp

## [0.2.0-SNAPSHOT] - 2023-02-09

### Added

 - Message factory and proper handlers for each type of IDS message (Artifact Message, Contract Request Message, Contract Agreement Message, and  Description Request Message) which are holding business logic for processing messages
 - HttpHeadersUtil - util class for handling things related to http-header conversion to IDS Messages
 - Global Exception handler and exceptions related to all types of IDS Rejection Reasons (BAD_PARAMETERS, INTERNAL_RECIPIENT_ERROR, MALFORMED_MESSAGE, MESSAGE_TYPE_NOT_SUPPORTED, METHOD_NOT_SUPPORTED, NOT_AUTHENTICATED, NOT_AUTHORIZED, NOT_FOUND, TEMPORARILY_NOT_AVAILABLE, TOO_MANY_RESULTS, VERSION_NOT_SUPPORTED)
 - SelfDescriptionService - service that holds business logic related to self-description
 - Non-privileged user and log support in Dockerfile
 - /home/nobody/data should be used as volume on consumer side to avoid permission errors when writing file to disk
 - Multipart message library upgrade from 1.0.14-SNAPSHOT to 1.0.15-SNAPSHOT (memory cleaner in MMP)
 - WebSocket Message Streamer library upgrade from 1.0.15-SNAPSHOT to 1.0.16-SNAPSHOT (memory cleaner in MMP)
 
### Changed

 - Business logic is moved from Message Utils to proper Message handlers, and messages are processed in different message handlers
 - Instead of returning null in different scenarios, and then building rejection messages, the proper exceptions are now thrown 
 - All Data Controllers are refactored according to the new logic
 - Java runtime from openjdk:11.0.15-jre to eclipse-temurin:11-jre-alpine in Dockerfile
 
## [0.1.14-SNAPSHOT] - 2023-02-01

### Changed

 - Multipart message library upgrade from 1.0.14-SNAPSHOT to 1.0.15-SNAPSHOT (memory cleaner in MMP)
 - WebSocket Message Streamer library upgrade from 1.0.15-SNAPSHOT to 1.0.16-SNAPSHOT (memory cleaner in MMP)

## [0.1.13-SNAPSHOT] - 2022-12-22

### Notes

 - version remains the same since nothing is done to the code

### Added

 - when pushing to master a new docker image will be created with version develop (rdlabengpa/ids_be_data_app:develop)

## [0.1.13-SNAPSHOT] - 2022-12-15

### Notes

 - make use of RestTemplateBuilder instead of RestTemplate to solve PKIX, please use ProxyServiceImpl constructor as reference

### Added

 - added property with path for new internal SelfDescription requests
 
```
application.ecc.selfdescription-context=/internal/sd
```
 
### Removed

 - removed properties used for fetching SelfDescription
 
```
application.ecc.RESTprotocol=
application.ecc.RESTport=
```
 

## [0.1.12-SNAPSHOT] - 2022-11-04

### Added

 - developer documentation for modifying DataApp, consumer and provider vise
 
### Changed

 - fixed transferContract field for WSS ArtifactRequestMessage flow


## [0.1.11-SNAPSHOT] - 2022-10-31

### Changed

 - banner is now packed with jar, to avoid need to manually change it in dockerized version 


## [0.1.10-SNAPSHOT] - 2022-09-27
 
### Added

 - added functionality to return only payload in response controlled with property application.extractPayloadFromResponse=
 
### Changed

 - if rejection message is received as response on sender side it is now processed and only rejection reason and status code are sent from data app (e.g. to Postman)

## [0.1.9-SNAPSHOT] - 2022-08-05
 
### Added

 - new big payload for response available at /big ("requestedArtifact": "http://w3id.org/engrd/connector/artifact/big")
 
### Changed

 - mixed and form responses are now passed through OutputStream to avoid org.apache.http.ContentTooLongException

## [0.1.8-SNAPSHOT] - 2022-07-18
 
### Added

 - new property to configure issuer connector, will be used in provider and consumer configuration
 - new property to configure usage control version - application.usageControlVersion
 - added 2 profile property files, so that dataApp can run in 2 instances, one for Consumer and other for Provider connector
 - reverted logic that reads policy from filesystem needed for MyData Usage Control
 
### Changed

 - updated logic for setting request and response messages with correct issuerConnector; in request flow - from property file, in response flow - from request message
 - changed the way to send back HttpEntity instead String representation of multipart response
 - when requested connector description, dataApp will fetch connector self description and send it back
 - in contract negotiation flow, dataApp will fetch connector self description and search it for requested element; also for contract agreement, it will get permission from connector self description document.

## [0.1.7-SNAPSHOT] - 2022-05-17
 
### Added
 - added transfer contract optional field in proxy request; this will be used in contract negotiation sequence
 - Created logic to fetch self description from connector and search contract offer for permission and target and create contract agreement
 - new property to configure default acceptance for contract offer - application.contract.negotiation.demo
 
## [0.1.6-SNAPSHOT] - 2021-12-02
 
### Added
 - added logic to Base64 encode and decode payload
 - new MMP library - removed extra line in payload part
 
## [0.1.5-SNAPSHOT] - 2021-11-16
 
### Added
 - now supporting file sending over multipart/form

## [0.1.4-SNAPSHOT] - 2021-11-05
 
### Changed
 - improved docker creation
 
## [0.1.3-SNAPSHOT] - 2021-11-02
 
### Changed
 - moved logic from MultiPartMessageService to MultipartMessageProcessor
 
### Removed
 - removed MultiPartMessageService

## [0.1.2-SNAPSHOT] - 2021-10-15
 
### Changed
 - minor exception and exception handling changes

## [0.1.1-SNAPSHOT] - 2021-10-08
 
### Changed
 - TestUtilMessageService now everywhere replaced with UtilMessageService

## [0.1.0-SNAPSHOT] - 2021-09-16
 
### Changed
 - infomodel version has been changed to 4.1.1
 - data app proxy functionality (see [README.md](/README.md))

## [0.0.8-SNAPSHOT] - 2021-07-09

### Added
 - DescriptionRequestMessage handling
 - Rejection message for ArtifactRequestMessage if some custom criteria are not met (only for demonstration purposes)

### Changed

## [0.0.7-SNAPSHOT] - 2021-07-08

### Added

### Changed
 - Increased version for multipart message processor library to 1.0.9-SHAPSHOT
 - Increased version for websocket message streamer library to 1.0.11-SHAPSHOT
 
## [0.0.6-SNAPSHOT] - 2021-07-02

### Added
 - fix for handling proxy payload - String and JSON support

### Changed
 
