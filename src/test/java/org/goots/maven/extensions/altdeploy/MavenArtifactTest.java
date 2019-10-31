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
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static junit.framework.TestCase.assertTrue;

public class MavenArtifactTest
{
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

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
        System.out.println ("### " + post3M1.getSelectedVersion().compareTo( post3version ) );
        assertTrue( post3M1.getSelectedVersion().compareTo( post3version ) > 0 );
    }
}