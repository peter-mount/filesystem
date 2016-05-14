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
package onl.area51.filesystem.dpkg;

interface Constants
{
static final String REFRESH_PACKAGES_ON_STARTUP = "refreshPackagesOnStartup";

    static final String GZ = ".gz";
    static final String PACKAGES = "Packages";
    static final String PACKAGES_GZ = PACKAGES + GZ;
    static final String DEB = ".deb";
    static final String FAILED_REFRESH_OF = "Failed refresh of ";
    static final String COMPLETED_REFRESH_OF = "Completed refresh of ";
    static final String BEGINING_REFRESH_OF = "Begining refresh of ";

    // buffer size used for reading and writing
    static final int BUFFER_SIZE = 8192;

}
