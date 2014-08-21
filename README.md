Testing Java EE Applications on WebLogic 12.1.3 Using Arquillian
================================================================
This application demonstrates how Java EE applications can be effectively 
tested using Arquillian on WebLogic 12.1.3. The application contains a 
set of JUnit tests that you should examine. The tests will be run as part 
of the Maven build. The tests require a running instance of 
WebLogic (please see setup instructions).

WebLogic 12.1.3 supports Java EE 6 and some key Java EE 7 APIs - 
WebSocket, JAX-RS 2, JSON-P and JPA 2.1. The application uses both
these APIs as well as Java EE 6 features such as CDI, EJB 3.1 and JSF 2.

Setup
-----
* Install WebLogic 12.1.3
* The Java EE 7 APIs are not automatically enabled. This 
  [Aquarium blog entry](https://blogs.oracle.com/theaquarium/entry/java_ee_7_support_comes)
  is invaluable in better understanding the Java EE 7 API support in 
  WebLogic 12.1.3. The referenced white paper explains the steps necessary to 
  enable the APIs.
* The demo application requires a data source. WebLogic 12.1.3 supports the 
  Java EE 6 standard @DataSourceDefinition and corresponding XML elements. 
  However, this does not seem to work with JPA 2.1 so we could not use it. A bug 
  has been filed to get this fixed. Yet another approach is to use WebLogic 
  proprietary JDBC modules. This requires an EAR file and would significantly 
  complicate an otherwise simple WAR build. For this reason, this was not an
  approach we used. As a result, however, you will need to manually create a
  data source for the demo application. The data source is expected to be bound 
  to 'jdbc/ActionBazaarDB'. We used embedded Derby but any underlying database
  should work. If helpful, a sample WebLogic data source definition is provided 
  [here](ActionBazaarDB-jdbc.xml).
* Please download this repository. You can use Git or just the simple zip
  download.
* The demo is just a simple Maven project under the [actionbazaar](actionbazaar)
  directory. You should be able to open it up in any Maven capable IDE, we used
  NetBeans.
* If desired setup WebLogic in your IDE. This is what we tested.
* The tests in the Maven build are executed against a running WebLogic instance.
  You will need to configure 
  [this file] (actionbazaar/src/test/resources/arquillian.xml) with the details
  of your running WebLogic instance (you could run the instance via the IDE).
  For details on configuring WebLogic for Arquillian, look 
  [here] (https://docs.jboss.org/author/display/ARQ/WLS+12.1+-+Remote).
* If desired, you can deploy and run the application itself. We did this both
  via NetBeans and by using the plain Maven generated war file.