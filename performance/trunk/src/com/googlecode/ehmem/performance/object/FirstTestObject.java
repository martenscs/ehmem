package com.googlecode.ehmem.performance.object;

import java.io.Serializable;
import java.util.Date;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class FirstTestObject implements Serializable, Keyed {
    private static final long serialVersionUID = 5467036461013086764L;

    private String key;
    private String textField;
    private long longField;
    private Date dateField;

    public FirstTestObject(String key, String textField, long longField, Date dateField) {
        this.key = key;
        this.textField = textField;
        this.longField = longField;
        this.dateField = dateField;
    }

    public String getKey() {
        return key;
    }

    /**
     * @return the textField
     */
    public String getTextField() {
        return textField;
    }

    /**
     * @return the longField
     */
    public long getLongField() {
        return longField;
    }

    /**
     * @return the dateField
     */
    public Date getDateField() {
        return dateField;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{first;key=%s;text=%s;long=%d;date=%s}", key, textField, longField, dateField);
    }
}
