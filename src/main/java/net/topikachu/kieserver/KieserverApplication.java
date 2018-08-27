package net.topikachu.kieserver;

import org.jboss.jca.adapters.AdaptersBundle;
import org.jboss.jca.embedded.Embedded;
import org.jboss.jca.embedded.EmbeddedFactory;
import org.jboss.jca.embedded.dsl.datasources13.api.DatasourcesDescriptor;
import org.jboss.jca.embedded.dsl.datasources13.api.XaDatasourceType;
import org.jboss.jca.embedded.dsl.datasources13.impl.DatasourcesDescriptorImpl;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.ResourceAdapterArchive;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.services.impl.security.web.CaptureHttpRequestFilter;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@SpringBootApplication
public class KieserverApplication {


    public static void main(String[] args) throws Throwable {

        InputStream inputStream = KieserverApplication.class.getClassLoader().getResourceAsStream("logging.properties");
        if (null != inputStream) {
            try {
                LogManager.getLogManager().readConfiguration(inputStream);
            } catch (IOException e) {
                Logger.getGlobal().log(Level.SEVERE, "init logging system", e);
            }
        }

        //bring up the kie
        System.setProperty("org.kie.server.startup.strategy", "LocalContainersStartupStrategy"); //Can change when have controller
        System.setProperty("jbpm.tm.jndi.lookup", "java:/TransactionManager");
        System.setProperty("jbpm.tsr.jndi.lookup", "java:/TransactionSynchronizationRegistry");
        System.setProperty("jbpm.ut.jndi.lookup", "java:/UserTransaction");

        //make mysql happy
        System.setProperty("ironjacamar.no_delist_resource_all", "true");

        System.setProperty("org.kie.server.persistence.ds", "java:/jbpm");
        System.setProperty("org.kie.server.persistence.tm", "net.topikachu.kieserver.IronJacamarJtaPlatform");

        Embedded embedded = EmbeddedFactory.create();
        //embedded.
        embedded.startup();

        ResourceAdapterArchive raa =
                ShrinkWrap.create(ResourceAdapterArchive.class, "jdbc-xa.rar");
        JavaArchive ja = ShrinkWrap.create(JavaArchive.class, UUID.randomUUID().toString() + ".jar");
        ja.addPackages(true, AdaptersBundle.class.getPackage());

        raa.addAsLibrary(ja);
        raa.addAsManifestResource("jdbc-ra.xml", "ra.xml");
        embedded.deploy(raa);


        DatasourcesDescriptor xaDsXml = new DatasourcesDescriptorImpl("jbpm-xa-ds.xml");
        XaDatasourceType<DatasourcesDescriptor> xaDatasource = xaDsXml.createXaDatasource();


        xaDatasource.jndiName("java:/jbpm")
                .poolName("jbpm")
                .xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
        xaDatasource.createXaDatasourceProperty()
                .name("url").text("jdbc:h2:mem:jbpm");
        xaDatasource.createXaDatasourceProperty()
                .name("user").text("sa");
        xaDatasource.createXaDatasourceProperty()
                .name("password").text("sa");
        xaDatasource.getOrCreateXaPool()
                .initialPoolSize(5)
                .minPoolSize(5);



        embedded.deploy(xaDsXml);


        String serverId = "kieserver";


        KieServerStateFileRepository repository = new KieServerStateFileRepository();
        KieServerState currentState = repository.load(serverId);

        repository.store(serverId, currentState);

        KieServerEnvironment.setServerName(serverId);
        KieServerEnvironment.setServerId(serverId);



        SpringApplication.run(KieserverApplication.class, args);

    }


    @Bean
    FilterRegistrationBean captureHttpRequestFilter() {
        FilterRegistrationBean captureHttpRequestFilter = new FilterRegistrationBean(new CaptureHttpRequestFilter());
        captureHttpRequestFilter.addUrlPatterns("/*");
        return captureHttpRequestFilter;
    }



    @Bean
    ServletRegistrationBean resteasy() {
        ServletRegistrationBean resteasy = new ServletRegistrationBean(new HttpServletDispatcher());
        resteasy.addUrlMappings("/services/rest/*");
        resteasy.addInitParameter("javax.ws.rs.Application", "org.kie.server.remote.rest.common.KieServerApplication");
        resteasy.setLoadOnStartup(1);
        return resteasy;
    }
}
