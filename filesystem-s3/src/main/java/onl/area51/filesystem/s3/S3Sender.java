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
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlaySender;

/**
 *
 * @author peter
 */
public class S3Sender
        extends AbstractS3Action
        implements OverlaySender
{

    private static final Logger LOG = Logger.getLogger( "S3" );

    public S3Sender( FileSystemIO delegate, Map<String, ?> env )
    {
        super( delegate, env );
    }

    @Override
    public void send( char[] path )
            throws IOException
    {
        String pathValue = String.valueOf( path );

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength( getDelegate().size( path ) );
        meta.setContentType( pathValue );

        try( InputStream is = getDelegate().newInputStream( path ) )
        {
            LOG.log( Level.INFO, () -> "Sending " + getBucketName() + ":" + pathValue );
            getS3().putObject( new PutObjectRequest( getBucketName(), pathValue, is, meta ) );
            LOG.log( Level.INFO, () -> "Sent " + getBucketName() + ":" + pathValue );
        } catch( AmazonS3Exception ex )
        {
            LOG.log( Level.INFO, () -> "Send error " + ex.getStatusCode() + " " + getBucketName() + ":" + pathValue );
            throw new IOException( ex.getStatusCode() + ": Failed to put " + pathValue, ex );
        } catch( IOException ex )
        {
            throw new IOException( "Failed to put " + pathValue, ex );
        }
    }

}
