import * as React from "react";

import SingleValueWidget from "./SingleValueWidget";
import BetweenValueWidget from "./BetweenValueWidget";
import InValueWidget from "./InValueWidget";
import AppSearchBar from "../../AppSearchBar";
import { ExpressionTerm, ConditionType } from "../../../types";

export interface Props {
  onChange: (value: any) => any;
  term: ExpressionTerm;
  valueType: string;
}

const ValueWidget = ({
  term: { uuid, value, condition },
  onChange,
  valueType
}: Props) => {
  switch (condition) {
    case "CONTAINS":
    case "EQUALS":
    case "GREATER_THAN":
    case "GREATER_THAN_OR_EQUAL_TO":
    case "LESS_THAN":
    case "LESS_THAN_OR_EQUAL_TO": {
      return (
        <SingleValueWidget
          value={value}
          valueType={valueType}
          onChange={onChange}
        />
      );
    }
    case "BETWEEN": {
      return (
        <BetweenValueWidget
          value={value}
          valueType={valueType}
          onChange={onChange}
        />
      );
    }
    case "IN": {
      return <InValueWidget value={value} onChange={onChange} />;
    }
    case "IN_DICTIONARY": {
      return (
        <AppSearchBar
          pickerId={uuid!}
          typeFilters={["Dictionary"]}
          onChange={onChange}
          value={value}
        />
      );
    }
    default:
      throw new Error(`Invalid condition: ${condition}`);
  }
};

// ValueWidget.propTypes = {
//   term: PropTypes.shape({
//     uuid: PropTypes.string.isRequired,
//     condition: PropTypes.string.isRequired
//   }),
//   onChange: PropTypes.func.isRequired,
//   valueType: PropTypes.string.isRequired
// }

export default ValueWidget;