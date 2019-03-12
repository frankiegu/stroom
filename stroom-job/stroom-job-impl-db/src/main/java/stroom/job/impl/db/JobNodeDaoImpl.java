package stroom.job.impl.db;

import org.jooq.Condition;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.TableField;
import stroom.db.util.AuditUtil;
import stroom.db.util.GenericDao;
import stroom.db.util.JooqUtil;
import stroom.job.impl.JobNodeDao;
import stroom.job.impl.db.jooq.tables.records.JobNodeRecord;
import stroom.job.shared.FindJobNodeCriteria;
import stroom.job.shared.Job;
import stroom.job.shared.JobNode;
import stroom.job.shared.JobNode.JobType;
import stroom.security.SecurityContext;
import stroom.util.shared.BaseResultList;
import stroom.util.shared.HasIntCrud;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static stroom.job.impl.db.jooq.Tables.JOB;
import static stroom.job.impl.db.jooq.Tables.JOB_NODE;

public class JobNodeDaoImpl implements JobNodeDao, HasIntCrud<JobNode> {
//    private static final Logger LOGGER = LoggerFactory.getLogger(JobNodeDaoImpl.class);
//    private static final LambdaLogger LAMBDA_LOGGER = LambdaLoggerFactory.getLogger(JobNodeDaoImpl.class);


    private static final Map<String, TableField> TABLE_FIELD_MAP = Map.of(
            FindJobNodeCriteria.FIELD_ID, JOB_NODE.ID);

    private static final Function<Record, Job> RECORD_TO_JOB_MAPPER = record -> {
        final Job job = new Job();
        job.setId(record.get(JOB.ID));
        job.setVersion(record.get(JOB.VERSION));
        job.setCreateTimeMs(record.get(JOB.CREATE_TIME_MS));
        job.setCreateUser(record.get(JOB.CREATE_USER));
        job.setUpdateTimeMs(record.get(JOB.UPDATE_TIME_MS));
        job.setUpdateUser(record.get(JOB.UPDATE_USER));
        job.setName(record.get(JOB.NAME));
        job.setEnabled(record.get(JOB.ENABLED));
        return job;
    };

    private static final Function<Record, JobNode> RECORD_TO_JOB_NODE_MAPPER = record -> {
        final JobNode jobNode = new JobNode();
        jobNode.setId(record.get(JOB_NODE.ID));
        jobNode.setVersion(record.get(JOB_NODE.VERSION));
        jobNode.setCreateTimeMs(record.get(JOB_NODE.CREATE_TIME_MS));
        jobNode.setCreateUser(record.get(JOB_NODE.CREATE_USER));
        jobNode.setUpdateTimeMs(record.get(JOB_NODE.UPDATE_TIME_MS));
        jobNode.setUpdateUser(record.get(JOB_NODE.UPDATE_USER));
        jobNode.setJobType(JobType.PRIMITIVE_VALUE_CONVERTER.fromPrimitiveValue(record.get(JOB_NODE.JOB_TYPE)));
        jobNode.setNodeName(record.get(JOB_NODE.NODE_NAME));
        jobNode.setTaskLimit(record.get(JOB_NODE.TASK_LIMIT));
        jobNode.setSchedule(record.get(JOB_NODE.SCHEDULE));
        jobNode.setEnabled(record.get(JOB_NODE.ENABLED));
        return jobNode;
    };

    private static final BiFunction<JobNode, JobNodeRecord, JobNodeRecord> JOB_NODE_TO_RECORD_MAPPER = (jobNode, record) -> {
        record.from(jobNode);
        record.set(JOB_NODE.JOB_ID, jobNode.getJob().getId());
        record.set(JOB_NODE.JOB_TYPE, jobNode.getJobType().getPrimitiveValue());
        return record;
    };

    private final ConnectionProvider connectionProvider;
    private final SecurityContext securityContext;
    private final GenericDao<JobNodeRecord, JobNode, Integer> dao;

    @Inject
    JobNodeDaoImpl(final ConnectionProvider connectionProvider,
                   final SecurityContext securityContext) {
        this.connectionProvider = connectionProvider;
        this.securityContext = securityContext;

        dao = new GenericDao<>(JOB_NODE, JOB_NODE.ID, JobNode.class, connectionProvider);
        dao.setObjectToRecordMapper(JOB_NODE_TO_RECORD_MAPPER);
        dao.setRecordToObjectMapper(RECORD_TO_JOB_NODE_MAPPER);
    }

    @Override
    public JobNode create(@Nonnull final JobNode jobNode) {
        AuditUtil.stamp(securityContext.getUserId(), jobNode);
        final JobNode result = dao.create(jobNode);
        result.setJob(jobNode.getJob());
        return result;

//        final JobNode result = JooqUtil.contextResult(connectionProvider, context -> {
//            LAMBDA_LOGGER.debug(() -> LambdaLogger.buildMessage("Creating a {}", JOB_NODE.getName()));
//            JobNodeRecord record = context.newRecord(JOB_NODE, jobNode);
//            record.set(JOB_NODE.JOB_ID, jobNode.getJob().getId());
//            record.store();
//            return record.into(JobNode.class);
//        });
//        result.setJob(jobNode.getJob());
//        return result;


    }

    @Override
    public JobNode update(@Nonnull final JobNode jobNode) {
        AuditUtil.stamp(securityContext.getUserId(), jobNode);
        final JobNode result = dao.update(jobNode);
        result.setJob(jobNode.getJob());
        return result;
    }

    @Override
    public boolean delete(int id) {
        return dao.delete(id);
    }

    @Override
    public Optional<JobNode> fetch(int id) {
        return dao.fetch(id);
    }

    public BaseResultList<JobNode> find(FindJobNodeCriteria criteria) {
        final Collection<Condition> conditions = new ArrayList<>();
        JooqUtil.getStringCondition(JOB.NAME, criteria.getJobName()).ifPresent(conditions::add);
        JooqUtil.getStringCondition(JOB_NODE.NODE_NAME, criteria.getNodeName()).ifPresent(conditions::add);

        final OrderField[] orderFields = JooqUtil.getOrderFields(TABLE_FIELD_MAP, criteria);

        final List<JobNode> list = JooqUtil.contextResult(connectionProvider, context -> context
                .select()
                .from(JOB_NODE)
                .join(JOB).on(JOB_NODE.JOB_ID.eq(JOB.ID))
                .where(conditions)
                .orderBy(orderFields)
                .limit(JooqUtil.getLimit(criteria.getPageRequest()))
                .offset(JooqUtil.getOffset(criteria.getPageRequest()))
                .fetch(record -> {
                    final Job job = RECORD_TO_JOB_MAPPER.apply(record);
                    final JobNode jobNode = RECORD_TO_JOB_NODE_MAPPER.apply(record);
                    jobNode.setJob(job);
                    return jobNode;
                }));

        return BaseResultList.createUnboundedList(list);
    }


//    private GenericDao<JobNodeRecord, JobNode, Integer> dao;
//
//    @Inject
//    JobNodeDao(final ConnectionProvider connectionProvider) {
//        dao = new GenericDao<>(JOB_NODE, JOB_NODE.ID, JobNode.class, connectionProvider);
//    }
//
//    @Override
//    public JobNode create(@Nonnull final JobNode jobNode) {
//        return dao.create(jobNode);
//    }
//
//    @Override
//    public JobNode update(@Nonnull final JobNode jobNode) {
//        return dao.update(jobNode);
//    }
//
//    @Override
//    public boolean delete(int id) {
//        return dao.delete(id);
//    }
//
//    @Override
//    public Optional<JobNode> fetch(int id) {
//        return dao.fetch(id);
//    }
}