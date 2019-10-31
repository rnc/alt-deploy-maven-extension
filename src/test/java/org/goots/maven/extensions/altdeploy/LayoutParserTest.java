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

import org.goots.maven.extensions.altdeploy.LayoutParser.Format;
import org.junit.Assert;
import org.junit.Test;

public class LayoutParserTest
{

    @Test
    public void testConvert() throws Exception
    {
        String altDeploymentRepository="local::default::file:///tmp/deploy";

        LayoutParser p = new LayoutParser();
        p.parse( altDeploymentRepository );

        Assert.assertSame( p.originalFormat(), Format.LEGACY );
        Assert.assertFalse( p.convert( Format.MODERN ).contains( "default" ) );
        Assert.assertSame( p.parse( p.convert( Format.MODERN ) ).originalFormat(), Format.MODERN );
    }
}