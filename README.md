# It is simple maven project with one class - Main 
Predefined conditions to run application (either first or second):
1) Java installed
2) Maven installed

Instructions to run:
* Using Intellij IDEA: click run button near class name.
* Using mvn command line tool: run command below from CMD
```
mvn exec:java -D"exec.mainClass"="Main"
```