find ./src -name "*.java" > allSource.log
javac -classpath ./lib/xmlrpc-client-3.1.3.jar:./lib/xmlrpc-common-3.1.3.jar:./lib/xmlrpc-server-3.1.3.jar:./lib/log4j-1.2.17.jar.jar:./lib/jade-4.3.2.jar.jar:./lib/commons-logging-1.1.3.jar:./lib/commons-codec-1.9.jar:./lib/bdi4jade.jar @allSource.log
