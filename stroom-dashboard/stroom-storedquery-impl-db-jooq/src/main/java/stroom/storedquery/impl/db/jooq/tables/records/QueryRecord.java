/*
 * This file is generated by jOOQ.
 */
package stroom.storedquery.impl.db.jooq.tables.records;


import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Row11;
import org.jooq.impl.UpdatableRecordImpl;

import stroom.storedquery.impl.db.jooq.tables.Query;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class QueryRecord extends UpdatableRecordImpl<QueryRecord> implements Record11<Integer, Integer, Long, String, Long, String, String, String, String, String, Boolean> {

    private static final long serialVersionUID = 1136673003;

    /**
     * Setter for <code>stroom.query.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>stroom.query.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>stroom.query.version</code>.
     */
    public void setVersion(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>stroom.query.version</code>.
     */
    public Integer getVersion() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>stroom.query.create_time_ms</code>.
     */
    public void setCreateTimeMs(Long value) {
        set(2, value);
    }

    /**
     * Getter for <code>stroom.query.create_time_ms</code>.
     */
    public Long getCreateTimeMs() {
        return (Long) get(2);
    }

    /**
     * Setter for <code>stroom.query.create_user</code>.
     */
    public void setCreateUser(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>stroom.query.create_user</code>.
     */
    public String getCreateUser() {
        return (String) get(3);
    }

    /**
     * Setter for <code>stroom.query.update_time_ms</code>.
     */
    public void setUpdateTimeMs(Long value) {
        set(4, value);
    }

    /**
     * Getter for <code>stroom.query.update_time_ms</code>.
     */
    public Long getUpdateTimeMs() {
        return (Long) get(4);
    }

    /**
     * Setter for <code>stroom.query.update_user</code>.
     */
    public void setUpdateUser(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>stroom.query.update_user</code>.
     */
    public String getUpdateUser() {
        return (String) get(5);
    }

    /**
     * Setter for <code>stroom.query.dashboard_uuid</code>.
     */
    public void setDashboardUuid(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>stroom.query.dashboard_uuid</code>.
     */
    public String getDashboardUuid() {
        return (String) get(6);
    }

    /**
     * Setter for <code>stroom.query.component_id</code>.
     */
    public void setComponentId(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>stroom.query.component_id</code>.
     */
    public String getComponentId() {
        return (String) get(7);
    }

    /**
     * Setter for <code>stroom.query.name</code>.
     */
    public void setName(String value) {
        set(8, value);
    }

    /**
     * Getter for <code>stroom.query.name</code>.
     */
    public String getName() {
        return (String) get(8);
    }

    /**
     * Setter for <code>stroom.query.data</code>.
     */
    public void setData(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>stroom.query.data</code>.
     */
    public String getData() {
        return (String) get(9);
    }

    /**
     * Setter for <code>stroom.query.favourite</code>.
     */
    public void setFavourite(Boolean value) {
        set(10, value);
    }

    /**
     * Getter for <code>stroom.query.favourite</code>.
     */
    public Boolean getFavourite() {
        return (Boolean) get(10);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record11 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row11<Integer, Integer, Long, String, Long, String, String, String, String, String, Boolean> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row11<Integer, Integer, Long, String, Long, String, String, String, String, String, Boolean> valuesRow() {
        return (Row11) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return Query.QUERY.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return Query.QUERY.VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field3() {
        return Query.QUERY.CREATE_TIME_MS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Query.QUERY.CREATE_USER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field5() {
        return Query.QUERY.UPDATE_TIME_MS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return Query.QUERY.UPDATE_USER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Query.QUERY.DASHBOARD_UUID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return Query.QUERY.COMPONENT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field9() {
        return Query.QUERY.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field10() {
        return Query.QUERY.DATA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field11() {
        return Query.QUERY.FAVOURITE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component2() {
        return getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component3() {
        return getCreateTimeMs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getCreateUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long component5() {
        return getUpdateTimeMs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component6() {
        return getUpdateUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getDashboardUuid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component8() {
        return getComponentId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component9() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component10() {
        return getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component11() {
        return getFavourite();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value3() {
        return getCreateTimeMs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getCreateUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value5() {
        return getUpdateTimeMs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getUpdateUser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getDashboardUuid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value8() {
        return getComponentId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value9() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value10() {
        return getData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value11() {
        return getFavourite();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value2(Integer value) {
        setVersion(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value3(Long value) {
        setCreateTimeMs(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value4(String value) {
        setCreateUser(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value5(Long value) {
        setUpdateTimeMs(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value6(String value) {
        setUpdateUser(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value7(String value) {
        setDashboardUuid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value8(String value) {
        setComponentId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value9(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value10(String value) {
        setData(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord value11(Boolean value) {
        setFavourite(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRecord values(Integer value1, Integer value2, Long value3, String value4, Long value5, String value6, String value7, String value8, String value9, String value10, Boolean value11) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached QueryRecord
     */
    public QueryRecord() {
        super(Query.QUERY);
    }

    /**
     * Create a detached, initialised QueryRecord
     */
    public QueryRecord(Integer id, Integer version, Long createTimeMs, String createUser, Long updateTimeMs, String updateUser, String dashboardUuid, String componentId, String name, String data, Boolean favourite) {
        super(Query.QUERY);

        set(0, id);
        set(1, version);
        set(2, createTimeMs);
        set(3, createUser);
        set(4, updateTimeMs);
        set(5, updateUser);
        set(6, dashboardUuid);
        set(7, componentId);
        set(8, name);
        set(9, data);
        set(10, favourite);
    }
}