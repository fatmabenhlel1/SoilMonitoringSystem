package me.soilmonitoring.iam;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.logging.Logger;

@ApplicationPath("/rest-iam")
public class IamApplication extends Application {
    @ApplicationScoped
    public static final class CDIConfigurator {  /*pour configurer l'application*/
        @Inject
        @ConfigProperty(name = "jwt.realm") /*variable d'environnement JWT_REALM*/
        private String realm;

        @Produces
        @Named(value = "realm")    /*bbd de securité realm*/
        public String getRealm(){
            return realm;
        }

        @Produces
        @Dependent
        public Logger getLogger(InjectionPoint injectionPoint){
            return Logger.getLogger(injectionPoint.getBean().getBeanClass().getName());
        }

        public void disposeLogger(@Disposes Logger logger){
            logger.info("logger disposed!");
        }
    }
}
