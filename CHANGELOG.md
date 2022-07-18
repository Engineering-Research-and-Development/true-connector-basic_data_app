# Changelog
All notable changes to this project will be documented in this file.

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
 