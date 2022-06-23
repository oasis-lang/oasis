Getting Started
===============

Welcome to the Oasis programming language! Oasis is a prototype-based, scripting language inspired by Lua, Python, and Self.
The main Oasis interpreter, ``oasiskt``, is written in Kotlin.

To use Oasis, you must have Java (>= 11) installed.

********
To Install
********

TODO: Windows installer, MacOS installer, Linux packages

********
To Build
********

Clone the repository:

.. code-block::

    git clone https://github.com/oasis-lang/oasis

Enter the Oasis directory and build:

.. code-block::

    cd oasis/
    ./gradlew build

After a little while, you should have a built Jarfile in ``build/libs``.
To run the Jarfile:

.. code-block::

    java -jar oasiskt.jar

You should be prompted with an ``oasis ->`` prompt. Try running something:

.. code-block::

    oasis -> 1 + 1
    2
    oasis -> io:print("Hello, world!")
    Hello, world!
    oasis ->

To use various Oasis tooling, such as the language server, you must define the OASIS_HOME environment variable.
This is a path to the Oasis jarfile.

* Windows

    See `this page (external site) <https://docs.oracle.com/en/database/oracle/machine-learning/oml4r/1.5.1/oread/creating-and-modifying-environment-variables-on-windows.html>`_ for more information. OASIS_HOME is a user variable.

* Linux and MacOS

    See `this page (external site) <https://unix.stackexchange.com/a/117470>`_ for more information.