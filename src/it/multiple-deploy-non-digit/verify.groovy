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
assert buildLog.getText().contains( "malformed project" )
assert buildLog.getText().contains( "Found non-digit versions for deploy plugin org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:RELEASE:runtime" )
if (mavenVersion.matches("^3.9.*") ) {
    assert buildLog.getText().contains( "Found multiple minor versions of maven-deploy-plugin; this is a malformed project")
    assert buildLog.getText().contains( "maven-deploy-plugin:3.0.0" )
    assert !buildLog.getText().contains( "maven-deploy-plugin:2.6" )
    assert buildLog.getText().contains( "maven-deploy-plugin:3.0.0-M1" )
} else {
    assert buildLog.getText().contains("Found multiple minor versions of maven-deploy-plugin; this is a malformed project")
    assert buildLog.getText().contains( "maven-deploy-plugin:2.7" )
    assert buildLog.getText().contains( "maven-deploy-plugin:2.6" )
    assert !buildLog.getText().contains( "maven-deploy-plugin:3.0.0-M1" )
}
