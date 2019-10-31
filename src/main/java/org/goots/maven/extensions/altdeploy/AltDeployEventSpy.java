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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.maven.execution.ExecutionEvent.Type.SessionStarted;

@Named
@Singleton
public class AltDeployEventSpy extends AbstractEventSpy
{
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
                    logger.info( "Activating AltDeploy extension {}", Utils.getManifestInformation() );

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

        final Properties userProps = session.getUserProperties();
        final Set<Artifact> artifacts = session.getAllProjects()
                                               .stream()
                                               .flatMap( p -> p.getPluginArtifacts().stream() )
                                               .collect( Collectors.toSet() );
        final Optional<Artifact> result =
                        artifacts.stream().filter( a -> "maven-deploy-plugin".equals( a.getArtifactId() ) ).findAny();

        if ( result.isPresent() )
        {
            Artifact deploy = result.get();

            if ( userProps.containsKey( ALT_DEPLOY ) )
            {
                String altDeploy = userProps.getProperty( ALT_DEPLOY );
                logger.debug( "### Found altDeploymentKey {} and current format is {}", altDeploy, layoutParser.parse( altDeploy ).originalFormat() );

                if ( deploy.getSelectedVersion().compareTo( POST_THREE_VERSION ) > 0 )
                {
                    userProps.setProperty( ALT_DEPLOY, layoutParser.parse( altDeploy ).convert( LayoutParser.Format.MODERN ) );
                }
                else
                {
                    userProps.setProperty( ALT_DEPLOY, layoutParser.parse( altDeploy ).convert( LayoutParser.Format.LEGACY ) );
                }
                logger.info( "Found deploy plugin {} and updating {} to {}", deploy, ALT_DEPLOY, userProps.getProperty( ALT_DEPLOY )  );
            }
        }
    }

    private boolean isEventSpyDisabled()
    {
        return "true".equalsIgnoreCase( System.getProperty( DISABLE_ALTDEPLOY_EXTENSION ) ) || "true".equalsIgnoreCase(
                        System.getenv( DISABLE_ALTDEPLOY_EXTENSION ) );
    }
}
