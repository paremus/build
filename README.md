# The Paremus Product Build Repository

This repository contains the top-level POM configuration for other Paremus builds, and also any boms/code related to building and testing.

# How to build this repository

This repository can be built using Maven 3.5.4 and Java 8. 

## Build profiles

By default the build will run with all tests, and lenient checks on copyright headers. To enable strict copyright checking (required for deployment) then the `strict-license-check` profile should be used, for example

    `mvn -P strict-license-check clean install`

If you make changes and do encounter licensing errors then the license headers can be regenerated using the `generate-licenses` profile

    `mvn -P generate-licenses process-sources`
