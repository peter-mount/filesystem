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
import java.util.Map;
import java.util.Objects;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;

/**
 *
 * @author peter
 */
public abstract class AbstractS3Action
{

    public static final String BUCKET = "bucket";
    public static final String BUCKET_READ = "bucket.read";
    private final FileSystemIO delegate;
    private final AmazonS3 s3;
    private final String bucketName;

    public AbstractS3Action( FileSystemIO delegate, Map<String, ?> env )
    {
        this.delegate = delegate;

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

        String n = FileSystemUtils.get( env, BUCKET_READ );
        if( n == null || n.trim().isEmpty() ) {
            n = FileSystemUtils.get( env, BUCKET );
        }
        bucketName = Objects.requireNonNull( n, BUCKET + " or " + BUCKET_READ + " is not defined" );
    }

    protected final FileSystemIO getDelegate()
    {
        return delegate;
    }

    protected final String getBucketName()
    {
        return bucketName;
    }

    protected final AmazonS3 getS3()
    {
        return s3;
    }

}
