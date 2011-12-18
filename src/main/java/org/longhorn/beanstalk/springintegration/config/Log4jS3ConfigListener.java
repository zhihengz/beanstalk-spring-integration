package org.longhorn.beanstalk.springintegration.config;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.core.io.Resource;
import org.springframework.web.util.Log4jWebConfigurer;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.apache.log4j.PropertyConfigurator;

public class Log4jS3ConfigListener implements ServletContextListener {

    private static final String 
        CONFIG_LOCATION_PARAM = "log4jS3ConfigLocation";

    private boolean fallback = true;
    public void contextInitialized( ServletContextEvent event ) {

        ServletContext servletContext = event.getServletContext();
        String location = servletContext.getInitParameter( CONFIG_LOCATION_PARAM );
        if ( location != null ) {
            try {
                fallback = false;
                loadPropertiesFromS3( location );
            } catch( Exception e ) {
                fallback = true;
                servletContext.log( "failed to load log4j properties from s3", e );
            }
        }
        if ( fallback ) {
            Log4jWebConfigurer.initLogging( event.getServletContext() );
        }
    }

    public void contextDestroyed( ServletContextEvent event ) {

        if ( fallback ) {
            Log4jWebConfigurer.shutdownLogging( event.getServletContext() );
        }
    }

    private void loadPropertiesFromS3( String location ) {

        S3ResourceLoader s3ResourceLoader = new S3ResourceLoader();
        String resolvedLocation = parseStringValue( location );
        Resource resource = s3ResourceLoader.getResource( resolvedLocation );
        Properties properties = new Properties();
        try {
            properties.load( resource.getInputStream() );
        } catch( IOException e ) {
            throw new S3ResourceException( "could not load log4j properties from " + resolvedLocation, e );
        }
        PropertyConfigurator.configure( properties );
        
    }
    private String parseStringValue( String strVal ) {
        PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper("${", "}", ":", false);
        PlaceholderResolver resolver = new SimpleSystemPropertyPlaceholderResolver();
        return helper.replacePlaceholders(strVal, resolver);
    }

    private class SimpleSystemPropertyPlaceholderResolver implements PlaceholderResolver {
        
        public String resolvePlaceholder( String placeholderName ) {
            String value = System.getProperty( placeholderName );
            if ( value == null ) {
                throw new S3ResourceException( "could not resolve " + placeholderName );
            }
            return value;
        }
    }
}
