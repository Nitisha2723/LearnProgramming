"""
test_data_structures.py — pytest tests for Module 04: Data Structures

Run with:
    pytest tests/test_data_structures.py -v
    pytest tests/test_data_structures.py -v --tb=short

Tests cover:
  - list slicing and comprehensions
  - dict operations and Counter
  - set operations
  - deque behavior
  - namedtuple
  - defaultdict patterns
"""

import pytest
from collections import Counter, defaultdict, deque, namedtuple


# =============================================================================
# LIST TESTS
# =============================================================================

class TestListSlicing:

    def test_basic_slice(self):
        lst = list(range(10))
        assert lst[2:5] == [2, 3, 4]

    def test_slice_to_end(self):
        lst = list(range(10))
        assert lst[7:] == [7, 8, 9]

    def test_slice_from_start(self):
        lst = list(range(10))
        assert lst[:3] == [0, 1, 2]

    def test_step_two(self):
        lst = list(range(10))
        assert lst[::2] == [0, 2, 4, 6, 8]

    def test_reverse_slice(self):
        lst = list(range(5))
        assert lst[::-1] == [4, 3, 2, 1, 0]

    def test_negative_index(self):
        lst = [10, 20, 30, 40, 50]
        assert lst[-2:] == [40, 50]

    @pytest.mark.parametrize("step, expected", [
        (1,  [0, 1, 2, 3, 4]),
        (2,  [0, 2, 4]),
        (3,  [0, 3]),
        (-1, [4, 3, 2, 1, 0]),
    ])
    def test_step_values(self, step, expected):
        lst = list(range(5))
        assert lst[::step] == expected


class TestListComprehensions:

    def test_basic_comprehension(self):
        result = [x * 2 for x in range(5)]
        assert result == [0, 2, 4, 6, 8]

    def test_filtered_comprehension(self):
        result = [x for x in range(10) if x % 2 == 0]
        assert result == [0, 2, 4, 6, 8]

    def test_string_transformation(self):
        words = ["hello", "world", "python"]
        result = [w.upper() for w in words if len(w) > 4]
        assert result == ["HELLO", "WORLD", "PYTHON"]

    def test_flatten_matrix(self):
        matrix = [[1, 2], [3, 4], [5, 6]]
        flat = [x for row in matrix for x in row]
        assert flat == [1, 2, 3, 4, 5, 6]

    def test_filter_none(self):
        data = [1, None, 2, None, 3]
        clean = [x for x in data if x is not None]
        assert clean == [1, 2, 3]

    @pytest.mark.parametrize("n, expected", [
        (3, [0, 1, 4]),
        (5, [0, 1, 4, 9, 16]),
        (0, []),
    ])
    def test_squares_comprehension(self, n, expected):
        result = [x**2 for x in range(n)]
        assert result == expected


class TestListSorting:

    def test_sort_ascending(self):
        lst = [3, 1, 4, 1, 5, 9, 2, 6]
        assert sorted(lst) == [1, 1, 2, 3, 4, 5, 6, 9]

    def test_sort_descending(self):
        lst = [3, 1, 4, 1, 5]
        assert sorted(lst, reverse=True) == [5, 4, 3, 1, 1]

    def test_sort_by_length(self):
        words = ["banana", "fig", "apple", "kiwi"]
        result = sorted(words, key=len)
        assert result == ["fig", "kiwi", "apple", "banana"]

    def test_sort_stable(self):
        # Items with same key should maintain original order
        data = [("b", 1), ("a", 1), ("c", 2)]
        result = sorted(data, key=lambda x: x[1])
        assert result == [("b", 1), ("a", 1), ("c", 2)]

    def test_sort_does_not_modify_original(self):
        original = [3, 1, 2]
        _ = sorted(original)
        assert original == [3, 1, 2]

    def test_sort_modifies_in_place(self):
        lst = [3, 1, 2]
        result = lst.sort()
        assert result is None        # sort() returns None
        assert lst == [1, 2, 3]     # modified in place


# =============================================================================
# DICT TESTS
# =============================================================================

class TestDictOperations:

    def test_get_existing_key(self):
        d = {"a": 1, "b": 2}
        assert d.get("a") == 1

    def test_get_missing_key_returns_none(self):
        d = {"a": 1}
        assert d.get("z") is None

    def test_get_missing_key_returns_default(self):
        d = {"a": 1}
        assert d.get("z", 42) == 42

    def test_dict_comprehension(self):
        result = {x: x**2 for x in range(1, 4)}
        assert result == {1: 1, 2: 4, 3: 9}

    def test_dict_comprehension_filter(self):
        grades = {"Alice": 85, "Bob": 50, "Carol": 90}
        passing = {k: v for k, v in grades.items() if v >= 70}
        assert passing == {"Alice": 85, "Carol": 90}

    def test_dict_inversion(self):
        original = {"a": 1, "b": 2, "c": 3}
        inverted = {v: k for k, v in original.items()}
        assert inverted == {1: "a", 2: "b", 3: "c"}

    def test_dict_merge_operator(self):
        d1 = {"a": 1, "b": 2}
        d2 = {"b": 99, "c": 3}
        merged = d1 | d2
        assert merged == {"a": 1, "b": 99, "c": 3}

    def test_dict_maintains_insertion_order(self):
        d = {}
        for key in ["c", "a", "b"]:
            d[key] = key
        assert list(d.keys()) == ["c", "a", "b"]

    def test_setdefault(self):
        d = {}
        result = d.setdefault("key", [])
        result.append(1)
        d.setdefault("key", []).append(2)
        assert d["key"] == [1, 2]


class TestCounter:

    def test_count_string(self):
        c = Counter("aabbbcccc")
        assert c["a"] == 2
        assert c["b"] == 3
        assert c["c"] == 4

    def test_missing_key_returns_zero(self):
        c = Counter("abc")
        assert c["z"] == 0

    def test_most_common(self):
        c = Counter("abracadabra")
        top = c.most_common(1)
        assert top[0][0] == "a"
        assert top[0][1] == 5

    def test_counter_addition(self):
        a = Counter("abc")
        b = Counter("bbc")
        result = a + b
        assert result["b"] == 3
        assert result["a"] == 1

    def test_counter_subtraction_drops_negatives(self):
        a = Counter({"a": 3, "b": 1})
        b = Counter({"a": 1, "b": 2})
        result = a - b
        assert result["a"] == 2
        assert "b" not in result   # 1 - 2 = -1, dropped

    @pytest.mark.parametrize("text, expected_top", [
        ("aaabbc", [("a", 3), ("b", 2), ("c", 1)]),
        ("",       []),
        ("xyz",    []),  # all count 1, order may vary but length is 3
    ])
    def test_most_common_parametrized(self, text, expected_top):
        c = Counter(text)
        if expected_top:
            top = c.most_common(len(expected_top))
            # Check counts (not necessarily order for ties)
            actual_counts = {k: v for k, v in top}
            for word, count in expected_top:
                assert actual_counts.get(word) == count


# =============================================================================
# SET TESTS
# =============================================================================

class TestSetOperations:

    def test_union(self):
        a = {1, 2, 3}
        b = {3, 4, 5}
        assert a | b == {1, 2, 3, 4, 5}

    def test_intersection(self):
        a = {1, 2, 3, 4}
        b = {3, 4, 5, 6}
        assert a & b == {3, 4}

    def test_difference(self):
        a = {1, 2, 3, 4}
        b = {3, 4, 5}
        assert a - b == {1, 2}

    def test_symmetric_difference(self):
        a = {1, 2, 3}
        b = {2, 3, 4}
        assert a ^ b == {1, 4}

    def test_subset(self):
        assert {1, 2} <= {1, 2, 3}
        assert not {1, 2, 5} <= {1, 2, 3}

    def test_superset(self):
        assert {1, 2, 3} >= {1, 2}

    def test_isdisjoint(self):
        a = {1, 2}
        b = {3, 4}
        assert a.isdisjoint(b)
        assert not a.isdisjoint({2, 5})

    def test_set_deduplication(self):
        lst = [1, 2, 2, 3, 3, 3]
        assert set(lst) == {1, 2, 3}

    def test_frozenset_as_dict_key(self):
        d = {frozenset({"a", "b"}): 1}
        assert d[frozenset({"b", "a"})] == 1   # Order doesn't matter

    @pytest.mark.parametrize("a, b, expected_intersection", [
        ({1, 2, 3}, {2, 3, 4}, {2, 3}),
        ({1, 2},    {3, 4},    set()),
        ({1, 2, 3}, {1, 2, 3}, {1, 2, 3}),
    ])
    def test_intersection_parametrized(self, a, b, expected_intersection):
        assert a & b == expected_intersection


# =============================================================================
# DEQUE TESTS
# =============================================================================

class TestDeque:

    def test_append_and_pop(self):
        d = deque([1, 2, 3])
        d.append(4)
        assert d.pop() == 4
        assert list(d) == [1, 2, 3]

    def test_appendleft_and_popleft(self):
        d = deque([2, 3, 4])
        d.appendleft(1)
        assert d.popleft() == 1
        assert list(d) == [2, 3, 4]

    def test_maxlen(self):
        d = deque(maxlen=3)
        for i in range(6):
            d.append(i)
        assert list(d) == [3, 4, 5]
        assert len(d) == 3

    def test_rotate_right(self):
        d = deque([1, 2, 3, 4, 5])
        d.rotate(2)
        assert list(d) == [4, 5, 1, 2, 3]

    def test_rotate_left(self):
        d = deque([1, 2, 3, 4, 5])
        d.rotate(-1)
        assert list(d) == [2, 3, 4, 5, 1]

    def test_fifo_queue(self):
        q = deque()
        q.append("a")
        q.append("b")
        q.append("c")
        assert q.popleft() == "a"
        assert q.popleft() == "b"
        assert q.popleft() == "c"


# =============================================================================
# DEFAULTDICT TESTS
# =============================================================================

class TestDefaultdict:

    def test_defaultdict_int(self):
        dd = defaultdict(int)
        dd["count"] += 1
        dd["count"] += 1
        assert dd["count"] == 2
        assert dd["missing"] == 0   # Not KeyError

    def test_defaultdict_list(self):
        dd = defaultdict(list)
        dd["key"].append(1)
        dd["key"].append(2)
        assert dd["key"] == [1, 2]
        assert dd["other"] == []    # Not KeyError

    def test_grouping_pattern(self):
        items = [("a", 1), ("b", 2), ("a", 3), ("b", 4)]
        groups = defaultdict(list)
        for key, val in items:
            groups[key].append(val)
        assert groups["a"] == [1, 3]
        assert groups["b"] == [2, 4]

    def test_counting_pattern(self):
        words = ["cat", "dog", "cat", "bird", "dog", "cat"]
        counts = defaultdict(int)
        for word in words:
            counts[word] += 1
        assert counts["cat"] == 3
        assert counts["dog"] == 2
        assert counts["bird"] == 1


# =============================================================================
# NAMEDTUPLE TESTS
# =============================================================================

class TestNamedtuple:

    def setup_method(self):
        """Create test namedtuple types."""
        self.Point = namedtuple("Point", ["x", "y"])
        self.Student = namedtuple("Student", ["name", "grade", "gpa"])

    def test_access_by_name(self):
        p = self.Point(3, 7)
        assert p.x == 3
        assert p.y == 7

    def test_access_by_index(self):
        p = self.Point(3, 7)
        assert p[0] == 3
        assert p[1] == 7

    def test_unpacking(self):
        p = self.Point(10, 20)
        x, y = p
        assert x == 10
        assert y == 20

    def test_replace(self):
        s = self.Student("Alice", 10, 3.8)
        promoted = s._replace(grade=11)
        assert promoted.grade == 11
        assert promoted.name == "Alice"   # Unchanged
        assert s.grade == 10              # Original unchanged

    def test_asdict(self):
        s = self.Student("Bob", 11, 3.2)
        d = s._asdict()
        assert d["name"] == "Bob"
        assert d["grade"] == 11

    def test_hashable(self):
        """namedtuples are hashable (can be used in sets and as dict keys)."""
        p1 = self.Point(1, 2)
        p2 = self.Point(3, 4)
        point_set = {p1, p2}
        assert len(point_set) == 2

    def test_immutable(self):
        p = self.Point(1, 2)
        with pytest.raises(AttributeError):
            p.x = 99
