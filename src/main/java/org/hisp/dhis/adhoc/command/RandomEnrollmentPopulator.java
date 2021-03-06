package org.hisp.dhis.adhoc.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.adhoc.annotation.Executed;
import org.hisp.dhis.adhoc.utils.DataGenerationUtils;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dxf2.utils.InputUtils;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceService;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.ProgramStageInstanceService;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeReservedValueService;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class RandomEnrollmentPopulator
{
    private static final Log log = LogFactory.getLog( RandomChildrenPopulator.class );
    
    @Autowired
    private TrackedEntityInstanceService teiService;
        
    @Autowired
    private ProgramService programService;
        
    @Autowired
    private ProgramInstanceService programInstanceService;
    
    @Autowired
    private ProgramStageInstanceService psiService;

    @Autowired
    private TrackedEntityAttributeValueService attributeValueService;
    
    @Autowired
    private TrackedEntityAttributeReservedValueService attributeReservedValueService;
    
    @Autowired
    private TrackedEntityDataValueService dataValueService;

    @Autowired
    protected InputUtils inputUtils;

    private String firstName;
    private String lastName;
    
    
    @Executed
    @Transactional
    public void execute()
        throws Exception
    {
        int numberOfRecords = 1; //Update number of records
        Program p = programService.getProgram( "p55TSnIz83Z" ); //Update program uid
        
        List<OrganisationUnit> ous = new ArrayList<OrganisationUnit>( p.getOrganisationUnits() );
        
        TrackedEntity te = p.getTrackedEntity();
        boolean isWoman = te.getName().toLowerCase().contains("mnch") || new Random().nextBoolean();
        
        int totalProgramStageInstanceCount = 0;
        
        for ( int recordNumber = 0; recordNumber < numberOfRecords; recordNumber++ ) 
        {
            int programStageInstanceCount = 0;
            
            DateTime date = new DateTime( DateTime.now().getYear(), 2, 1, 12, 5 ).plusDays( new Random().nextInt( 60 ) );
            
            OrganisationUnit ou = ous.get( new Random().nextInt( ous.size() ) );
            TrackedEntityInstance tei = new TrackedEntityInstance();
            
            tei.setTrackedEntity( te );
            tei.setOrganisationUnit( ou );


            double lat = 0;
            double lng = 0;
            
            teiService.addTrackedEntityInstance( tei );
            
            for ( TrackedEntityAttribute att : p.getTrackedEntityAttributes() ) 
            {
                String an = att.getName().toLowerCase();

                if ( att.isGenerated() )
                {
                    attributeValueService.addTrackedEntityAttributeValue( new TrackedEntityAttributeValue( att, tei,
                            attributeReservedValueService.getGeneratedValue( att ) ) );
                }
                else if ( an.contains( "first" ) && an.contains( "name" ) ) 
                {
                    firstName = DataGenerationUtils.getRandomFirstName( isWoman );
                    attributeValueService.addTrackedEntityAttributeValue( new TrackedEntityAttributeValue( att, tei,
                            firstName ) );
                }
                else if ( an.contains( "last" ) && an.contains( "name" ) ) 
                {
                    lastName = DataGenerationUtils.getRandomLastName();
                    attributeValueService.addTrackedEntityAttributeValue( new TrackedEntityAttributeValue( att, tei,
                            lastName ) );
                }
                else if ( ( an.contains( "birth" ) || an.contains( "born" ) || an.contains("dob")) )
                {
                    attributeValueService.addTrackedEntityAttributeValue( new TrackedEntityAttributeValue( att, tei,
                            DataGenerationUtils.getRandomDateString( 1970, 1990 ) ) );
                }
                else if (att.isOptionSetValue()) {
                    attributeValueService.addTrackedEntityAttributeValue(new TrackedEntityAttributeValue(att, tei,
                            DataGenerationUtils.getRandomOptionSetCode(att.getOptionSet())));
                }
                else if (an.contains("residence")) {
                    double[] coords = DataGenerationUtils.getRandomCoordinates(ou.getCoordinates(), 10000);
                    lat =  coords[0];
                    lng = coords[1];
                    //log.info("lat: " + lat + ", lng: " + lng);

                    attributeValueService.addTrackedEntityAttributeValue(new TrackedEntityAttributeValue(att, tei,
                            String.format("[%s,%s]",lng,lat)));
                }
            }

            ProgramInstance pi = programInstanceService.enrollTrackedEntityInstance( tei, p, date.toDate(), date.toDate(), ou );
            for ( ProgramStage ps : p.getProgramStages() )
            {
                //int eventsToAdd = ps.getRepeatable() ? DataGenerationUtils.randBetween( 1, 5 ) : 1;
                int eventsToAdd = 1;

                for ( int eventCount = 0; eventCount < eventsToAdd; eventCount++ ) 
                {
                    date = date.plusDays( DataGenerationUtils.randBetween( 1, 5 ) );

                    ProgramStageInstance psi = new ProgramStageInstance( pi, ps );
                    psi.setDueDate( date.toDate() );
                    psi.setExecutionDate( date.toDate() );
                    psi.setOrganisationUnit( ou );
                    psi.setLatitude(lat);
                    psi.setLongitude(lng);
                    psi.setCompletedBy("simon");
                    psi.setCompletedDate(date.toDate());
                    psi.setStoredBy("simon");
                    psi.setStatus(EventStatus.COMPLETED);
                    //DataElementCategoryOptionCombo attributeOptionCombo = inputUtils.getAttributeOptionCombo(
                     //       p.getCategoryCombo().getUid(), event.getAttributeCategoryOptions(), false );

                    //psi.setAttributeOptionCombo();

                    log.info("lat: " + lat + ", lng: " + lng);


                    psiService.addProgramStageInstance( psi );
                    programStageInstanceCount++;
                    totalProgramStageInstanceCount++;

                    for ( ProgramStageDataElement psde : ps.getProgramStageDataElements() )
                    {
                        DataElement de = psde.getDataElement();
                        String dn = de.getName().toLowerCase();

                        if ( de.isNumericType() && dn.contains( "moglobin" ) )
                        {
                            dataValueService.saveTrackedEntityDataValue( new TrackedEntityDataValue( psi, de,
                                    Integer.toString( DataGenerationUtils.randBetween( 6, 25 ) ) ) );
                        }
                        else if ( ValueType.BOOLEAN.equals( de.getValueType() ) )
                        {
                            dataValueService.saveTrackedEntityDataValue( new TrackedEntityDataValue( psi, de,
                                    DataGenerationUtils.getRandomBoolString() ) );
                        } else if (de.isOptionSetValue()) {
                            dataValueService.saveTrackedEntityDataValue(new TrackedEntityDataValue(psi, de,
                                    DataGenerationUtils.getRandomOptionSetCode(de.getOptionSet())));
                        }
                    }
                }
            }
            
            log.info( "Created trackedEntityInstance: " + tei.getUid() + " programInstance: " + pi.getUid() + 
                " for program: " + p.getName() + " - " + p.getUid() + " with " + 
                programStageInstanceCount + " programstageInstances.");
        }
        
        log.info( "Created " + numberOfRecords + " programInstances with a total of " + totalProgramStageInstanceCount + " programStageInstances");
        log.info( "Tracker info: " + firstName + " " + lastName);
    }
}