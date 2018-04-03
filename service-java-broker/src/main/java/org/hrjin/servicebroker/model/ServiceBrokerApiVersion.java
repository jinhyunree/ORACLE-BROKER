package org.hrjin.servicebroker.model;

/**
 * Created by hrjin on 2018-03-26.
 */
public class ServiceBrokerApiVersion {
    public final static String DEFAULT_API_VERSION_HEADER = "X-Broker-Api-Version";
    public final static String API_VERSION_ANY = "*";

    private String brokerApiVersionHeader;
    private String apiVersions;

    public ServiceBrokerApiVersion(String brokerApiVersionHeader, String apiVersions) {
        this.brokerApiVersionHeader = brokerApiVersionHeader;
        this.apiVersions = apiVersions;
    }

    public ServiceBrokerApiVersion(String apiVersions) {
        this(DEFAULT_API_VERSION_HEADER, apiVersions);
    }

    public ServiceBrokerApiVersion() {
        this(DEFAULT_API_VERSION_HEADER, API_VERSION_ANY);
    }

    public String getBrokerApiVersionHeader() {
        return brokerApiVersionHeader;
    }

    public String getApiVersions() {
        return apiVersions;
    }
}
