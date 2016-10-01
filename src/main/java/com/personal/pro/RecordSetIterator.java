package com.personal.pro;
public interface RecordSetIterator {
    void   reset() ;
    byte[] getNextRecord();
    boolean hasMoreRecords() ;
}
