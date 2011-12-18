package org.longhorn.beanstalk.springintegration.config;

public class S3ResourceException extends RuntimeException {

    public S3ResourceException( String msg ) {
        super( msg );
    }
    public S3ResourceException( String msg, Throwable cause ) {
        super( msg, cause );
    }
}
