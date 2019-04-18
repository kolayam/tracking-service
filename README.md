### NIMBLE tracking-service Repository###
> Tracking service in NIMBLE platform. This Repo is part of NIMBLE Project (https://www.nimble-project.org).

### Introduction ###
> Tracking Service REST APIs enables to track product with EPC codes.

### Setup ###

> Run `mvn clean package` in the folder, where the file pom.xml locates in, for building the application.

-------------------------------------------------
1. Swagger problem  
(1) Das Maven-Plugin kann nicht integriert werden, was nicht weiter schlimm ist. Hier einfach den Fehler in eine Warnung umwandeln.
In Eclipse: "Preferences -> Maven -> Error/Warnings" und Fehler in Warnung umwandeln: "Plugin execution not converted by lifecycle configuration”
(2) Um den von Swagger autogenerierten Quellcode zu Eclipse hinzuzufügen bitte
	(a) "mvn clean package” in der Konsole ausführen, um den Quellcode zu generieren. Um "mvn package" richtig auszuführen, die pfad zu owl Datei in "MediatorSPARQLDerivationTest" angepasst werden muss.
	
	(b) Im “Project Explorer” in Eclipse den Ordner “target/generated-sources/swagger/src/gen/java/main” als Source Folder hinzufügen.
-------------------------------------------

### Run Locally ###

> Run `mvn spring-boot:run' 

### Docker ###

> Run `mvn docker:build` for building docker image for the application.

### Run with Docker ###

> Run 'docker run -d -e EPCIS_SERVICE_URL="http://localhost:8080" nimbleplatform/tracking-service:0.0.10' for running the docker image. Please update environment variable correspondingly to connect with EPCIS REST API services.  

### Quick Test ###
> When it works properly, http://localhost:8090/greeting will return a greeting message.










