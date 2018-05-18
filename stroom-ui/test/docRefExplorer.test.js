import expect from 'expect.js';

import { createStore } from 'redux';

import {
    iterateNodes
} from '../src/lib/treeUtils';

import {
    DEFAULT_EXPLORER_ID,
    receiveDocTree,
    explorerTreeOpened,
    moveExplorerItem,
    toggleFolderOpen,
    openDocRef,
    deleteDocRef,
    searchTermChanged,
    selectDocRef,
    openDocRefContextMenu,
    closeDocRefContextMenu,
    explorerTreeReducer
} from '../src/components/DocExplorer/redux';

import { 
    testTree, 
    fellowship, 
    sam, 
    ridersOfRohan,
    gimli, 
    smaug,
    golem,
    shelob,
    agents_of_chaos,
    evilTrinkets,
    giftsFromGaladriel,
    favouriteAdjectives,
    DOC_REF_TYPES 
} from '../src/components/DocExplorer/storybook/testTree'

// Rebuilt for each test
let store;

describe('Doc Explorer Reducer', () => {
    beforeEach(function() {
        store = createStore(explorerTreeReducer);
    });
      
    describe('Explorer Tree', () => {
        it('should contain the test tree', () => {
            store.dispatch(receiveDocTree(testTree))

            let state = store.getState();
            expect(state).to.have.property('documentTree')
            expect(state.documentTree).to.be(testTree);
        });
        it('should create a new explorer state for given ID', () => {
            // Given
            let explorerId = 'testIdMonkey';
            let allowMultiSelect = true;
            let allowDragAndDrop = true;
            let typeFilter = undefined;

            // When
            store.dispatch(receiveDocTree(testTree))
            store.dispatch(explorerTreeOpened(explorerId, allowMultiSelect, allowDragAndDrop, typeFilter));

            // Then
            let state = store.getState();
            expect(state.explorers).to.have.property(explorerId);
            let explorer = state.explorers[explorerId];
            expect(explorer.typeFilter).to.be(undefined);
            expect(explorer.allowMultiSelect).to.be(true);
            expect(explorer.allowDragAndDrop).to.be(true);
        })
    })

    describe('Type Filtering', () => {
        it('should make all doc refs visible with no type filter', () => {
            // Given
            let explorerId = 'testIdGroot';
            let allowMultiSelect = true;
            let allowDragAndDrop = true;
            let typeFilter = undefined;

            // When
            store.dispatch(receiveDocTree(testTree))
            store.dispatch(explorerTreeOpened(explorerId, allowMultiSelect, allowDragAndDrop, typeFilter));
            let state = store.getState();
            let explorer = state.explorers[explorerId];

            // Then
            iterateNodes(state.documentTree, (lineage, node) => {
                expect(explorer.isVisible[node.uuid]).to.be(true);
            })
        })
        it('should only make doc refs matching type filter visible', () => {
            // Given
            let explorerId = 'testIdGroot';
            let allowMultiSelect = true;
            let allowDragAndDrop = true;
            let typeFilter = DOC_REF_TYPES.HOBBIT;

            // When
            store.dispatch(receiveDocTree(testTree))
            store.dispatch(explorerTreeOpened(explorerId, allowMultiSelect, allowDragAndDrop, typeFilter));

            // Then
            let state = store.getState();
            expect(state.explorers).to.have.property(explorerId);
            let explorer = state.explorers[explorerId];

            // Check a folder that contains a match
            expect(explorer.isVisible[fellowship.uuid]).to.be(true);
            
            // Check a matching doc
            expect(explorer.isVisible[sam.uuid]).to.be(true);

            // Check a folder that doesn't contain a matching
            expect(explorer.isVisible[ridersOfRohan.uuid]).to.be(false);

            // Check a non matching doc
            expect(explorer.isVisible[gimli.uuid]).to.be(false);
        });
        it('should combine search terms and type filters correctly', () => {
            // Given
            let explorerId = 'testIdRimmer';
            let allowMultiSelect = true;
            let allowDragAndDrop = true;
            let typeFilter = DOC_REF_TYPES.DICTIONARY;
            let searchTerm = 'sma';

            // When
            store.dispatch(receiveDocTree(testTree))
            store.dispatch(explorerTreeOpened(explorerId, allowMultiSelect, allowDragAndDrop, typeFilter));
            store.dispatch(searchTermChanged(explorerId, searchTerm));
            store.dispatch(searchTermChanged(explorerId, undefined));

            // Then
            let state = store.getState();
            expect(state.explorers).to.have.property(explorerId);
            let explorer = state.explorers[explorerId];
            
            // Check a matching doc
            expect(explorer.isVisible[evilTrinkets.uuid]).to.be(true);

            // Check a folder that doesn't contain a matching
            expect(explorer.isVisible[ridersOfRohan.uuid]).to.be(false);

            // Check a non matching doc
            expect(explorer.isVisible[sam.uuid]).to.be(false);
        })
        it('should combine search terms and type filters correctly', () => {
            // Given
            let explorerId = 'testIdLister';
            let allowMultiSelect = true;
            let allowDragAndDrop = true;
            let typeFilter = DOC_REF_TYPES.HOBBIT;
            let searchTerm = 'sma';

            // When
            store.dispatch(receiveDocTree(agents_of_chaos))
            store.dispatch(explorerTreeOpened(explorerId, allowMultiSelect, allowDragAndDrop, typeFilter));
            store.dispatch(searchTermChanged(explorerId, searchTerm));
            store.dispatch(searchTermChanged(explorerId, undefined));

            // Then
            let state = store.getState();
            expect(state.explorers).to.have.property(explorerId);
            let explorer = state.explorers[explorerId];

            // Check the matching doc
            expect(explorer.isVisible[golem.uuid]).to.be(true);

            // Check not matching docs
            expect(explorer.isVisible[shelob.uuid]).to.be(false);
            expect(explorer.isVisible[smaug.uuid]).to.be(false);

            // Check matching folder
            expect(explorer.isVisible[agents_of_chaos.uuid]).to.be(true);
            
            // Check a matching doc
            //expect(explorer.isVisible[evilTrinkets.uuid]).to.be(true);

            // // Check a folder that doesn't contain a matching
            // expect(explorer.isVisible[ridersOfRohan.uuid]).to.be(false);

            // // Check a non matching doc
            // expect(explorer.isVisible[sam.uuid]).to.be(false);
        });
    });
});