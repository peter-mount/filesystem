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

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import onl.area51.filesystem.FileSystemUtils;
import onl.area51.filesystem.io.FileSystemIO;
import org.xmlpull.v1.XmlPullParserException;

/**
 *
 * @author peter
 */
public abstract class AbstractMinioAction
{

    public static final String BUCKET = "bucket";
    public static final String BUCKET_READ = "bucket.read";
    private final FileSystemIO delegate;
    private final MinioClient minioClient;
    private final String bucketName;

    public AbstractMinioAction( FileSystemIO delegate, Map<String, ?> env )
    {
        try {
            this.delegate = delegate;

            minioClient = new MinioClient( FileSystemUtils.getString( env, "endpoint", () -> System.getenv( "MINIO_ENDPOINT" ) ),
                                           FileSystemUtils.getString( env, "accessKey", () -> System.getenv( "MINIO_ACCESS_KEY" ) ),
                                           FileSystemUtils.getString( env, "secretKey", () -> System.getenv( "MINIO_SECRET_KEY" ) ) );

            String n = FileSystemUtils.get( env, BUCKET_READ );
            if( n == null || n.trim().isEmpty() ) {
                n = FileSystemUtils.get( env, BUCKET );
            }
            bucketName = Objects.requireNonNull( n, BUCKET + " or " + BUCKET_READ + " is not defined" );

            if( !minioClient.bucketExists( bucketName ) ) {
                // Implicit create=true must be present
                if( !FileSystemUtils.isTrue( env, "create" ) ) {
                    throw new IllegalArgumentException( "Bucket " + bucketName + " not found" );
                }

                minioClient.makeBucket( bucketName );
            }
        }
        catch( MinioException |
               NoSuchAlgorithmException |
               InvalidKeyException |
               XmlPullParserException |
               IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    protected final FileSystemIO getDelegate()
    {
        return delegate;
    }

    protected final String getBucketName()
    {
        return bucketName;
    }

    protected final MinioClient getMinioClient()
    {
        return minioClient;
    }

}
