# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]

## [2.0.5] - 2019-08-19
### Changed
- Update framework to 6.0.5

## [2.0.4] - 2019-08-16
### Changed
- Update framework to 6.0.4

## [2.0.3] - 2019-08-16
### Changed
- Update framework to 6.0.3

## [2.0.2] - 2019-08-15
### Changed
- Update framework to 6.0.2

## [2.0.1] - 2019-08-15
### Changed
- Update framework to 6.0.1

## [2.0.0] - 2019-08-15

### Added
- Event Catchup is now Observable using the JEE event system
- Observers for the shutter then catchup process
- Add Shuttering implementation to PublisherTimerBean
- Added an Observer for PublishedEvent Rebuild
- CommandHandlerQueueChecker to check the command handler queue is empty when shuttering
- Added implementation for updating StreamStatus table with component column
- Add Published Event Source
- Add event linked tracking for processed events
- Subscription prioritisation  
- Catchup now callable from JMX bean
- Support to for linked events after stream transformation
- Java API to retrieve all stream IDs (active and inactive ones)
- truncate() method to the EventSource interface
- populate() method to the EventSource interface
- Added component to the event buffer to allow it to handle both the event listener and indexer
- Added component column to processed_event table to allow Event Listener and Indexer to create unique primary keys   

### Changed
- Catchup and rebuild moved to a new 'event-store-management' module  
- Catchup and rebuild now use the new System Command jmx architecture
- Shuttering executors no longer register themselves but are discovered at system startup and registered
- Implement usage of new system database
- Pre-publish and publish timer beans run for a given timer max runtime. The max time can be set with JNDI values "pre.publish.timer.max.runtime.milliseconds" and "event.dequer.timer.max.runtime.milliseconds".
- Renamed subscription-repository to event-tracking
- Add event-buffer-core and event-subscription-registry pom entries
- Moved shuttering from the publishing timer bean into the command api in framework
- Events for catchup and rebuild moved in from framework-api
- Merged event-publisher modules into one
- Event-Buffer functionality with changes to StreamBuffer & StreamStatus tables to support EventIndexer
- PrePublishBean now limits the maximum number of events published per run of the timer bean.
- No longer passing around event store data source JNDI name. Using default name instead
- Simplify datasource usage and setup
- Moved Subscription domain, parsing classes and builders to Framework
- Create concurrent catchup process, replays events on different streams concurrently
- Catchup now returns events from published_event rather than from event_log
- Replaced BackwardsCompatibleJsonSchemaValidator with DummyJsonSchemaValidator in Integration Tests
- Catchup, Rebuild and Index no longer call shutter/unshutter when running
- Improved utility classes for getting Postgres DataSources
- Renamed TestEventInserter to EventStoreDataAccess as an improved test class
- Added a findEventsByStreamId() method to EventStoreDataAccess
- Moved system commands to framework jmx-api
- Updated framework-api to 4.0.1
- Updated framework to 6.0.0
- Updated common-bom to 2.4.0
- Updated utilities to 1.20.1
- Updated test-utils to 1.24.3

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
- New util module
- Better logging for event catchup 
- An event-number to each event to allow for event catchup
- Added a new TimerBean 'PrePublishBean'
- Added a new auto incrementing column event_number to event_log table
- Subscription liquibase for subscription table
- Subscription repository
- Subscription event interceptor to update the current event number in the subscriptions table

### Changed
- Updated publish process to add events into a pre_publish_queue table
- Renamed the sequence_number column in event_stream to position_in_stream
- Renamed the sequence_id column in event_log to position_in_stream
- Tightened up transaction boundaries for event catchup so that each event is run in its own transaction
- Event catchup now delegated to a separate session bean to allow for running in RequestScope
- Current event number is initialised to zero if it does not exist on app startup
- Tightened up transaction boundaries for event catchup so that each event is run in its own transaction
- A new transaction is started for each event during event catchup
- Updated publish process to add events into a pre_publish_queue table
- Renamed the sequence_id column in event_log to position_in_stream
- Renamed the sequence_number column in event_stream to position_in_stream

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
- Removed the need to have event-source.yaml
- Remove requirement to have a subscription-descriptor.yaml on the classpath
- event-publisher-process to event-store-bom
- New test-utils-event-store module to hold TestEventRepository moved from framework
- Reverted names of event buffer tables to their original names: 
subscription -> stream_status, event-buffer -> stream buffer
- Moved test-utils-persistence into this project from 
[Framework](https://github.com/CJSCommonPlatform/microservice_framework)

### Added
- Extracted project from all event store related modules in Microservices Framework 5.0.0-M1: https://github.com/CJSCommonPlatform/microservice_framework


