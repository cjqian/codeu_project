# Wikipedia Search Engine: CodeU Final Project
We built a search engine. [Here's](https://docs.google.com/document/d/126A0hboSQHDoLtKBN4XXQaUpu2sFSa4QaAoRYJn2xt0/edit) our design doc, and [here's](https://docs.google.com/presentation/d/1UpwkJSePXlG6T-iu_4cDtfFVPj_RxTep6tZYf1DG3YI) our presentation. 
## Usage

Here's a screencap of the expected output.
![Screencap showing expected usage.](https://puu.sh/qw9eP/4bbcce3de4.png)
### Command Syntax
When in the `javac-lab12` directory, queries can be made to the interface by 
```
$ ant build
$ ant QueryHandler -Dquery="[query]"
```

Make sure that you're using `-Dquery` and not `DQuery`! The `build.xml` is case-sensitive.

### Query Syntax
Queries are read in [CNF](https://en.wikipedia.org/wiki/Conjunctive_normal_form) (conjunctive normal form), with the following requirements:

- Currently, multi-word queries are not accepted.
- The tokens "AND" and "OR" are used to separate terms.
- Open and closed parentheses MUST be separated with spaces!
- Single word queries do not need parentheses. However, multi-value queries will require parentheses.

Here are some acceptable queries:
```
java
( java )
( java AND programming )
( java AND programming ) OR ( language )
( java and programming ) OR ( java and language )
```

Here are some unacceptable queries:
```
java AND                [1]
(java AND programming)  [2]
java AND programming    [3]
(java OR programming)   [4]
```
Why?
1. The "AND" token is not used to separate terms.
2. The open/closed parentheses are not separated by spaces.
3. No parentheses are used in a multi-term query.
4. This is not correct CNF-notation.


## Future Steps
For the interface specifically, we hope to eventually do the following:
1. Implement a more sophisticated DFA token parser so there are less requirements on the syntax of input parameters.
2. Allow flags (for example, `--show-urls` or `--disable-browser`) that control program execution.
3. Design a way for multiple queries to be handled without exiting the program.

Issues and pull requests are welcome. 