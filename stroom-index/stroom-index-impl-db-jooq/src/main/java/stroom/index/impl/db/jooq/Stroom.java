/*
 * This file is generated by jOOQ.
 */
package stroom.index.impl.db.jooq;


import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;
import stroom.index.impl.db.jooq.tables.IndexShard;
import stroom.index.impl.db.jooq.tables.IndexVolume;
import stroom.index.impl.db.jooq.tables.IndexVolumeGroup;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
public class Stroom extends SchemaImpl {

    private static final long serialVersionUID = -301713393;

    /**
     * The reference instance of <code>stroom</code>
     */
    public static final Stroom STROOM = new Stroom();

    /**
     * The table <code>stroom.index_shard</code>.
     */
    public final IndexShard INDEX_SHARD = stroom.index.impl.db.jooq.tables.IndexShard.INDEX_SHARD;

    /**
     * The table <code>stroom.index_volume</code>.
     */
    public final IndexVolume INDEX_VOLUME = stroom.index.impl.db.jooq.tables.IndexVolume.INDEX_VOLUME;

    /**
     * The table <code>stroom.index_volume_group</code>.
     */
    public final IndexVolumeGroup INDEX_VOLUME_GROUP = stroom.index.impl.db.jooq.tables.IndexVolumeGroup.INDEX_VOLUME_GROUP;

    /**
     * No further instances allowed
     */
    private Stroom() {
        super("stroom", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            IndexShard.INDEX_SHARD,
            IndexVolume.INDEX_VOLUME,
            IndexVolumeGroup.INDEX_VOLUME_GROUP);
    }
}