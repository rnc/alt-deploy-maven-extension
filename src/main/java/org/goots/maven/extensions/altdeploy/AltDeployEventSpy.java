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
package org.goots.maven.extensions.altdeploy;

import org.apache.maven.BuildFailureException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.maven.execution.ExecutionEvent.Type.SessionStarted;

@Named
@Singleton
public class AltDeployEventSpy extends AbstractEventSpy
{
    private static final String LATEST_THREE_VERSION = "3.0.0-M1";

    private static final ArtifactVersion POST_THREE_VERSION = new DefaultArtifactVersion( "3.0.0-A1" );

    private static final String ALT_DEPLOY = "altDeploymentRepository";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @SuppressWarnings( "FieldCanBeLocal" )
    private final String DISABLE_ALTDEPLOY_EXTENSION = "altdeploy.extension.disable";

    private final LayoutParser layoutParser;

    @Inject
    public AltDeployEventSpy(LayoutParser lp)
    {
        this.layoutParser = lp;
    }

    @Override
    public void onEvent( Object event )
    {
        if ( isEventSpyDisabled() )
        {
            return;
        }

        if ( event instanceof ExecutionEvent )
        {
            final ExecutionEvent ee = (ExecutionEvent) event;
            final ExecutionEvent.Type type = ee.getType();

            if ( type == SessionStarted)
            {
                try
                {
                    logger.info( "Activating AltDeploy extension {}", ManifestUtils.getManifestInformation() );

                    checkForAltDeployment( ee.getSession() );
                }
                catch ( BuildFailureException | IOException | OverConstrainedVersionException | ArtifactDeploymentException e )
                {
                    ee.getSession().getResult().addException( e );
                }
            }
        }
    }

    private void checkForAltDeployment( MavenSession session )
                    throws BuildFailureException, OverConstrainedVersionException, ArtifactDeploymentException
    {
        if ( session == null )
        {
            throw new BuildFailureException( "Session is null" );
        }

        final Set<Artifact> artifacts = session.getAllProjects()
                                               .stream()
                                               .flatMap( p -> p.getPluginArtifacts().stream() )
                                               .collect( Collectors.toSet() );

        final Set<Artifact> result = processArtifacts(artifacts);

        if ( result.size() > 1 )
        {
            long threeCount = result.stream().filter( r -> r.getVersion().startsWith( "3" ) ).count();
            if ( threeCount == result.size() || threeCount == 0 )
            {
                // Either the result is all 3.x or 2.x - print out a warning only
                logger.warn( "Found multiple minor versions of maven-deploy-plugin; this is a malformed project.\n\t{}", result );
            }
            else
            {
                logger.error( "Found multiple major versions of maven-deploy-plugin; this is a malformed project.\n\t{}", result );
                throw new BuildFailureException( "Found multiple versions of maven-deploy-plugin; this is a malformed project: " +  result);
            }
        }

        if ( ! result.isEmpty() )
        {
            Artifact deploy = result.stream().findAny().get();

            logger.debug( "Deploy plugin is {}", deploy);

            updateProperties( deploy, session.getUserProperties() );
            updateProperties( deploy, session.getSystemProperties() );
            updateProperties( deploy, System.getProperties() );

            for ( Profile p : session.getSettings().getProfiles() )
            {
                logger.debug( "Examining setting profile {}", p.getId() );
                updateProperties( deploy, p.getProperties() );
            }
            for ( MavenProject p : session.getAllProjects() )
            {
                logger.debug( "Examining project {}", p.getId() );
                updateProperties( deploy, p.getProperties() );
            }
        }
    }

    // Package private for testing
    Set<Artifact> processArtifacts( Set<Artifact> artifacts )
    {
        final List<Artifact> intermediary =
                        artifacts.stream().filter( a -> "maven-deploy-plugin".equals( a.getArtifactId() ) ).
                                        sorted().distinct().collect( Collectors.toList() );

        final Set<Artifact> result = intermediary.stream().filter( a -> {
            if ( Character.isDigit( a.getVersion().charAt( 0 ) ) )
            {
                return true;
            }
            else
            {
                logger.debug( "Found non-digit versions for deploy plugin {}", a );
                return false;
            }
        } ).collect(Collectors.toSet());

        if ( result.size() != intermediary.size() )
        {
            intermediary.forEach( a -> {
                if ( Character.isLetter( a.getVersion().charAt( 0 ) ) )
                {
                    String target;
                    if ( result.size() > 0 )
                    {
                        target = result.stream().findFirst().get().getVersion();
                    }
                    else
                    {
                        target = LATEST_THREE_VERSION;
                    }
                    logger.info( "Resetting maven-deploy-plugin {} to version {}", a, target );
                    a.setVersion( target );
                    result.add( a );
                }
            } );
        }

        return result;
    }

    private void updateProperties( Artifact deploy, Properties properties )
                    throws OverConstrainedVersionException, ArtifactDeploymentException
    {
        if ( properties.containsKey( ALT_DEPLOY ) )
        {
            String altDeploy = properties.getProperty( ALT_DEPLOY );

            if ( deploy.getSelectedVersion().compareTo( POST_THREE_VERSION ) > 0 )
            {
                properties.setProperty( ALT_DEPLOY, layoutParser.parse( altDeploy ).convert( LayoutParser.Format.MODERN ) );
            }
            else
            {
                properties.setProperty( ALT_DEPLOY, layoutParser.parse( altDeploy ).convert( LayoutParser.Format.LEGACY ) );
            }
            logger.info( "Updating {} configuration of {} to {}", ALT_DEPLOY, altDeploy, properties.getProperty( ALT_DEPLOY )  );
        }
    }

    private boolean isEventSpyDisabled()
    {
        return "true".equalsIgnoreCase( System.getProperty( DISABLE_ALTDEPLOY_EXTENSION ) ) || "true".equalsIgnoreCase(
                        System.getenv( DISABLE_ALTDEPLOY_EXTENSION ) );
    }
}
