package stroom.explorer;

import stroom.entity.shared.SharedDocRefInfo;
import stroom.explorer.shared.ExplorerServiceInfoAction;
import stroom.query.api.v2.DocRefInfo;
import stroom.task.AbstractTaskHandler;
import stroom.task.TaskHandlerBean;

import javax.inject.Inject;

@TaskHandlerBean(task = ExplorerServiceInfoAction.class)
class ExplorerServiceInfoHandler extends AbstractTaskHandler<ExplorerServiceInfoAction, SharedDocRefInfo> {

    private final ExplorerService explorerService;

    @Inject
    ExplorerServiceInfoHandler(final ExplorerService explorerService) {
        this.explorerService = explorerService;
    }

    @Override
    public SharedDocRefInfo exec(final ExplorerServiceInfoAction task) {
        final DocRefInfo docRefInfo = explorerService.info(task.getDocRef());

        return new SharedDocRefInfo.Builder()
                .type(docRefInfo.getDocRef().getType())
                .uuid(docRefInfo.getDocRef().getUuid())
                .name(docRefInfo.getDocRef().getName())
                .otherInfo(docRefInfo.getOtherInfo())
                .createTime(docRefInfo.getCreateTime())
                .createUser(docRefInfo.getCreateUser())
                .updateTime(docRefInfo.getUpdateTime())
                .updateUser(docRefInfo.getUpdateUser())
                .build();
    }
}