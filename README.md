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

This FileSystem accepts a "fileSystemType" environment property which defines the layout of the cache.
* cache - the default, the local filenames are based on the md5 of the filename
* flat - emulates the local FileSystem
* mediawiki - emulates how MediaWiki stores images
* opendata - emulates how the OpenData CMS stores it's pages

How entries are expired in caches are also configurable:
* maxAge defines the max age in milliseconds that a file in the cache can exist before it's expired. If this is 0 (default) then there is no expiry.
* scanDelay defines the period between expiry checks. If not present it defaults to maxAge. If maxAge is 0 then this has no effect.
* expireOnStartup will run an expiry on the cache immediately the filesystem is opened. If maxAge is 0 then this has no effect.
* clearOnStartup will clear the cache of all files when the filesystem is opened.
