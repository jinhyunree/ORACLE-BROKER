package org.hrjin.servicebroker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.hrjin.servicebroker.exception.ServiceBrokerException;
import org.hrjin.servicebroker.oracle.config.ServiceCatalogConfig;
import org.hrjin.servicebroker.oracle.service.impl.OracleCatalogService;
import org.hrjin.servicebroker.service.ServiceCatalogService;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.openpaas.servicebroker.service.CatalogService;


/**
 * 시작전에 Spring Boot로 Service Broker를 띄워 놓구 진행합니다.
 * 향후에 Spring Configuration?으로 서비스를 시작하게 만들 예정
 *
 * @author ahnchan
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServiceCatalogServiceTest {

    private static Properties prop = new Properties();
    ServiceCatalogConfig cc = new ServiceCatalogConfig();
    private ServiceCatalogService service = new OracleCatalogService(cc.catalog());

    @BeforeClass
    public static void init() {

        System.out.println("== Started test Catalog API ==");

        // Initialization
        // Get properties information
        String propFile = "test.properties";

        InputStream inputStream = ServiceCatalogServiceTest.class.getClassLoader().getResourceAsStream(propFile);

        try {
            prop.load(inputStream);
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

    }


    @Test
    public void getCatalog() throws ServiceBrokerException {
        System.out.println("== Started test Catalog API ==");
        try {
            service.getCatalog();
            service.getServiceDefinition("test");
        } catch (Exception e) {
            // TODO: handle exception
        }
        System.out.println("== Started test Catalog API ==");
        System.out.println("@@@@@@@@@@@@@@ : " + service.toString());
    }

}
