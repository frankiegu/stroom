package stroom.startup;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import stroom.proxy.guice.ProxyConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Config extends Configuration {
    private String mode;
    private ProxyConfig proxyConfig;
    private String externalConfig = "~/.stroom/stroom.conf";

    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @JsonProperty("jerseyClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }

    @JsonProperty("jerseyClient")
    public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

    @JsonProperty
    public String getMode() {
        return mode;
    }

    @JsonProperty
    public void setMode(final String mode) {
        this.mode = mode;
    }

    @JsonProperty
    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    @JsonProperty
    public void setProxyConfig(final ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @JsonProperty
    public String getExternalConfig() {
        return externalConfig;
    }

    @JsonProperty
    public void setExternalConfig(final String externalConfig) {
        this.externalConfig = externalConfig;
    }

}
