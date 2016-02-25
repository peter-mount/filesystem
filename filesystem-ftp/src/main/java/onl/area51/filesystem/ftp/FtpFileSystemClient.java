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
package onl.area51.filesystem.ftp;

import java.io.Closeable;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import onl.area51.filesystem.FileSystemUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * An FTP client used by a filesystem
 *
 * @author peter
 */
public class FtpFileSystemClient
        implements Closeable
{

    private static final Logger LOG = Logger.getLogger( FtpFileSystemClient.class.getName() );

    private final Map<String, ?> env;
    private final boolean useEpsvWithIPv4;
    private final boolean localActive;
    private final boolean binaryTransfer;
    private final boolean debuggingEnabled;
    private final boolean listHiddenFiles;
    private final long keepAliveTimeout;
    private final int controlKeepAliveReplyTimeout;
    private final String server;
    private final int port;
    private final String user;
    private final String password;

    private FTPClient client;
    private boolean loggedIn;

    private ExecutorService service;

    public FtpFileSystemClient( Map<String, ?> env )
    {
        this.env = env;

        keepAliveTimeout = FileSystemUtils.getLong( env, "ftp.keepAliveTimeout", 0l );
        controlKeepAliveReplyTimeout = (int) FileSystemUtils.getLong( env, "ftp.controlKeepAliveReplyTimeout", 0L );
        useEpsvWithIPv4 = FileSystemUtils.isTrue( env, "ftp.useEpsvWithIPv4" );
        localActive = FileSystemUtils.isTrue( env, "ftp.localActive" );
        binaryTransfer = !FileSystemUtils.isFalse( env, "ftp.binaryTransfer" );
        debuggingEnabled = FileSystemUtils.isTrue( env, "ftp.debuggingEnabled" );
        listHiddenFiles = FileSystemUtils.isTrue( env, "listHiddenFiles" );

        server = Objects.requireNonNull( FileSystemUtils.getString( env, "ftp.server" ), "ftp.server is required" );
        port = (int) FileSystemUtils.getLong( env, "ftp.port", 0L );
        user = FileSystemUtils.getString( env, "ftp.user", null );
        password = FileSystemUtils.getString( env, "ftp.password", null );

    }

    @Override
    public void close()
            throws IOException
    {
        try {
            disconnect();
        }
        finally {
            service.shutdownNow();
        }
    }

    public void disconnect()
            throws IOException
    {
        try {
            if( client != null && client.isConnected() ) {
                try {
                    if( loggedIn ) {
                        client.logout();
                    }
                }
                finally {
                    loggedIn = false;

                    client.disconnect();
                }
            }
        }
        finally {
            client = null;
        }
    }

    private void connect()
            throws IOException
    {
        if( client == null ) {
            // For now no proxy support
            client = new FTPClient();

            if( keepAliveTimeout >= 0 ) {
                client.setControlKeepAliveTimeout( keepAliveTimeout );
            }

            if( controlKeepAliveReplyTimeout >= 0 ) {
                client.setControlKeepAliveReplyTimeout( controlKeepAliveReplyTimeout );
            }

            client.setListHiddenFiles( listHiddenFiles );
        }

        if( !client.isConnected() ) {

            LOG.log( Level.INFO, () -> "Connecting to " + server + " on " + (port > 0 ? port : client.getDefaultPort()) );

            if( port > 0 ) {
                client.connect( server, port );
            }
            else {
                client.connect( server );
            }

            LOG.log( Level.INFO, () -> "Connected to " + server + " on " + (port > 0 ? port : client.getDefaultPort()) );

            // After connection attempt, you should check the reply code to verifysuccess.
            if( !FTPReply.isPositiveCompletion( client.getReplyCode() ) ) {
                LOG.log( Level.INFO, () -> "Connection refused to " + server + " on " + (port > 0 ? port : client.getDefaultPort()) );
                throw new ConnectException( "Failed to connect to " + server );
            }
        }

        if( !loggedIn ) {
            if( !client.login( user, password ) ) {
                throw new ConnectException( "Failed to login" );
            }

            if( binaryTransfer ) {
                client.setFileType( org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE );
            }
            else {
                // in theory this should not be necessary as servers should default to ASCII but they don't all do so - see NET-500
                client.setFileType( org.apache.commons.net.ftp.FTP.ASCII_FILE_TYPE );
            }

            // Use passive mode as default because most of us are
            // behind firewalls these days.
            if( localActive ) {
                client.enterLocalActiveMode();
            }
            else {
                client.enterLocalPassiveMode();
            }

            client.setUseEPSVwithIPv4( useEpsvWithIPv4 );
            loggedIn = true;
        }
    }
}
