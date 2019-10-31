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