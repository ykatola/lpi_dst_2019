# It is multi-client application
Predefined conditions to run application (either first or second):
1) Java installed
2) Maven installed

Instructions to run:
* Using Intellij IDEA: click run button near class name.
* Using mvn command line tool: run command below from CMD
```
mvn exec:java -D"exec.mainClass"="lpi.Main"
```

* You will be suggested choosing one of the client to use or exit from program:
1. RMI Client.
2. TCP Client.
3. REST Client.
4. MQ Client.
5. TCP Client.
6. Exit

* Then you should be able to choose port and ip address of server to connect to
1. In port section use only numbers to select port.
2. In ip section enter ip address in form like - 0.0.0.127 or type 'd' to use default - 'localhost'
3. Then, after successful connection you will be able to execute commands.

* Every client has implemented functions you can run by entering one of the following command:
1. ping
2. echo {arg}
3. login {loginName} {password}
4. list
5. msg {receiver} {messageContent}
6. file {receiver} {pathToFile}
7. exit