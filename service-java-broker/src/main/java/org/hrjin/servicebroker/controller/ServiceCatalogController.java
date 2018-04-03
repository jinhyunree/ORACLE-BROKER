package org.hrjin.servicebroker.controller;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.model.Catalog;
import org.hrjin.servicebroker.service.ServiceCatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by hrjin on 2018-03-26.
 */
@Controller
public class ServiceCatalogController extends BaseController{
    public static final String BASE_PATH = "/v2/catalog";

    private static final Logger logger = LoggerFactory.getLogger(ServiceCatalogController.class);

    private ServiceCatalogService service;

    @Autowired
    public ServiceCatalogController(ServiceCatalogService service) {
        this.service = service;
    }

    @RequestMapping(value = BASE_PATH, method = RequestMethod.GET)
    public @ResponseBody
    Catalog getCatalog() throws ServiceBrokerException {
        logger.info("GET: " + BASE_PATH + ", getCatalog()");
        return service.getCatalog();
    }
}
