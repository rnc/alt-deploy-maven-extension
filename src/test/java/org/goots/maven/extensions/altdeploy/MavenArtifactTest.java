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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class MavenArtifactTest
{
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    private MavenProject project1;
    private Plugin mdp;

    @Before
    public void setup()
    {
        mdp = new Plugin();
        mdp.setGroupId( "org.apache.maven" );
        mdp.setArtifactId( "maven-deploy-plugin" );
        mdp.setVersion( "" );

        project1 = new MavenProject();
        project1.setGroupId( "project1" );
    }

    @Test
    public void compareArtifact() throws OverConstrainedVersionException, IllegalAccessException
    {
        ArtifactVersion post3version = (ArtifactVersion) FieldUtils.readDeclaredStaticField( AltDeployEventSpy.class, "POST_THREE_VERSION", true );

        Artifact legacy = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "2.8",
                                          "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Artifact post3M1 = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "3.0.0-M1",
                                              "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );

        assertTrue ( legacy.getSelectedVersion().compareTo( post3version ) < 0);
        assertTrue ( legacy.getSelectedVersion().compareTo( post3M1.getSelectedVersion() ) < 0);
        assertTrue( post3M1.getSelectedVersion().compareTo( post3version ) > 0 );
    }

    @Test
    public void compareArtifactWithOnlyRelease()
    {
        Artifact legacy = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "RELEASE",
                                               "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Map<Artifact, Plugin> collection = new HashMap<>();
        collection.put( legacy, mdp );
        Map<MavenProject, Map<Artifact, Plugin>> projectPlugins = new HashMap<>();
        projectPlugins.put( project1, collection );


        AltDeployEventSpy spy = new AltDeployEventSpy( new LayoutParser() );
        Set<Artifact> result = spy.processArtifacts( projectPlugins );

        assertEquals( 1, result.size() );
        assertTrue ( result.stream().findFirst().get().getVersion().equalsIgnoreCase( "3.0.0-M1" ));
    }

    @Test
    public void compareArtifactWithReleaseAndPreThree()
    {
        Artifact text = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "RELEASE",
                                               "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Artifact legacy = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "2.8",
                                               "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Map<Artifact, Plugin> collection = new HashMap<>();
        collection.put( legacy, mdp );
        collection.put( text, mdp );
        Map<MavenProject, Map<Artifact, Plugin>> projectPlugins = new HashMap<>();
        projectPlugins.put( project1, collection );

        AltDeployEventSpy spy = new AltDeployEventSpy( new LayoutParser() );
        Set<Artifact> result = spy.processArtifacts( projectPlugins );

        assertEquals( 1, result.size() );
        assertTrue ( result.stream().findFirst().get().getVersion().equalsIgnoreCase( legacy.getVersion() ));
    }

    @Test
    public void compareArtifactWithReleaseAndPostThree()
    {
        Artifact text = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "RELEASE",
                                               "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Artifact post3M1 = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "3.0.0-M1",
                                                "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Map<Artifact, Plugin> collection = new HashMap<>();
        collection.put( text, mdp );
        collection.put( post3M1, mdp );
        Map<MavenProject, Map<Artifact, Plugin>> projectPlugins = new HashMap<>();
        projectPlugins.put( project1, collection );

        AltDeployEventSpy spy = new AltDeployEventSpy( new LayoutParser() );
        Set<Artifact> result = spy.processArtifacts( projectPlugins );

        assertEquals( 1, result.size() );
        assertTrue ( result.stream().findFirst().get().getVersion().equalsIgnoreCase( post3M1.getVersion() ));
    }

    @Test
    public void compareArtifactWithReleaseAndMultiple()
    {
        Artifact text = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "RELEASE",
                                             "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Artifact legacy = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "2.8",
                                               "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Artifact post3M1 = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "3.0.0-M1",
                                                "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Map<Artifact, Plugin> collection = new HashMap<>();
        collection.put( legacy, mdp );
        collection.put( text, mdp );
        collection.put( post3M1, mdp );
        Map<MavenProject, Map<Artifact, Plugin>> projectPlugins = new HashMap<>();
        projectPlugins.put( project1, collection );

        AltDeployEventSpy spy = new AltDeployEventSpy( new LayoutParser() );
        Set<Artifact> result = spy.processArtifacts( projectPlugins );

        assertEquals( 2, result.size() );
        assertTrue ( result.stream().findFirst().get().getVersion().equalsIgnoreCase( "3.0.0-M1" ));
    }

    @Test
    public void compareArtifactWithMultiple()
    {
        Artifact legacy = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "2.8",
                                               "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Artifact post3M1 = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "3.0.0-M1",
                                                "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Map<Artifact, Plugin> collection = new HashMap<>();
        collection.put( legacy, mdp );
        collection.put( post3M1, mdp );
        Map<MavenProject, Map<Artifact, Plugin>> projectPlugins = new HashMap<>();
        projectPlugins.put( project1, collection );

        AltDeployEventSpy spy = new AltDeployEventSpy( new LayoutParser() );
        Set<Artifact> result = spy.processArtifacts( projectPlugins );

        assertEquals( 2, result.size() );
        assertTrue ( result.stream().findFirst().get().getVersion().equalsIgnoreCase( "3.0.0-M1" ));
    }

    @Test
    public void compareArtifactWithMultipleProject()
    {
        Artifact legacy = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "2.8",
                                               "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Artifact post3M1 = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "RELEASE",
                                                "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        Artifact legacy2 = new DefaultArtifact( "org.apache.maven.plugins", "maven-deploy-plugin", "2.8",
                                                "runtime", "maven-plugin", "", new DefaultArtifactHandler(  ) );
        mdp.setVersion( "2.8" );
        Plugin mdp2 = mdp.clone();
        mdp2.setGroupId( "org.apache.maven" );
        mdp2.setArtifactId( "maven-deploy-plugin" );
        mdp2.setVersion( "2.8" );
        Plugin mdp3 = mdp.clone();
        mdp3.setGroupId( "org.apache.maven" );
        mdp3.setArtifactId( "maven-deploy-plugin" );
        mdp3.setVersion( "RELEASE" );

        Map<Artifact, Plugin> collection = new HashMap<>();
        collection.put( legacy, mdp );
        collection.put( post3M1, mdp2 );
        Map<Artifact, Plugin> collection2 = new HashMap<>();
        collection2.put( legacy2, mdp3 );
        Map<MavenProject, Map<Artifact, Plugin>> projectPlugins = new HashMap<>();
        projectPlugins.put( project1, collection );
        MavenProject project2 = new MavenProject();
        project2.setGroupId( "project2" );
        projectPlugins.put( project2, collection2 );

        AltDeployEventSpy spy = new AltDeployEventSpy( new LayoutParser() );
        Set<Artifact> result = spy.processArtifacts( projectPlugins );

        assertEquals( 1, result.size() );
        assertTrue ( mdp.getVersion().equalsIgnoreCase( "2.8" ));
        assertTrue ( mdp2.getVersion().equalsIgnoreCase( "2.8" ));
        assertTrue ( mdp3.getVersion().equalsIgnoreCase( "2.8" ));
    }
}
