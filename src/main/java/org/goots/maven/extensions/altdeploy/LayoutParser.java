package org.goots.maven.extensions.altdeploy;

import org.apache.maven.artifact.deployer.ArtifactDeploymentException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.goots.maven.extensions.altdeploy.LayoutParser.Format.LEGACY;
import static org.goots.maven.extensions.altdeploy.LayoutParser.Format.MODERN;

@SuppressWarnings( "WeakerAccess" )
public class LayoutParser
{
    private static final Pattern ALT_REPO_SYNTAX_PATTERN_LEGACY = Pattern.compile( "(.+)::(.+)::(.+)" );

    private static final Pattern ALT_REPO_SYNTAX_PATTERN_MODERN = Pattern.compile( "(.+)::(.+)" );


    private String id;
    private String layout = "default";
    private String url;
    private Format format;

    public LayoutParser parse (String repo) throws ArtifactDeploymentException
    {
        Matcher matcher = ALT_REPO_SYNTAX_PATTERN_LEGACY.matcher( repo );

        if ( !matcher.matches() )
        {
            matcher = ALT_REPO_SYNTAX_PATTERN_MODERN.matcher( repo );

            if ( !matcher.matches() )
            {
                throw new ArtifactDeploymentException( "Unable to parse altDeploymentRepository with contents "  );
            }
            else
            {
                format = MODERN;
                id = matcher.group( 1 ).trim();
                url = matcher.group( 2 ).trim();
            }
        }
        else
        {
            format = LEGACY;
            id = matcher.group( 1 ).trim();
            layout = matcher.group( 2 ).trim();
            url = matcher.group( 3 ).trim();
        }

        return this;
    }

    public Format originalFormat()
    {
        return format;
    }

    /**
     * Return the repository in the desired format.
     *
     * @param requested, the desired repository format
     * @return the equivalent repository format.
     */
    public String convert (Format requested)
    {
        if ( requested == MODERN)
        {
            return id + "::" + url;
        }
        else
        {
            return id + "::" + layout + "::" + url;
        }
    }


    public enum Format {
        LEGACY,
        MODERN
    }
}
