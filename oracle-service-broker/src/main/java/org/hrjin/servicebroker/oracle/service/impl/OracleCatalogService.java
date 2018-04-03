package org.hrjin.servicebroker.oracle.service.impl;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.model.Catalog;
import org.hrjin.servicebroker.model.ServiceDefinition;
import org.hrjin.servicebroker.service.ServiceCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hrjin on 2018-03-26.
 */
@Service
public class OracleCatalogService implements ServiceCatalogService {

    private Catalog catalog;
    private Map<String,ServiceDefinition> serviceDefs = new HashMap<String,ServiceDefinition>();
    private static final Logger logger = LoggerFactory.getLogger(OracleCatalogService.class);

    @Autowired
    public OracleCatalogService(Catalog catalog) {
        this.catalog = catalog;
        initializeMap();
    }

    private void initializeMap() {
        for (ServiceDefinition def: catalog.getServiceDefinitions()) {
            serviceDefs.put(def.getId(), def);
        }
    }

    @Override
    public Catalog getCatalog() throws ServiceBrokerException {
        return catalog;
    }

    @Override
    public ServiceDefinition getServiceDefinition(String serviceId) throws ServiceBrokerException {
        return serviceDefs.get(serviceId);
    }
}
