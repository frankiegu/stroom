/*
 * Copyright 2017 Crown Copyright
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

package stroom.dashboard.shared;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "searchRequest", propOrder = {"search", "componentResultRequests", "dateTimeLocale"})
@XmlRootElement(name = "searchRequest")
public class SearchRequest implements Serializable {
    private static final long serialVersionUID = -6668626615097471925L;

    @XmlElement
    private Search search;
    @XmlElement
    private Map<String, ComponentResultRequest> componentResultRequests;
    @XmlElement
    private String dateTimeLocale;

    public SearchRequest() {
        // Default constructor necessary for GWT serialisation.
    }

    public SearchRequest(final Search search, final Map<String, ComponentResultRequest> componentResultRequests, final String dateTimeLocale) {
        this.search = search;
        this.componentResultRequests = componentResultRequests;
        this.dateTimeLocale = dateTimeLocale;
    }

    public Search getSearch() {
        return search;
    }

    public Map<String, ComponentResultRequest> getComponentResultRequests() {
        return componentResultRequests;
    }

    public String getDateTimeLocale() {
        return dateTimeLocale;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SearchRequest that = (SearchRequest) o;

        if (search != null ? !search.equals(that.search) : that.search != null) return false;
        if (componentResultRequests != null ? !componentResultRequests.equals(that.componentResultRequests) : that.componentResultRequests != null)
            return false;
        return dateTimeLocale != null ? dateTimeLocale.equals(that.dateTimeLocale) : that.dateTimeLocale == null;
    }

    @Override
    public int hashCode() {
        int result = search != null ? search.hashCode() : 0;
        result = 31 * result + (componentResultRequests != null ? componentResultRequests.hashCode() : 0);
        result = 31 * result + (dateTimeLocale != null ? dateTimeLocale.hashCode() : 0);
        return result;
    }
}
