/**
 * Copyright (C) 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
def pomFile = new File( basedir, 'pom.xml' )
System.out.println( "Slurping POM: ${pomFile.getAbsolutePath()}" )


File buildLog = new File( basedir, 'build.log' )
assert buildLog.getText().contains( "Activating AltDeploy extension" )
assert buildLog.getText().contains( "Deploy plugin is org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:2.7:runtime" )
assert buildLog.getText().count("Found deploy plugin org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:RELEASE") == 2
assert buildLog.getText().count("Resetting maven-deploy-plugin org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:RELEASE") == 2