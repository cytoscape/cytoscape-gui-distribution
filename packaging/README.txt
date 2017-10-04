To build the installer bundles, edit pom.xml to change the <install4j.executable> 
property to the location of the "install4jc" application on your machine.

Then run:

	mvn clean install 

The installers will be created in the target/install4j subdirectory.
It's no longer necessary to run "mvn install4j:compile" as that task has
been scheduled to run automatically in the "compile" phase.

Note to Windows developers:

You'll run into two kinds of trouble. 

First, Maven doesn't deal well with blanks in path names. This causes
problems when Install4J installs into "C:\Program Files", which is 
its default. It also causes trouble if your cytoscape-gui-distribution
repo is in a user directory that contains a blank (e.g., 
"C:\Users\Elmer Fudd"). In both cases, you can solve the problem 
by creating soft links (e.g., C:\ProgramFiles and C:\Users\ElmerFudd)
that refer to the actual directories 
(e.g., mkdir /d C:\ProgramFiles "C:\Program Files"). 

Second, when Maven checks the <install4j.executable> property, it's
looking for an exact file name match. You must supply the .EXE 
suffix (e.g., C:\ProgramFiles\install4j6\bin\install4jc.exe).




This causes trouble in the 
<install4j.executable>
