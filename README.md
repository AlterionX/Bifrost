# Bifrost RegEx, Lexer, and Parser #

## Summary ##

This compilation engine aims to take in a set of configuration files for a language, and then compile assembly code for the language to be recompiled through an assembler.

Due to heavy use of lambda expressions in certain areas of the project, this requires Java 8. The sample project located in the tyrion subdirectory does not compile, however it can generate a full syntax tree and modify a limited subset into three-address code.

My apologies for the names.

## General Structure ##

* The engine symbol table: `Yggdrasil`
* The lexer: `Helvegar`
  * The regex engine: `Bragi`
  * The regex is actually called `Skald`
* The parser: `Jormungandr`
* The AST: `Yggdrasil`
  * The tree walker interface(abstract class): `Stag`
* The semantics analyzer: `Heimdallr`
  * The symbol table: `Nidhogg`
  * User provided extensions of `Stag`, the tree walker class
* Intermediate code generator: `Hoenir`
  * A subclass of Stag as well
* Assembly compiler: `Surtr`
* Assembler caller: `Idavoll`

The one class that inherently ties everything together is `Yggdrasil`. This is the basis of all the classes.

## General theory and Project Status ##

This compiler breaks the process of compiling into seven separate stages, lexical analysis, syntactic analysis, global (structural) semantics analysis, local semantics analysis, intermediate code generation, intermediate code optimization, machine code generation and machine code optimization.

These can be further, loosely, categorized into the lexical, syntactic, semantic, and machine analysis stages.

Currently implemented: simple regular expression engine, regular expression based lexer, simple SLR parser table generator, simple SLR parser, multiple symbol tables, for the engine and for the program being compiled. There is an interface for tree walking, with dynamically compiled Java classes available through configuration. Tree modification at parsing time.

Of the seven stages, the first four are at a basic, working standard, provided well-formatted configuration files.

Being worked on: Parser and Lexer serialization, syntatic and semantic analysis engine, simple intermediate code optimization engine, simple machine code optimization engine.

Future goals: LALR, LR parsing, a general fall back algorithm (Earley, CYK, etc.). Fully working sample project. Type-checking engine. Advanced versions of the other componenets. Alternative and extended input formats. Support literals within the context free grammar parser. Standardization of input formats. Improved performance.

Project on hold for various IRL issues.

## Usage ##

### Auxilary file list ###

|File extension |Engine target |Meaning                                |
|---------------|--------------|---------------------------------------|
|`.lexdec`      |`Helvegar`    |Lexer declaration                      |
|`.pardec`      |`Jormungandr` |Parser declaration                     |
|`.anadec`      |`Heimdallr`   |Analyzer declaration                   |
|`.scrdec`      |`ScopeChanger`|Scope resolution declaration           |
|`.irldec`      |`Hoenir`      |Intermediate Machine Format declaration|
|`.mltdec`      |`Surtr`       |Machine type, class, info declaration  |
|`.asmrcall`    |`Idavoll`     |Assembler caller                       |

### RegEx Engine ###

The command line interface `Bragi` is available for testing RegEx with the engine, to ensure a match. Unfortunately, `Bragi` does not take multi-line inputs, and will not search an entire `String`. The fix for the second is quick, but the first will take a little longer.

### Lexer ###

The Lexer takes a single file name `target.lexdec`. It is in another section. It will, in effect, take symbols and regex representations of those symbols, passing them onwards to the parser. The symbols will be added as terminals to the list of symbols.

### Parser ###

The Lexer takes a single file name `target.pardec` as the configuration file. This file's format is listed below. In brief, it will contain a CFG written in some format as well as additional commands for the parse tree builder. It will use symbols listed in the lexer's declaration as terminals, and any new tokens, as defined below, to be non-terminals.

### Semantic Analyzer ###

There are two components, one for user modification of the tree and symbol table, another for translation into intermediate code representation. Both traverse the AST by utilizing the `Stag` interface.

The first can have its behavior modified by the `target.anadec` file and corresponding tree walkers. The second component utilizes the `target.irldec` file to define an intermediate language. `target.scrdec` is used to control stepping through scopes within the AST.

### Machine Language Translator ###

Hahaha. I don't think this is going to be done anytime soon. It's being worked on.

## License ##

This is licensed under the MIT license. See LICENSE.md.
