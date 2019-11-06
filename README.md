[![Build Status (Travis CI)](https://travis-ci.org/rnc/alt-deploy-maven-extension.svg?branch=master)](https://travis-ci.org/rnc/alt-deploy-maven-extension.svg?branch=master)



# Maven extension to handle altDeploymentRepository legacy/new formats.

## Overview

This extension will activate if a maven-deploy-plugin _and_ `altDeploymentRepository` have been specified.
If it has, then it will examine the value for `altDeploymentRepository` and convert it to the correct form
for the plugin version in use.

It will search for `altDeploymentRepository` in the following locations:
* Project properties
* User properties
* System Properties
* Settings file properties

If the deployment plugin version is >= 3.0.0-M1 it uses:

```
id::url
```

otherwise (e.g. for 2.8) it uses

```
id::layout::url
```

(where layout defaults to `default`).


## Installation

It is recommended to install in your Maven `lib/ext` directory. Binary releases can be obtained from Maven Central [here](http://central.maven.org/maven2/org/goots/maven/extensions/alt-deploy-maven-extension) and snapshots from [here](https://oss.sonatype.org/content/repositories/snapshots/org/goots/maven/extensions/alt-deploy-maven-extension).

## Configuration

There is no configuration required for this plugin. It can be disabled by setting `altdeploy.extension.disable` to true.
