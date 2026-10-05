# String Problems — Interview Prep

Ten classic string problems that appear frequently in technical interviews.

---

## Problem 1: Valid Anagram

**Difficulty:** Easy

### Problem Statement
Given two strings `s` and `t`, return `true` if `t` is an anagram of `s`, and `false` otherwise.
An anagram uses exactly the same characters as the original, just rearranged.

### Examples
```
Input:  s = "anagram", t = "nagaram"
Output: true

Input:  s = "rat", t = "car"
Output: false
```

### Constraints
- 1 <= s.length, t.length <= 5 * 10^4
- s and t consist of lowercase English letters

### Brute Force
Sort both strings and compare. If they're equal, they're anagrams.
- Time: O(n log n) due to sorting
- Space: O(n) for the sorted copies

### Optimal Approach — Character Frequency Array
Use an integer array of size 26 (one slot per lowercase letter).
1. Increment count for each character in s
2. Decrement count for each character in t
3. If any count != 0, strings are not anagrams

**Why 26-element array instead of HashMap?** Constant-size array with direct index lookup
is faster in practice than HashMap for known character sets.

- Time: O(n) — two passes through strings
- Space: O(1) — fixed-size array of 26

### Edge Cases
- Different lengths: immediately return false
- Same string: always an anagram of itself
- Single character strings

---

## Problem 2: Reverse String In Place

**Difficulty:** Easy

### Problem Statement
Write a function that reverses a string. The input is given as an array of characters.
Modify in-place with O(1) extra memory.

### Examples
```
Input:  s = ['h','e','l','l','o']
Output: ['o','l','l','e','h']

Input:  s = ['H','a','n','n','a','h']
Output: ['h','a','n','n','a','H']
```

### Constraints
- 1 <= s.length <= 10^5
- s[i] is a printable ASCII character

### Approach — Two Pointers
Start with pointers at both ends. Swap characters, then move both pointers inward.
Stop when pointers meet or cross.

- Time: O(n)
- Space: O(1)

### Edge Cases
- Empty array or single character: no swaps needed
- Palindrome: still reverses correctly (looks the same)

---

## Problem 3: Palindrome Check

**Difficulty:** Easy

### Problem Statement
A phrase is a palindrome if, after converting all uppercase letters to lowercase and
removing all non-alphanumeric characters, it reads the same forward and backward.
Return true if the given string s is a palindrome.

### Examples
```
Input:  s = "A man, a plan, a canal: Panama"
Output: true

Input:  s = "race a car"
Output: false

Input:  s = " "
Output: true (empty after filtering → palindrome)
```

### Constraints
- 1 <= s.length <= 2 * 10^5
- s consists of printable ASCII characters

### Approach — Two Pointers
1. Use left and right pointers starting at ends
2. Skip non-alphanumeric characters
3. Compare characters (case-insensitive) when both pointers land on alphanumeric chars
4. If any mismatch, return false; otherwise return true

- Time: O(n)
- Space: O(1) — no extra string built

### Edge Cases
- All punctuation/spaces: empty after filtering → palindrome
- Single character: palindrome
- Two characters: palindrome iff they're the same (ignoring case)

---

## Problem 4: Longest Substring Without Repeating Characters

**Difficulty:** Medium

### Problem Statement
Given a string `s`, find the length of the longest substring without repeating characters.

### Examples
```
Input:  s = "abcabcbb"
Output: 3  (substring "abc")

Input:  s = "bbbbb"
Output: 1  (substring "b")

Input:  s = "pwwkew"
Output: 3  (substring "wke")
```

### Constraints
- 0 <= s.length <= 5 * 10^4
- s consists of English letters, digits, symbols, and spaces

### Brute Force
Check every substring for duplicate characters.
- Time: O(n²) or O(n³)
- Space: O(min(n, alphabet))

### Optimal Approach — Sliding Window + HashMap
Maintain a window [left, right] with no repeating characters.
When adding a new character causes a repeat, shrink the window from the left until
the repeat is gone.

1. Use a HashMap to store each character's most recent index
2. Expand right pointer one character at a time
3. If `s[right]` was last seen at index `i` and `i >= left` (i.e., it's inside the window):
   - Jump left to `i + 1` (skip past the duplicate)
4. Update max window size: `right - left + 1`

**Key optimization:** Instead of shrinking one-by-one, we jump left directly to
one past the last occurrence of the repeated character.

- Time: O(n) — each character is processed once
- Space: O(min(n, alphabet)) — HashMap bounded by alphabet size

### Edge Cases
- Empty string: return 0
- All unique characters: entire string is the answer
- All same character: answer is 1

---

## Problem 5: String Compression

**Difficulty:** Medium

### Problem Statement
Given an array of characters `chars`, compress it using the following algorithm:
Begin with an empty string. For each group of consecutive repeating characters:
- If the group's length is 1, append the character to the string.
- Otherwise, append the character and then the group's length.
Return the new length of the array. Modify the array in-place with O(1) extra space.

### Examples
```
Input:  chars = ['a','a','b','b','c','c','c']
Output: 6, array modified to ['a','2','b','2','c','3']

Input:  chars = ['a']
Output: 1, array modified to ['a']

Input:  chars = ['a','b','b','b','b','b','b','b','b','b','b','b','b']
Output: 4, array modified to ['a','b','1','2']
```

### Constraints
- 1 <= chars.length <= 2000
- chars[i] is a lowercase letter, uppercase letter, digit, or symbol

### Approach — Two Pointer / Count Runs
Use a write pointer `w` and read pointer `i`:
1. For each run of identical characters, count the run length
2. Write the character at `chars[w]`, increment w
3. If run length > 1, convert count to string and write each digit separately

- Time: O(n)
- Space: O(1)

### Edge Cases
- Single character: no count written
- Run length >= 10: count has multiple digits (e.g., 12 → '1', '2')
- All different characters: each writes just the character

---

## Problem 6: Valid Parentheses

**Difficulty:** Easy
*(Note: This problem also appears in the coding-challenges section.)*

### Problem Statement
Given a string `s` containing only '(', ')', '{', '}', '[', ']', determine if the input
string is valid. A string is valid if:
- Open brackets must be closed by the same type of brackets.
- Open brackets must be closed in the correct order.
- Every close bracket has a corresponding open bracket.

### Examples
```
Input:  s = "()"
Output: true

Input:  s = "()[]{}"
Output: true

Input:  s = "(]"
Output: false

Input:  s = "([)]"
Output: false
```

### Constraints
- 1 <= s.length <= 10^4
- s consists of parentheses only

### Approach — Stack
Push opening brackets onto the stack. When encountering a closing bracket:
- If the stack is empty or the top doesn't match, return false.
- Otherwise, pop the top.

After processing all characters, the string is valid iff the stack is empty.

- Time: O(n)
- Space: O(n) — stack in worst case (all opening brackets)

### Edge Cases
- Empty string: valid (or return true by convention)
- Odd length string: automatically invalid (can short-circuit)
- Only closing brackets: stack is empty when first close appears → false

---

## Problem 7: Reverse Words in a String

**Difficulty:** Medium

### Problem Statement
Given an input string `s`, reverse the order of the words. A word is defined as
a sequence of non-space characters. Words are separated by at least one space.
Return a single space between words, with no leading or trailing spaces.

### Examples
```
Input:  s = "the sky is blue"
Output: "blue is sky the"

Input:  s = "  hello world  "
Output: "world hello"

Input:  s = "a good   example"
Output: "example good a"
```

### Constraints
- 1 <= s.length <= 10^4
- s contains English letters, digits, spaces

### Approach 1 — Split and Join
1. Split s by whitespace (`s.trim().split("\\s+")`)
2. Reverse the resulting array
3. Join with single spaces

- Time: O(n)
- Space: O(n)

### Approach 2 — In-Place Two Pointer (for char arrays)
1. Trim and clean the string (remove extra spaces)
2. Reverse the entire string
3. Reverse each individual word

- Time: O(n)
- Space: O(n) for conversion to char array

### Edge Cases
- Multiple spaces between words (trim/split handles this)
- Leading or trailing spaces
- Single word: return it unchanged

---

## Problem 8: Roman to Integer

**Difficulty:** Easy

### Problem Statement
Given a roman numeral as a string, convert it to an integer.
Roman numeral symbols: I=1, V=5, X=10, L=50, C=100, D=500, M=1000.
Subtraction rule: if a smaller value precedes a larger value, subtract it.
(IV=4, IX=9, XL=40, XC=90, CD=400, CM=900)

### Examples
```
Input:  s = "III"
Output: 3

Input:  s = "LVIII"
Output: 58  (L=50, V=5, III=3)

Input:  s = "MCMXCIV"
Output: 1994  (M=1000, CM=900, XC=90, IV=4)
```

### Constraints
- 1 <= s.length <= 15
- s contains only I, V, X, L, C, D, M
- It is guaranteed that s is a valid roman numeral in range [1, 3999]

### Approach — HashMap + Scan Left to Right
1. Build a map of symbol to value
2. Iterate left to right
3. If the current symbol's value is less than the next symbol's value, subtract it
4. Otherwise, add it

**Why this works:** In a valid Roman numeral, a smaller value before a larger one always
means subtraction. Scanning left to right and checking the next character handles this neatly.

- Time: O(n)
- Space: O(1) — map has fixed 7 entries

### Edge Cases
- Single character: just return its value
- Subtraction at the very last position: impossible in valid Roman numerals

---

## Problem 9: Count and Say

**Difficulty:** Medium

### Problem Statement
The count-and-say sequence is defined as follows:
- countAndSay(1) = "1"
- countAndSay(n) = run-length encoding of countAndSay(n-1)

Run-length encoding: for a string, describe consecutive groups:
"3322251" → "two 3's, three 2's, one 5, one 1" → "23321511"

Return countAndSay(n).

### Examples
```
Input:  n = 1
Output: "1"

Input:  n = 4
Output: "1211"
  n=1: "1"
  n=2: "11"   (one 1)
  n=3: "21"   (two 1s)
  n=4: "1211" (one 2, then one 1)
```

### Constraints
- 1 <= n <= 30

### Approach — Iterative String Building
Start with "1". For each step from 2 to n:
1. Count consecutive identical characters
2. Append count then character to the next string

Use StringBuilder for efficient string construction.

- Time: O(n * m) where m is the length of the nth term
- Space: O(m) for the current and next strings

### Edge Cases
- n = 1: return "1" without any processing
- Long runs: count can be more than 9 theoretically, but in practice stays small

---

## Problem 10: Longest Common Prefix

**Difficulty:** Easy

### Problem Statement
Write a function to find the longest common prefix string amongst an array of strings.
If there is no common prefix, return an empty string "".

### Examples
```
Input:  strs = ["flower","flow","flight"]
Output: "fl"

Input:  strs = ["dog","racecar","car"]
Output: ""
```

### Constraints
- 1 <= strs.length <= 200
- 0 <= strs[i].length <= 200
- strs[i] consists of only lowercase English letters

### Approach — Vertical Scanning
Take the first string as the reference prefix.
For each character position i in the first string:
- Check if strs[j][i] == strs[0][i] for all j
- If any string is shorter than i or has a different character, return strs[0][0..i-1]
If we complete the loop, return the entire first string.

- Time: O(S) where S is the sum of all characters in all strings
- Space: O(1)

### Edge Cases
- Empty array: return ""
- Array with empty string: prefix is ""
- All strings identical: return any of them
- Single character strings: either return that character or ""
