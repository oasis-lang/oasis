Getting Started
===============

Welcome to the Oasis programming language! Oasis is a prototype-based, scripting language inspired by Lua, Python, and Self.
The main Oasis interpreter, ``oasiskt``, is written in Kotlin.

To use Oasis, you must have Java (>= 16) installed.

********
To Build
********

Clone the repository:

    git clone https://github.com/oasis-lang/oasis

Enter the Oasis directory and build:

    cd oasis/
    ./gradlew build

After a little while, you should have a built Jarfile in `build/libs`.
To run the Jarfile:

    java -jar oasiskt.jar

You should be prompted with an `oasis -> ` prompt. Try running something:

    oasis -> 1 + 1
    2
    oasis -> io:print("Hello, world!")
    Hello, world!
    oasis ->
