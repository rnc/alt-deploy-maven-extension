/*
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
File buildLog = new File( basedir, 'build.log' )
System.out.println( "Slurping buildLog: ${buildLog.getAbsolutePath()}" )

assert buildLog.getText().contains( "Activating AltDeploy extension" )
if (mavenVersion.equals("3.9.0") ) {
    assert buildLog.getText().contains("Deploy plugin is org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:3.0.0:runtime")
} else if (mavenVersion.equals("3.9.1") ) {
        assert buildLog.getText().contains("Deploy plugin is org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:3.1.0:runtime")
} else {
    assert buildLog.getText().contains( "Deploy plugin is org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:2.7:runtime" )
}
assert buildLog.getText().count("Found deploy plugin org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:RELEASE") == 2
assert buildLog.getText().count("Resetting maven-deploy-plugin org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:RELEASE") == 2
