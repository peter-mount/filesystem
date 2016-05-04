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
package onl.area51.filesystem.minio;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InputSizeMismatchException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.NoResponseException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.io.FileSystemIO;
import onl.area51.filesystem.io.overlay.OverlaySender;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author peter
 */
public class MinioSender
        extends AbstractMinioAction
        implements OverlaySender
{

    private static final Logger LOG = Logger.getLogger( "Minio" );

    public MinioSender( FileSystemIO delegate, Map<String, ?> env )
    {
        super( delegate, env );
    }

    @Override
    public void send( char[] path )
            throws IOException
    {
        String pathValue = String.valueOf( path );

        try( InputStream is = getDelegate().newInputStream( path ) ) {
            LOG.log( Level.FINE, () -> "Sending " + getBucketName() + ":" + pathValue );

            getMinioClient().putObject( getBucketName(), pathValue, is, getDelegate().size( path ), "application/binary" );
            LOG.log( Level.FINE, () -> "Sent " + getBucketName() + ":" + pathValue );
        }
        catch( InvalidBucketNameException |
               NoSuchAlgorithmException |
               InsufficientDataException |
               InvalidKeyException |
               NoResponseException |
               XmlPullParserException |
               ErrorResponseException |
               InternalException |
               InvalidArgumentException |
               InputSizeMismatchException ex ) {
            throw new IOException( ex );
        }
    }

}
