package org.hisp.dhis.adhoc;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.adhoc.annotation.Executed;
import org.hisp.dhis.system.util.AnnotationUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.collect.ImmutableList;

/**
 * The purpose of this tool is to assist in performing ad-hoc tasks which
 * benefits from having the DHIS 2 service layer accessible. Examples of
 * such tasks are writing complex custom data entry forms to file and performing 
 * database operations which cannot be solved using SQL.
 * 
 * This class should be executed. You can do this e.g. by choosing "Run as" -
 * "Java application" in your IDE.
 * 
 * To add tasks one should:
 * 
 * <ol>
 * <li>Create a Java class</li>
 * <li>Annotate the method which performs the work with {@link @Executed}.</li>
 * <li>Register the implementation class as a bean in beans.xml under src/main/resources/META-INF/dhis.</li>
 * <li>Add the bean identifier to the list of commands in this class.</li>
 * </ol>
 */
public class RunMe

{    
    /**
     * Change location of DHIS 2 home to match your environment.
     */
    private static final String DHIS2_HOME = "/home/lars/dev/config/dhis2"; // Change this

    /**
     * Add commands here by adding the bean identifier to the list.
     */
    private static final ImmutableList<String> COMMANDS = ImmutableList.of( "randomDataUsagePopulator" ); // Change this

    // -------------------------------------------------------------------------
    // RunMe
    // -------------------------------------------------------------------------

    private static final Log log = LogFactory.getLog( RunMe.class );
    
    private static ApplicationContext context;
        
    public static void main( String[] args )
        throws Exception
    {
        System.setProperty( "dhis2.home", DHIS2_HOME );
        
        log.info( "Initializing Spring context" );
        
        context = new ClassPathXmlApplicationContext( "classpath*:/META-INF/dhis/beans.xml" );
        
        log.info( "Spring context initialized" );
        
        for ( String id : COMMANDS )
        {
            Object command = context.getBean( id );
            
            log.info( String.format( "Executing command: %s", id ) );
            
            invokeCommand( command );
            
            log.info( "Done: " + id );
        }
        
        log.info( "Process completed" );
    }
        
    private static void invokeCommand( Object object )
        throws Exception
    {
        List<Method> methods = AnnotationUtils.getAnnotatedMethods( object, Executed.class );
        
        if ( methods.size() != 1 )
        {
            log.warn( String.format( "Exactly one method must be annotated for execution on class: %s", object.getClass() ) );
            return;
        }
        
        methods.get( 0 ).invoke( object, new Object[0] );
    }
}
