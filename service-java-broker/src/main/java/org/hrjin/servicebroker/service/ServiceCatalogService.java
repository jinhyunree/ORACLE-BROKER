package org.hrjin.servicebroker.service;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.model.Catalog;
import org.hrjin.servicebroker.model.ServiceDefinition;

/**
 * Created by hrjin on 2018-03-26.
 */
public interface ServiceCatalogService {
    /**
     * @return The catalog of services provided by this broker.
     */
    Catalog getCatalog() throws ServiceBrokerException;

    /**
     * @param serviceId  The id of the service in the catalog
     * @return The service definition or null if it doesn't exist
     */
    ServiceDefinition getServiceDefinition(String serviceId) throws ServiceBrokerException;
}
