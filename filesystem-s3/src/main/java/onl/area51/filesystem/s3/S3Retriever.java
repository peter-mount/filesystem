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

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlayRetriever;

/**
 *
 * @author peter
 */
public class S3Retriever
        extends AbstractS3Action
        implements OverlayRetriever
{

    private static final Logger LOG = Logger.getLogger( "S3" );

    public S3Retriever( FileSystemIO delegate, Map<String, ?> env )
    {
        super( delegate, env );
    }

    @Override
    public void retrieve( char[] path )
            throws IOException
    {
        String pathValue = String.valueOf( path );
        try
        {
            LOG.log( Level.FINE, () -> "Retrieving " + getBucketName() + ":" + pathValue );
            S3Object obj = getS3().getObject( new GetObjectRequest( getBucketName(), pathValue ) );
            FileSystemUtils.copyFromRemote( () -> obj.getObjectContent(), getDelegate(), path );
            LOG.log( Level.FINE, () -> "Retrieved " + getBucketName() + ":" + pathValue );
        } catch( AmazonS3Exception ex )
        {
            LOG.log( Level.FINE, () -> "Error " + ex.getStatusCode() + " " + getBucketName() + ":" + pathValue );
            if( ex.getStatusCode() == 404 )
            {
                throw new FileNotFoundException( pathValue );
            }
            throw new IOException( "Cannot access " + pathValue, ex );
        }
    }
}
