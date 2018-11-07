# Change Log
All notable changes to this project will be documented in this file, which follows the guidelines
on [Keep a CHANGELOG](http://keepachangelog.com/). This project adheres to
[Semantic Versioning](http://semver.org/).

## [Unreleased]

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


