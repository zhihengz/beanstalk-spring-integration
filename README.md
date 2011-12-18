# Beanstalk Spring Integration

plumber work on help using springframework in beanstalk environment

## Loading properties from amazon S3 buckets

A typic beanstalk webapp will use spring webframework to wire up the context
of the application. Usually it involves set configuration values in
application context xml, such as database credentials, dependent service 
endpoints. 

One way to resolve this is to either directly set values in spring context or 
use properties files within classpath. However the idea that embeds 
configuration within application itself is generally not flexible. Considered
an application may have different deployment environment such as production,
integration and development. In each environment, it is desire and sometime
even must have different configuartion values.

In an non-beanstalk deployment, we could just load properties files from file
system or http url if this is inside secure network. But for beanstalk, it is
ideally a black-box operation, e.g. it is very inconvience to deploy 
configurations into the ec2 instances running this beanstalk, and it is not
secure to use open http url resources.

`S3PropertyPlaceholderConfigurer` is a plumber to load properties from an
private S3 bucket in spring context. 

Assuming you have spring context to load properties from classpath like this:

```
<bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
  <property name="locations">
    <list>
      <value>classpath:app.properties</value>
      <value>classpath:another.properties</value>
    </list>
  <property>
</bean>
```

To load same properties file from a s3 bucket `config/production/app.properties`
You need generate an IAM with aws access key id and secret key, and in 
beanstalk > {you app} > {your environment} > configuration > container, set
AWS_ACCESS_KEY_ID to the aws access key and AWS_SECRET_KEY to the aws secure
key. Then update context to be

```
<bean id="propertyConfigurer" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
  <property name="locations">
    <list>
      <value>classpath:another.properties</value>
    </list>
  <property>
  <property name="s3Locations">
    <list>
      <value>s3://config/production/app.properties</value>
    </list>
  </property>
</bean>
```

Noticed that you can use load properties files as before.
