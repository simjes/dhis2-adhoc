## DHIS 2 Ad-hoc Tool

The ad-hoc tool is written in Java. It provides access to the DHIS 2 Java APIs and services.

The purpose of this tool is to assist in performing ad-hoc tasks which benefits from having the DHIS 2 service layer accessible. Examples of such tasks are writing complex custom data entry forms to file and performing database operations which cannot easily be solved with SQL.

The ad-hoc tool is based on the command pattern, and lets you write your own command classes which perform the actual work.

Command classes must be annotated with the @Executed annotation, mapped as a Spring bean if necessary and added to the commands method in RunMe.java.

The RunMe.java class is the starting point, and this class can be run as a Java project using your IDE or the command line.

