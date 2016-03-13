/*
 * Copyright 2016 peter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package onl.area51.filesystem.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.OverlayingFileSystemIO;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author peter
 */
@MetaInfServices(OverlayingFileSystemIO.class)
public class S3
        extends OverlayingFileSystemIO.Synchronous
{

    public static final String BUCKET = "bucket";

    private final AmazonS3 s3;
    private final String bucketName;

    public S3( FileSystemIO delegate, Map<String, ?> env )
    {
        super( delegate, Executors.newSingleThreadExecutor() );

        AWSCredentials credentials;
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        }
        catch( Exception ex ) {
            throw new AmazonClientException( "Cannot load the credentials from the credential profiles file. "
                                             + "Please make sure that your credentials file is at the correct "
                                             + "location (~/.aws/credentials), and is in valid format.",
                                             ex );
        }

        s3 = new AmazonS3Client( credentials );
        Region region = Region.getRegion( Regions.EU_WEST_1 );
        s3.setRegion( region );

        bucketName = Objects.requireNonNull( FileSystemUtils.get( env, BUCKET ), BUCKET + " is not defined" );
    }

    @Override
    protected void retrievePath( String path )
            throws IOException
    {
        try {
            S3Object obj = s3.getObject( new GetObjectRequest( bucketName, path ) );
            copyFromRemote( () -> obj.getObjectContent(), path.toCharArray() );
        }
        catch( AmazonS3Exception ex ) {
            String s = ex.getMessage();
            if( s != null && s.contains( "key does not exist" ) ) {
                throw new FileNotFoundException( path );
            }
            throw new IOException( s, ex );
        }
    }
}
