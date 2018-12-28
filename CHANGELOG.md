# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]

## [1.1.0-M4] - 2018-12-28
### Changed
- Event catchup now delegated to a separate session bean to allow for running in RequestScope
- A new transaction is started for each event during event catchup
### Added
- New util module

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
_ Renamed the sequence_number column in event_stream to position_in_stream

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


