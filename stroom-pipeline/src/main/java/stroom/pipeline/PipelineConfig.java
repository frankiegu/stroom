package stroom.pipeline;

import com.fasterxml.jackson.annotation.JsonProperty;
import stroom.pipeline.destination.AppenderConfig;
import stroom.pipeline.filter.XmlSchemaConfig;
import stroom.pipeline.filter.XsltConfig;
import stroom.pipeline.refdata.ReferenceDataConfig;
import stroom.util.cache.CacheConfig;
import stroom.util.shared.IsConfig;
import stroom.util.xml.ParserConfig;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Singleton
public class PipelineConfig implements IsConfig {
    private AppenderConfig appenderConfig = new AppenderConfig();
    private ParserConfig parserConfig = new ParserConfig();
    private ReferenceDataConfig referenceDataConfig = new ReferenceDataConfig();
    private XmlSchemaConfig xmlSchemaConfig = new XmlSchemaConfig();
    private XsltConfig xsltConfig = new XsltConfig();
    private CacheConfig pipelineDataCache = new CacheConfig.Builder()
            .maximumSize(1000L)
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @JsonProperty("appender")
    public AppenderConfig getAppenderConfig() {
        return appenderConfig;
    }

    public void setAppenderConfig(final AppenderConfig appenderConfig) {
        this.appenderConfig = appenderConfig;
    }

    @JsonProperty("parser")
    public ParserConfig getParserConfig() {
        return parserConfig;
    }

    public void setParserConfig(final ParserConfig parserConfig) {
        this.parserConfig = parserConfig;
    }

    @JsonProperty("referenceData")
    public ReferenceDataConfig getReferenceDataConfig() {
        return referenceDataConfig;
    }

    public void setReferenceDataConfig(final ReferenceDataConfig referenceDataConfig) {
        this.referenceDataConfig = referenceDataConfig;
    }

    @JsonProperty("xmlSchema")
    public XmlSchemaConfig getXmlSchemaConfig() {
        return xmlSchemaConfig;
    }

    public void setXmlSchemaConfig(final XmlSchemaConfig xmlSchemaConfig) {
        this.xmlSchemaConfig = xmlSchemaConfig;
    }

    @JsonProperty("xslt")
    public XsltConfig getXsltConfig() {
        return xsltConfig;
    }

    public void setXsltConfig(final XsltConfig xsltConfig) {
        this.xsltConfig = xsltConfig;
    }

    public CacheConfig getPipelineDataCache() {
        return pipelineDataCache;
    }

    public void setPipelineDataCache(final CacheConfig pipelineDataCache) {
        this.pipelineDataCache = pipelineDataCache;
    }
}
