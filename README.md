# filesystem
A suite of Java NIO FileSystem implementations

The currently supported file systems are:
* local - A local directory exposed as a FileSystem
* cache - an extension of local suitable for caching files.

For each FileSystem you can define:
* Where the filesystem is installed using the "baseDirectory" environment property. If this is not defined then it will place the filesystem under the users home directory (~/.area51/scheme/name where scheme is the filesystem scheme and name is it's name).
* If the "deleteOnExit" environment property exists then the filesystem will be deleted when it's closed or when the Java VM shuts down.

# local

The local FileSystem simply exposes a local directory as a FileSystem. This is useful if you want to keep an application from accessing files outside of that directory.

# cache

An alternative is cache which will store files within the directory in a more optimised manner to improve performance and disk usage.
