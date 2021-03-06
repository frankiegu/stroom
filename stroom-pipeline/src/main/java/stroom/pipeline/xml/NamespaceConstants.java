/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.pipeline.xml;

/**
 * Namespace constants used by Stroom.
 */
public final class NamespaceConstants {
    /**
     * This namespace is used for records created by the XML converter.
     */
    public static final String RECORDS = "records:2";
    /**
     * This namespace is used for Stroom XSLT functions.
     */
    public static final String STROOM = "stroom";

    private NamespaceConstants() {
        // Constants class so not instantiable.
    }
}
