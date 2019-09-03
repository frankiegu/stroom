/*
 * This file is generated by jOOQ.
 */
package stroom.index.impl.db.jooq;


import stroom.index.impl.db.jooq.tables.IndexShard;
import stroom.index.impl.db.jooq.tables.IndexVolume;
import stroom.index.impl.db.jooq.tables.IndexVolumeGroup;

import javax.annotation.Generated;


/**
 * Convenience access to all tables in stroom
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.11.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>stroom.index_shard</code>.
     */
    public static final IndexShard INDEX_SHARD = stroom.index.impl.db.jooq.tables.IndexShard.INDEX_SHARD;

    /**
     * The table <code>stroom.index_volume</code>.
     */
    public static final IndexVolume INDEX_VOLUME = stroom.index.impl.db.jooq.tables.IndexVolume.INDEX_VOLUME;

    /**
     * The table <code>stroom.index_volume_group</code>.
     */
    public static final IndexVolumeGroup INDEX_VOLUME_GROUP = stroom.index.impl.db.jooq.tables.IndexVolumeGroup.INDEX_VOLUME_GROUP;
}