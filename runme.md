# Library Management System - Run Guide

## Prerequisite: Build the project first (only needed once)

```bash
mvn clean compile
```

## Run automated simulator (tests all 5 tasks)

```bash
mvn exec:java -Dexec.mainClass="rw.rndl.LibrarySimulator"
```

## Run interactive menu application

```bash
mvn exec:java -Dexec.mainClass="rw.rndl.LibraryApplication"
```
