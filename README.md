# Cytoscape 3 Desktop Version
------------

This is the desktop version of Cytoscape 3.

## Creating Installers

For deploying core apps to nexus, follow the steps listed below:

1. Update the version number for the core app in pom.xml in assembly directory in this repository.

2. In the pom.xml for the core app, add repositories and distributionManagement entries. You can just copy these from an existing pom, like CyREST: https://github.com/cytoscape/cyREST/blob/3e1ef7fcf867dbcd80749618c4134173e02688e9/pom.xml#L35

3. Add this as an entry to your maven settings.xml file (location varies between OS's, check the Maven documentation for location).

   If you use brew to install maven, then the settings file should be in: ```/usr/local/Cellar/maven/<version>/libexec/conf```

```
<servers>
  <server>
    <id>releases</id>
    <username>deployment</username>
    <password>deploy</password>
  </server>
  <server>
    <id>snapshots</id>
    <username>deployment</username>
    <password>deploy</password>
  </server>
  <server>
    <id>thirdparty</id>
    <username>deployment</username>
    <password>deploy</password>
  </server>
  <server>
    <id>cytoscape_releases</id>
    <username>cytoscape_deployer</username>
    <password>turtlesallthewaydown</password>
  </server>
  <server>
    <id>cytoscape_snapshots</id>
    <username>cytoscape_deployer</username>
    <password>turtlesallthewaydown</password>
  </server>
  <server>
    <id>cytoscape_thirdparty</id>
    <username>cytoscape_deployer</username>
    <password>turtlesallthewaydown</password>
  </server>
</servers>
```


4. Add the app to the core-apps-meta pom.xml (see this: https://github.com/cytoscape/core-apps-meta/blob/d376e7efe0d393dea90e687c177a7b813598293e/pom.xml#L139). 


