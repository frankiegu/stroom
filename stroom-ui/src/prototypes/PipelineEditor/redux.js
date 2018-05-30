import { createAction, handleActions, combineActions } from 'redux-actions';

import {
  moveElementInPipeline,
  deleteElementInPipeline
} from './pipelineUtils';

const pipelineChanged = createAction('PIPELINE_CHANGED', (pipelineId, pipeline) => ({
  pipelineId,
  pipeline,
}));

const pipelineElementSelected = createAction(
  'PIPELINE_ELEMENT_SELECTED',
  (pipelineId, elementId) => ({ pipelineId, elementId }),
);

const pipelineElementMoved = createAction(
  'PIPELINE_ELEMENT_MOVED',
  (pipelineId, itemToMove, destination) => ({ pipelineId, itemToMove, destination }),
);

const requestDeletePipelineElement = createAction(
  'REQUEST_DELETE_PIPELINE_ELEMENT',
  (pipelineId, elementId) => ({pipelineId, elementId})
)

const confirmDeletePipelineElement = createAction(
  'CONFIRM_DELETE_PIPELINE_ELEMENT',
  (pipelineId, elementId) => ({pipelineId, elementId})
)

const cancelDeletePipelineElement = createAction(
  'CANCEL_DELETE_PIPELINE_ELEMENT',
  (pipelineId) => ({pipelineId})
)

const openPipelineElementContextMenu = createAction(
  'OPEN_PIPELINE_ELEMENT_CONTEXT_MENU',
  (pipelineId, elementId) => ({pipelineId, elementId})
)
const closePipelineElementContextMenu = createAction(
  'CLOSE_PIPELINE_ELEMENT_CONTEXT_MENU',
  (pipelineId, elementId) => ({pipelineId, elementId})
)

// pipelines, keyed on ID, there may be several expressions on a page
const defaultPipelineState = {};

const pipelineReducer = handleActions(
  {
    [pipelineChanged]: (state, action) => ({
      ...state,
      [action.payload.pipelineId]: {
        pipeline: action.payload.pipeline,
        selectedElementId: undefined,
        pendingElementIdToDelete : undefined
      },
    }),
    [pipelineElementSelected]: (state, action) => ({
      ...state,
      [action.payload.pipelineId]: {
        ...state[action.payload.pipelineId],
        selectedElementId: action.payload.elementId,
      },
    }),
    [requestDeletePipelineElement]:(state, action) => ({
      ...state,
      [action.payload.pipelineId]: {
        ...state[action.payload.pipelineId],
        pendingElementIdToDelete : action.payload.elementId
      }
    }),
    [confirmDeletePipelineElement]:(state, action) => ({
      ...state,
      [action.payload.pipelineId]: {
        pipeline : deleteElementInPipeline(
          state[action.payload.pipelineId].pipeline,
          action.payload.elementId
        ),
        pendingElementIdToDelete : undefined
      }
    }),
    [cancelDeletePipelineElement]: (state, action) => ({
      ...state,
      [action.payload.pipelineId]: {
        ...state[action.payload.pipelineId],
        pendingElementIdToDelete : undefined
      }
    }),
    [pipelineElementMoved]: (state, action) => ({
      ...state,
      [action.payload.pipelineId]: {
        ...state[action.payload.pipelineId],
        pipeline: moveElementInPipeline(
          state[action.payload.pipelineId].pipeline,
          action.payload.itemToMove,
          action.payload.destination
        )
      },
    }),
    [openPipelineElementContextMenu]: (state, action) => ({
      ...state,
      [action.payload.pipelineId]: {
        ...state[action.payload.pipelineId],
        contextMenuElementId : action.payload.elementId
      }
    }),
    [closePipelineElementContextMenu]: (state, action) => ({
      ...state,
      [action.payload.pipelineId]: {
        ...state[action.payload.pipelineId],
        contextMenuElementId : undefined
      }
    })
  },
  defaultPipelineState,
);

const defaultElementState = {}

const elementsReceived = createAction(
  'ELEMENTS_RECEIVED',
  (elements) => ({elements})
)
const elementPropertiesReceived = createAction(
  'ELEMENT_PROPERTIES_RECEIVED',
  (elementProperties) => ({elementProperties})
)

const elementReducer = handleActions({
  [elementsReceived]:
  (state, action) => ({
    ...state,
    elements: action.payload.elements
  }),

  [elementPropertiesReceived]:
  (state, action) => ({
    ...state,
    elementProperties: action.payload.elementProperties
  })

}, defaultElementState);

export {
  pipelineChanged,
  pipelineElementSelected,
  requestDeletePipelineElement,
  confirmDeletePipelineElement,
  cancelDeletePipelineElement,
  pipelineElementMoved,
  openPipelineElementContextMenu,
  closePipelineElementContextMenu,
  elementsReceived,
  elementPropertiesReceived,
  pipelineReducer,
  elementReducer
};