package org.hisp.dhis.adhoc.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.hisp.dhis.option.OptionSet;
import org.springframework.core.io.ClassPathResource;

import com.csvreader.CsvReader;

public class DataGenerationUtils
{
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private static List<String> womenFirstNames = null;
    private static List<String> menFirstNames = null;
    private static List<String> lastNames = null;

    private static void LoadNames() throws Exception
    {
        //TODO: Questionable thread safety
        if ( menFirstNames == null )
        {
            CsvReader maleReader = new CsvReader( new ClassPathResource( "male_names.csv" ).getInputStream(), UTF_8 );
            menFirstNames = new ArrayList<String>();
            lastNames = new ArrayList<>();
            
            while ( maleReader.readRecord() )
            {
                menFirstNames.add( maleReader.get( 0 ) );
                lastNames.add( maleReader.get( 1 ) );
            }
            maleReader.close();
       
            CsvReader femaleReader = new CsvReader( new ClassPathResource( "female_names.csv" ).getInputStream(), UTF_8 );
            womenFirstNames = new ArrayList<String>();
            while ( femaleReader.readRecord() )
            {
                womenFirstNames.add( femaleReader.get( 0 ) );
                lastNames.add( femaleReader.get( 1 ) );
            }
            femaleReader.close();
        }
    }
    
    public static String getRandomFirstName( boolean isWoman ) throws Exception
    {
        LoadNames();
        
        return isWoman ? womenFirstNames.get( new Random().nextInt( womenFirstNames.size() ) ) :
            menFirstNames.get( new Random().nextInt( menFirstNames.size() ) );
    }
    
    public static String getRandomLastName() throws Exception
    {
        LoadNames();
        
        return lastNames.get( new Random().nextInt( womenFirstNames.size() ) );
    }
    
    public static String getRandomDateString( int startYear, int endYear ) 
    {
        GregorianCalendar gc = new GregorianCalendar();
        int year = randBetween(startYear, endYear);
        gc.set(Calendar.YEAR, year);
        int dayOfYear = randBetween(1, gc.getActualMaximum(Calendar.DAY_OF_YEAR));
        gc.set(Calendar.DAY_OF_YEAR, dayOfYear);
        
        return dateFormat.format( gc.getTime() );
    }

    public static int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }
    
    public static String getRandomOptionSetCode( OptionSet optionSet )
    {
        int no = optionSet.getOptions().size();
        int r = new Random().nextInt( no );
        return optionSet.getOptions().get( r ).getCode();        
    }
    
    public static String getRandomBoolString()
    {
        return String.valueOf( new Random().nextBoolean() );
    }

    public static double[] getRandomCoordinates(String orgUnitCoordinates, double radiusInMeter) {
        String[] centerCoordinates = orgUnitCoordinates.substring(1, orgUnitCoordinates.length() -1).split(",");
        double latitudeCenter = Double.parseDouble(centerCoordinates[1]);
        double longitudeCenter = Double.parseDouble(centerCoordinates[0]);

        double radiusInDegrees = radiusInMeter / 111300f;

        // Get a random distance and a random angle.
        double u = Math.random();
        double v = Math.random();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;

        // Get the x and y delta values.
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double xp = x / Math.cos(Math.toRadians(latitudeCenter));
        return new double[] {latitudeCenter+y, longitudeCenter+xp};
        //return String.format("[%s,%s]", latitudeCenter + y, longitudeCenter + xp);
    }
}
