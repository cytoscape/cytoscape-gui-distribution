To build the installer bundles, edit pom.xml to change the <install4j.executable> 
property to the location of the "install4jc" application on your machine.

Then run:

	mvn clean install 

The installers will be created in the target/install4j subdirectory.
It's no longer necessary to run "mvn install4j:compile" as that task has
been scheduled to run automatically in the "compile" phase.

