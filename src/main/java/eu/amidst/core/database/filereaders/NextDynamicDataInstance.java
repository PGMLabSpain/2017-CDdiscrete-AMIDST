/**
 ******************* ISSUE LIST **************************
 *
 * 1. We could eliminate the if(timeIDcounter == 1) in nextDataInstance_NoTimeID_NoSeq if
 * we maintain a future DataRow (we read an extra row in advance). Then we would need the
 * method public boolean isNull(){
 * return (present==null || past==null);
 * }
 *
 * ********************************************************
 */
package eu.amidst.core.database.filereaders;

import eu.amidst.core.database.Attribute;

/**
 * Created by ana@cs.aau.dk on 13/11/14.
 */
public final class NextDynamicDataInstance {

    private DataRow present;
    private DataRow past;

    /* Only used in case the sequenceID is not in the datafile */
    private int sequenceID;
    /* timeIDcounter is used to keep track of missing values*/
    private int timeIDcounter;

    public NextDynamicDataInstance(DataRow past, DataRow present, int sequenceID, int timeIDcounter){
        this.past = past;
        this.present = present;

        this.sequenceID = sequenceID;
        this.timeIDcounter = timeIDcounter;
    }
    public  DynamicDataInstance nextDataInstance_NoTimeID_NoSeq(DataFileReader reader){
        DynamicDataInstance dynDataInst = null;
        if(timeIDcounter == 1) {
            dynDataInst = new DynamicDataInstance(past, present, sequenceID, timeIDcounter++);
        }else {
            past = present;
            present = reader.nextDataRow();
            dynDataInst = new DynamicDataInstance(past, present, sequenceID, timeIDcounter++);
        }
        return dynDataInst;
    }

    public DynamicDataInstance nextDataInstance_NoSeq(DataFileReader reader, Attribute attTimeID){
        double presentTimeID = present.getValue(attTimeID);

        /*Missing values of the form (X,?), where X can also be ?*/
        if(timeIDcounter < present.getValue(attTimeID)){
            timeIDcounter++;
            DynamicDataInstance dynDataInst = new DynamicDataInstance(past, new DataRowMissing(), (int) sequenceID,
                    (int) presentTimeID);
            past = new DataRowMissing(); //present is still the same instance, we need to fill in the missing instances
            return dynDataInst;

        /*Missing values of the form (X,Y), where X can also be ? and Y is an observed (already read) instance*/
        }else if(timeIDcounter == present.getValue(attTimeID)) {
            timeIDcounter++;
            DynamicDataInstance dynDataInst = new DynamicDataInstance(past, present, (int) sequenceID,
                    (int) presentTimeID);
            past = present; //present is still the same instance, we need to fill in the missing instances
            return dynDataInst;

        /*Read a new DataRow*/
        }else{
            present = reader.nextDataRow();
            /*Recursive call to this method taking into account the past DataRow*/
            return nextDataInstance_NoSeq(reader, attTimeID);
        }

    }

    public DynamicDataInstance nextDataInstance_NoTimeID(DataFileReader reader, Attribute attSequenceID){
        double pastSequenceID = past.getValue(attSequenceID);
        double presentSequenceID = present.getValue(attSequenceID);
        if (pastSequenceID == presentSequenceID) {
            timeIDcounter++;
            DynamicDataInstance dynDataInst = new DynamicDataInstance(past, present, (int) presentSequenceID, timeIDcounter);
            past = present;
            present = reader.nextDataRow();
            return dynDataInst;
        }
        else{
             past = present;
             present = reader.nextDataRow();
             /* Recursive call */
             timeIDcounter = 0;
             return nextDataInstance_NoTimeID(reader, attSequenceID);
        }
    }

    public DynamicDataInstance nextDataInstance(DataFileReader reader, Attribute attSequenceID, Attribute attTimeID){
        double pastSequenceID = past.getValue(attSequenceID);
        double pastTimeID = past.getValue(attTimeID);

        /*Missing values of the form (X,?), where X can also be ?*/
        if(timeIDcounter < present.getValue(attTimeID)){
            timeIDcounter++;
            DynamicDataInstance dynDataInst = new DynamicDataInstance(past, new DataRowMissing(), (int) pastSequenceID,
                    (int) pastTimeID);
            past = new DataRowMissing(); //present is still the same instance, we need to fill in the missing instances
            return dynDataInst;

        /*Missing values of the form (X,Y), where X can also be ? and Y is an observed (already read) instance*/
        }else if(timeIDcounter == present.getValue(attTimeID)) {
            timeIDcounter++;
            DynamicDataInstance dynDataInst = new DynamicDataInstance(past, present, (int) pastSequenceID,
                    (int) pastTimeID);
            past = present; //present is still the same instance, we need to fill in the missing instances
            return dynDataInst;

        /*Read a new DataRow*/
        }else{
            present = reader.nextDataRow();
            double presentSequenceID = present.getValue(attSequenceID);
            if (pastSequenceID == presentSequenceID) {
                /*Recursive call to this method taking into account the past DataRow*/
                return nextDataInstance(reader, attSequenceID, attTimeID);
            }else{
                past = present;
                present = reader.nextDataRow();
                /* Recursive call discarding the past DataRow*/
                timeIDcounter = 0;
                return nextDataInstance(reader, attSequenceID, attTimeID);
            }
        }
    }


}