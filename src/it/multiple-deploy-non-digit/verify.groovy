def pomFile = new File( basedir, 'pom.xml' )
System.out.println( "Slurping POM: ${pomFile.getAbsolutePath()}" )


File buildLog = new File( basedir, 'build.log' )
assert buildLog.getText().contains( "Activating AltDeploy extension" )
assert buildLog.getText().contains( "malformed project" )
assert buildLog.getText().contains( "Found non-digit versions for deploy plugin org.apache.maven.plugins:maven-deploy-plugin:maven-plugin:RELEASE:runtime" )
assert buildLog.getText().contains( "Found multiple minor versions of maven-deploy-plugin; this is a malformed project" )
assert buildLog.getText().contains( "maven-deploy-plugin:2.6" )
assert buildLog.getText().contains( "maven-deploy-plugin:2.7" )
assert !buildLog.getText().contains( "maven-deploy-plugin:3.0.0-M1" )
