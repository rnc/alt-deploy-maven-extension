def pomFile = new File( basedir, 'pom.xml' )
System.out.println( "Slurping POM: ${pomFile.getAbsolutePath()}" )


File buildLog = new File( basedir, 'build.log' )

def pom = new XmlSlurper().parse( pomFile )
def v = pom.version.text()
def g = pom.groupId.text()
def a = pom.artifactId.text()

assert buildLog.getText().contains( "Activating AltDeploy extension" )
assert buildLog.getText().contains( "Deploy plugin is" )
assert buildLog.getText().contains( "of indy-mvn::default::file://")
assert buildLog.getText().contains( "to indy-mvn::file://")


def repodir = new File(localRepositoryPath, "${g.replace('.', '/')}/${a}/${v}" )
def repopom = new File( repodir, "${a}-${v}.pom" )
System.out.println( "Checking for installed pom: ${repopom.getAbsolutePath()}")
assert repopom.exists()

