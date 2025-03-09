using System;
using System.Collections.Generic;
using System.Linq;

namespace Autocomplete
{
    public static class LeftBorderTask
    {
        public static int GetLeftBorderIndex(IReadOnlyList<string> phrases, string prefix, int left, int right)
        {
            if (phrases.Count == 0)
                return 0;

            if (left == right)
                return left;

            var middle = (left + right) / 2;

            if (string.Compare(phrases[middle], prefix, StringComparison.InvariantCultureIgnoreCase) < 0)
                return GetLeftBorderIndex(phrases, prefix, middle + 1, right);
            else
                return GetLeftBorderIndex(phrases, prefix, left, middle);
        }

        public static int GetLeftBorderIndex(IReadOnlyList<string> phrases, string prefix)
        {
            return GetLeftBorderIndex(phrases, prefix, -1, phrases.Count - 1);
        }
    }
} 