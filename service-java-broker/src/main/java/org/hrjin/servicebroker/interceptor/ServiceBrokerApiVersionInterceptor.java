package org.hrjin.servicebroker.interceptor;

import org.hrjin.servicebroker.exception.ServiceBrokerApiVersionException;
import org.hrjin.servicebroker.model.ServiceBrokerApiVersion;
import org.slf4j.Logger;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by hrjin on 2018-03-26.
 */
public class ServiceBrokerApiVersionInterceptor extends HandlerInterceptorAdapter {

    private final ServiceBrokerApiVersion version;

    public ServiceBrokerApiVersionInterceptor() {
        this(null);
    }

    private static final Logger LOGGER = getLogger(ServiceBrokerApiVersionInterceptor.class);
    public ServiceBrokerApiVersionInterceptor(ServiceBrokerApiVersion version) {
        this.version = version;
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws ServiceBrokerApiVersionException {
        if (version != null && !anyVersionAllowed()) {
            String apiVersion = request.getHeader(version.getBrokerApiVersionHeader());
            boolean contains = false;
            for (String brokerApiVersion : version.getApiVersions().split(", ")) {
                if(brokerApiVersion.contains(".") &&  apiVersion.contains(".")){
                    if("x".equals(brokerApiVersion.split("[.]")[1]) && apiVersion.split("[.]")[0].equals(brokerApiVersion.split("[.]")[0])){
                        contains = true;
                        break;
                    }
                }
                if (brokerApiVersion.equals(apiVersion)){
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                throw new ServiceBrokerApiVersionException(version.getApiVersions(), apiVersion);
            }

        }
        return true;
    }

    private boolean anyVersionAllowed() {
        return ServiceBrokerApiVersion.API_VERSION_ANY.equals(version.getApiVersions());
    }

}
