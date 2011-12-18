package org.longhorn.beanstalk.springintegration.config;

import java.io.InputStream;

import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;

public class S3ResourceLoader implements ResourceLoader {

    private static final String LOCATION_PREFIX = "s3://";
    
    public static class S3Path {
        public String bucket;
        public String key;
    };

    private String awsAccessKey;
    private String awsSecretKey;
    private AWSCredentials credentials;
    private S3Service s3Service;
    public S3ResourceLoader( ) {
        this.awsAccessKey = getRequiredSystemProperty( "AWS_ACCESS_KEY_ID" );
        this.awsSecretKey = getRequiredSystemProperty( "AWS_SECRET_KEY" );
        this.credentials = new AWSCredentials( awsAccessKey, awsSecretKey );
        try {
            this.s3Service = new RestS3Service( credentials );
        } catch( S3ServiceException e ) {
            throw new S3ResourceException( "could not initialize s3 service", e );
        }
    }
    public ClassLoader getClassLoader() {
        return this.getClassLoader();
    }

    public Resource getResource( String location ) {
        try {
            S3Path s3Path = parseS3Path( location );
            S3Object s3Object = s3Service.getObject( s3Path.bucket, s3Path.key );
            byte[] buf = readS3Object( s3Object );
            return new ByteArrayResource( buf , location );

        } catch ( Exception e ) {
            throw new S3ResourceException( "could not load resource from " + location, e );
        }
    }

    private String getRequiredSystemProperty( String propertyName ) {
        String value = System.getProperty( propertyName );
        if ( value == null || "".equals( value.trim() ) ) {
            throw new S3ResourceException( "no " + propertyName + " property found in system" );
        }
        return value;
    }

    private S3Path parseS3Path( String location ) {

        String path = getLocationPath( location );
        int indexOfSlash = path.lastIndexOf( "/" );
        S3Path s3Path = new S3Path( );
        s3Path.bucket = path.substring( 0, indexOfSlash );
        s3Path.key = path.substring( indexOfSlash + 1, path.length() );
        return s3Path;
    }

    private String getLocationPath( String location ) {

        if ( location == null || "".equals( location.trim() ) ) {
            throw new S3ResourceException( "location cannot be empty or null" );
        }

        String resolvedLocation = location;

        if ( ! resolvedLocation.startsWith( LOCATION_PREFIX ) ) {
            throw new S3ResourceException( resolvedLocation + " does not begin with " + LOCATION_PREFIX );
        }
        
        return resolvedLocation.substring( LOCATION_PREFIX.length(), resolvedLocation.length() );
        
    }
    private byte[] readS3Object( S3Object s3Object ) throws Exception {
        InputStream inputStream = s3Object.getDataInputStream();
        int size = 1024;
        byte[] buf = new byte[ size ];
        
        int readedSize = inputStream.read( buf );
        while ( readedSize == 1024 ) {
            byte[] tmpBuf = new byte[ size ];
            readedSize = inputStream.read( tmpBuf );
            byte[] newBuf = new byte[ buf.length + readedSize ];
            System.arraycopy( buf, 0, newBuf, 0, buf.length );
            System.arraycopy( tmpBuf, 0, newBuf, buf.length, readedSize );
            buf = newBuf;
        }
        return buf;
    }
}
