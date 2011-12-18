package org.longhorn.beanstalk.springintegration.config;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class S3ResourceLoadTest {

    private ApplicationContext context = null;

    @Before
    public void setUp() {
	
	if ( !areAWSCredentialsInSystemProperties() ) {
	    printOutTestSkipMessage();
	    assumeTrue( false );
	}
        System.clearProperty( "foo" );
    }

    private boolean areAWSCredentialsInSystemProperties() {
	
	return "${aws.credentials.accessKey}".equals( System.getProperty("AWS_ACCESS_KEY_ID") ) ||
	    "${aws.credentials.secretKey}".equals( System.getProperty( "AWS_SECRET_KEY" ) );
    }

    private void printOutTestSkipMessage() {

	System.out.println( "++++++++++++++++++++++++++++++++++++++++++++++" );
	System.out.println( "+ No AWS credentails found, skip tests       +" );
	System.out.println( "++++++++++++++++++++++++++++++++++++++++++++++" );
    }
    @Test
    public void testLoadPropertiesFromS3() {
        context = new ClassPathXmlApplicationContext( new String[] { "testContext.xml" } );
        TestBean testBean = (TestBean) context.getBean( "testBean" );
        assertEquals( "bar", testBean.getFoo() );
    }

    @Test
    public void testLoadPropertiesFromLocations() {
        context = new ClassPathXmlApplicationContext( new String[] { "load_from_locations_context.xml" } );
        TestBean testBean = (TestBean) context.getBean( "testBean" );
        assertEquals( "bar", testBean.getFoo() );
    }

    @Test
    public void testLoadPropertiesFromEmbeddedProperties() {
        context = new ClassPathXmlApplicationContext( new String[] { "load_from_embeded_properties_context.xml" } );
        TestBean testBean = (TestBean) context.getBean( "testBean" );
        assertEquals( "bar", testBean.getFoo() );
    }

    @Test
    public void testFetchS3LocationsFromSystemProperties() {
        System.setProperty( "s3locations", "s3://com-ea2d-tmp/s3.properties" );
        context = new ClassPathXmlApplicationContext( new String[] { "s3location_from_system_properties_context.xml" } );
        TestBean testBean = (TestBean) context.getBean( "testBean" );
        assertEquals( "bar", testBean.getFoo() );
    }

    @Test
    public void testSystemOverride() {
        System.setProperty( "foo", "world" );
        context = new ClassPathXmlApplicationContext( new String[] { "testContext.xml" } );
        TestBean testBean = (TestBean) context.getBean( "testBean" );
        assertEquals( "world", testBean.getFoo() );
    }
    
}
