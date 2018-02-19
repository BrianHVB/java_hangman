## Hangman

A console-based version of the popular Hangman game. Includes both a normal version, and a "devious" version where the computer can cheat, by not selecting a word up front.

The program was designed to illustrate the following:
	* The use of functional programming techniques using features from Java 8
	* Full documentation using Javadoc comments and the `javadoc` tool
	* Test driven development and a full test suite providing a high degree of code coverage
	* Code quality enforcement using the standard Java checkstyle ruleset.
	* Peer review of code quality by others working on similar projects
	* Use of Clean Code principals such as short methods, descriptive names, and other guidelines.
	
### Sections

There are three components to the codebase. The code contained in `src/` is an interface used to drive the program. The code in `normal/` and `/devious` are implementations of the interface, along with additional resources required by the game. In addition to the codebase, a full set of documentatino for both the normal and devious versions is contained in `docs/`

### Authorship

All code except for the game interface was authored by Brian Houle.