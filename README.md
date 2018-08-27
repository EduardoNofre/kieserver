# kieserver
1. The kie api is at http://${host}:${port}/services/rest/. For example http://localhost:8080/services/rest/server
1. Because of https://issues.jboss.org/browse/DROOLS-2475, I copy the JSONMarshaller source code in the project. Don't remove the source file until the issue is fixed.
2. There are some magic system properties when starting. Please check the kie source code or document to understand.
3. There's an ironjacamar.no_delist_resource_all system property. I'm not sure if I use it correctly or not. However, it seems MySQL requires this setting, otherwise "XAER_RMFAIL: The command cannot be executed when global transaction is in the  IDLE state" happens from time to time.  I appreciate if anybody can tell me the right solution. 
4. IronJacamar requires jboss stdio which overrides the stand IO. I tried my best to display the information. But the output mix the jdk log format. Develop a JDK logger format to meet your requirement by yourself.
5. It's a skeleton of our product as a POC. I have no plan to maintain or enhance it. However, if there's any bug in the code, I'm happy to fix it.
6. There's no any license or agreement required to use this code. And no warranty either.
