import React from 'react';

import { compose } from 'recompose';
import { connect } from 'react-redux';
import { Input, Dropdown } from 'semantic-ui-react';

import { actionCreators as appSearchBarActionCreators } from './redux';
import { searchApp } from 'components/FolderExplorer/explorerClient';
import withOpenDocRef from 'sections/RecentItems/withOpenDocRef';
import { DocRefListingEntry } from 'components/DocRefListingEntry';
import { withDocRefTypes } from 'components/DocRefTypes';
import withSelectableItemListing, { actionCreators as selectableItemListingActionCreators } from 'lib/withSelectableItemListing';
import { withInputKeyDown, actionCreators as keyIsDownActionCreators, FOCUSSED_ELEMENTS } from 'lib/KeyIsDown';

const {
  searchTermUpdated,
  searchDocRefTypeChosen
} = appSearchBarActionCreators;

const {
  elementFocussed,
  elementBlurred
} = keyIsDownActionCreators;

const LISTING_ID = 'app-search-bar';

const enhance = compose(
  withOpenDocRef,
  withDocRefTypes,
  withInputKeyDown,
  connect(
    ({
      appSearch: {
        searchTerm, searchDocRefType, searchResults, selectedIndex,
      },
    }, props) => ({
      searchValue:
        searchTerm.length > 0 ? searchTerm : searchDocRefType ? `type:${searchDocRefType}` : '',
      selectedIndex,
      searchResults,
    }),
    {
      searchApp,
      searchTermUpdated,
      searchDocRefTypeChosen,
      elementFocussed,
      elementBlurred
    },
  ),
  withSelectableItemListing(({searchResults}) => ({
    listingId: LISTING_ID,
    items: searchResults,
    autofocus: false
  }))
);

const AppSearchBar = ({
  searchResults,
  selectedIndex,
  searchApp,
  openDocRef,
  searchValue,
  searchTermUpdated,
  searchDocRefTypeChosen,
  history,
  docRefTypes,
  onInputKeyDown,
  elementFocussed,
  elementBlurred                                                                                 
}) => (
  <Dropdown
    fluid
    icon={null}
    trigger={
      <Input
        fluid
        className="AppSearch__search-input"
        icon="search"
        placeholder="Search..."
        value={searchValue}
        onKeyDown={onInputKeyDown}
        onFocus={() => elementFocussed(FOCUSSED_ELEMENTS.DOC_REF_LISTING, LISTING_ID)}
        onBlur={() => elementBlurred(FOCUSSED_ELEMENTS.DOC_REF_LISTING, LISTING_ID)}
        onChange={({ target: { value } }) => {
          searchTermUpdated(value);
          searchApp({ term: value });
        }}
      />
    }
  >
    <Dropdown.Menu className="AppSearch__menu">
      {searchResults.length === 0 &&
        docRefTypes.map(docRefType => (
          <Dropdown.Item
            className="AppSearch__dropdown-item"
            key={docRefType}
            onClick={() => {
              searchApp({ docRefType });
              searchDocRefTypeChosen(docRefType);
            }}
          >
            <img
              className="stroom-icon--small"
              alt="X"
              src={require(`../../images/docRefTypes/${docRefType}.svg`)}
            />
            {docRefType}
          </Dropdown.Item>
        ))}
      {searchResults.length > 0 &&
        searchResults.map((searchResult, index) => (
          <DocRefListingEntry
            key={searchResult.uuid}
            index={index}
            listingId={LISTING_ID}
            docRef={searchResult}
            openDocRef={openDocRef}
            includeBreadcrumb
          />
        ))}
    </Dropdown.Menu>
  </Dropdown>
);

const EnhancedAppSearchBar = enhance(AppSearchBar);

EnhancedAppSearchBar.propTypes = {};

export default EnhancedAppSearchBar;