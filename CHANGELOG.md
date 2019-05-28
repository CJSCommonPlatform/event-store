# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]
## [2.0.0-M29] - 2019-05-28
### Fixed
- Re-release of 2.0.0-M28 as deployment to Bintray.com failed due to network issues

## [2.0.0-M28] - 2019-05-24
### Added
- Added an Observer for PublishedEvent Rebuild

## [2.0.0-M27] - 2019-05-22
### Fixed
- Removed rogue test library from production scope in maven 

## [2.0.0-M26] - 2019-05-22
### Changed
- Pre-publish and publish timer beans run for a given timer max runtime. The max time can be set with JNDI values "pre.publish.timer.max.runtime.milliseconds" and "event.dequer.timer.max.runtime.milliseconds".
- framework -> 6.0.0-M26
- framework-api -> 4.0.0-M21

## [2.0.0-M25] - 2019-05-15
### Changed
- Merged event-publisher modules into one
- Moved shuttering from the publishing timer bean into the command api in framework

## [2.0.0-M24] - 2019-05-07
### Changed
- framework -> 6.0.0-M23
- framework-api -> 4.0.0-M19

## [2.0.0-M22] - 2019-05-07
### Changed
- Update utilities to 1.18.0
- Update test-utils to 1.23.0
- Update framework to 6.0.0-M22
- Update common-bom to 2.0.2
- Update framework-api to 4.0.0-M18
- Event-Buffer functionality with changes to StreamBuffer & StreamStatus tables to support EventIndexer

### Added
- Added event-publisher-timer to event-store-bom dependency management

## [2.0.0-M19] - 2019-04-30
### Fixed
- Fixed liquibase scripts that fail if the table linked_event already exists 

## [2.0.0-M18] - 2019-04-29
### Changed
- Removed the join to stream_status when inserting an event to event_log
- PrePublishBean now limits the maximum number of events published per run of the timer bean.

## [2.0.0-M17] - 2019-04-25
### Changed
- Removed deprecated code

## [2.0.0-M16] - 2019-04-24
### Changed
- No longer passing around event store data source JNDI name. Using default name instead
- Removed now unnecessary factory classes

## [2.0.0-M15] - 2019-04-17
### Changed
- Update utilities to 1.17.0-M2
- Update test-utils to 1.22.0-M1
- Update framework to 6.0.0-M15
- Update framework-api to 4.0.0-M9

## [2.0.0-M14] - 2019-04-15
### Changed
- Simplify datasource usage and setup
- Update framework to 6.0.0-M14

## [2.0.0-M13] - 2019-04-15
### Changed
- Remove deprecated github_token entry from travis.yml
- Update framework to 6.0.0-M12

## [2.0.0-M12] - 2019-04-15
### Added
- Add Published Event Source

### Changed
- Updated framework-api to 4.0.0-M7
- Update framework to 6.0.0-M11

## [2.0.0-M11] - 2019-04-11
### Changed
- Catchup now returns events from linked_event rather than from event_log

## [2.0.0-M10] - 2019-04-05
### Changed
- Update framework to 6.0.0-M10

## [2.0.0-M9] - 2019-04-02
### Added
- Add Shuttering implementation to PublisherTimerBean

## [2.0.0-M8] - 2019-04-01
### Added
- Event Catchup is now Observable using the JEE event system
- Catchup now callable from JMX bean

## [2.0.0-M7] - 2019-03-22
### Fixed
- Use correct maven scope for test jars

### Added
- Support to for linked events after stream transformation
- truncate() method to the EventSource interface
- populate() method to the EventSource interface

## [2.0.0-M6] - 2019-03-20
### Fixed
- Moved test-utils into correct maven test scope

## [2.0.0-M5] - 2019-03-20
### Fixed
- Added missing beans.xml

## [2.0.0-M4] - 2019-03-15
### Added
- Add event linked tracking for processed events

### Removed
- Subscription current event number 

## [2.0.0-M3] - 2019-03-12
- Update framework to 6.0.0-M3

## [2.0.0-M2] - 2019-03-11
### Changed
- Update framework to 6.0.0-M2

## [2.0.0-M1] - 2019-03-07
### Changed
- Moved Subscription domain, parsing classes and builders to Framework

## [1.2.0-M4] - 2019-03-05
### Changed
- Update framework to 5.3.0-M5
- Update framework-api to 3.4.0-M2
- Create concurrent catchup process, replays events on different streams concurrently

## [1.2.0-M3] - 2019-03-01
### Changed
- Update framework to 5.3.0-M3
- Update framework-domain to 1.1.2-M1

## [1.2.0-M2] - 2019-02-27
### Changed
- Replaced BackwardsCompatibleJsonSchemaValidator with DummyJsonSchemaValidator in Integration Tests
- Update framework-api to 3.4.0-M1
- Update framework to 5.3.0-M2

## [1.2.0-M1] - 2019-02-08
### Added
- Subscription prioritisation  

## [1.1.3-M1] - 2019-02-08
### Added
- Subscription prioritisation  

## [1.1.3] - 2019-02-04
### Changed
- Update utilities to 1.16.4
- Update test-utils to 1.22.0
- Update framework to 5.1.1
- Update framework-domain to 1.1.1
- Update common-bom to 1.29.0
- Update framework-api to 3.2.0

## [1.1.2] - 2019-01-22
### Added
- JNDI configuration variable to enable/disable Event Catchup


## [1.1.1] - 2019-01-15
### Added
- Event Catchup on startup, where all unknown events are retrieved from the EventSource and played
- An event-number to each event to allow for event catchup
- Added a new TimerBean 'PrePublishBean'
- Added a new auto incrementing column event_number to event_log table
- Subscription event interceptor to update the current event number in the subscriptions table
- New util module
- Subscription repository
- Subscription liquibase for subscription table
- Better logging for event catchup 
- A new transaction is started for each event during event catchup
- Event catchup only runs with event listener components
- Indexes to 'name' and 'date_created' columns in the event_log table

### Changed
- Updated publish process to add events into a pre_publish_queue table
- Renamed the sequence_number column in event_stream to position_in_stream
- Renamed the sequence_id column in event_log to position_in_stream
- Tightened up transaction boundaries for event catchup so that each event is run in its own transaction
- Event catchup now delegated to a separate session bean to allow for running in RequestScope
- Current event number is initialised to zero if it does not exist on app startup

## [1.1.0-M8] - 2019-01-08
### Changed
- Tightened up transaction boundaries for event catchup so that each event is run in its own transaction
### Fixed
- Fixed error of two subscriptions created in database for each subscription

## [1.1.0-M7] - 2019-01-03
### Changed
- Fix checksum issue with liquibase scripts

## [1.1.0-M6] - 2019-01-02
### Changed
- Event catchup only runs with event listener components

## [1.1.0-M5] - 2018-12-31
### Changed
- Event catchup now delegated to a separate session bean to allow for running in RequestScope
- A new transaction is started for each event during event catchup
### Added
- New util module

## [1.1.0-M4] - 2018-12-28
- Release failed

## [1.1.0-M3] - 2018-12-28
- Release failed

## [1.1.0-M2] - 2018-12-17
### Added
- Better logging for event catchup 
### Changed
- Current event number is initialised to zero if it does not exist on app startup

## [1.1.0-M1]
### Changed
- Updated publish process to add events into a pre_publish_queue table
- Renamed the sequence_id column in event_log to position_in_stream
- Renamed the sequence_number column in event_stream to position_in_stream

### Added
- Event Catchup on startup, where all unknown events are retrieved from the EventSource and played
- An event-number to each event to allow for event catchup
- Added a new TimerBean 'PrePublishBean'
- Added a new auto incrementing column event_number to event_log table
- Subscription liquibase for subscription table
- Subscription repository
- Subscription event interceptor to update the current event number in the subscriptions table

## [1.0.4] - 2018-11-16
### Changed
- Updated framework-api to 3.0.1
- Updated framework to 5.0.4
- Updated framework-domain to 1.0.3

### Added
- Added a page size when reading stream of events

## [1.0.3] - 2018-11-13
### Changed
- Removed hard coded localhost from test datasource url

## [1.0.2] - 2018-11-09
### Changed
- Update framework to 5.0.3

## [1.0.1] - 2018-11-07
### Changed
- Update framework to 5.0.2

## [1.0.0] - 2018-11-07
### Changed
- Update framework to 5.0.0
- Renamed DatabaseCleaner method to match table name

## [1.0.0-M6] - 2018-11-06

### Changed
- Update framework to 5.0.0-M3
- Removed the need to have event-source.yaml

## [1.0.0-M5] - 2018-11-02

### Changed
- Remove requirement to have a subscription-descriptor.yaml on the classpath

### Added
- event-publisher-process to event-store-bom
- New test-utils-event-store module to hold TestEventRepository moved from framework

## [1.0.0-M4] - 2018-10-31

### Changed
- Update framework-domain to version 1.0.0-M3

## [1.0.0-M2] - 2018-10-31

### Changed
- Reverted names of event buffer tables to their original names: 
subscription -> stream_status, event-buffer -> stream buffer
- Moved test-utils-persistence into this project from 
[Framework](https://github.com/CJSCommonPlatform/microservice_framework)

## [1.0.0-M1] - 2018-10-26

### Added
- Extracted project from all event store related modules in Microservices Framework 5.0.0-M1: https://github.com/CJSCommonPlatform/microservice_framework


