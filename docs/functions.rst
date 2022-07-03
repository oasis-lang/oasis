######
Functions
######

********
Basics
********

Functions in Oasis are first-class. That means they can be passed as arguments to other functions, and they can be returned by other functions.
They are just like any other value.

Numeric and boolean arguments and return values are passed by value; but prototypes, lists, maps, strings, and functions are passed by reference.

.. code-block:: oasis

    let foo = 12

    foo = fn
        io:print("foo function")
    end

    let f = fn(g, x)
        g(x)
    end

********
Forms
********

Functions can be expressed in multiple ways.

A standard function:

.. code-block:: oasis

    let foo = fn(x, y)
        io:print("I was given " + x + " and " + y + ".")
    end

    foo("hello", "world")

An argumentless function:

.. code-block:: oasis

    let foo = fn
        io:print("I was called.")
    end

    foo()

A lambda function:

.. code-block:: oasis

    let foo = fn(x, y) => x + y
    // these can also take no arguments
    let foo = fn => "hello"

********
Blocks
********

If a function's last operand takes a function, you can use block syntax when calling it.

.. code-block:: oasis

    let foo = fn(x, y, z)
        io:print(x + y)
        z()
    end

    foo(1, 2) do
        io:print("hello")
    end

    // prints "3 hello"
    // One argument is supported, too

    let infinitely = fn(x)
        while true
            infinitely()
        end
    end

    infinitely do
        io:print("hello") // will forever print "hello"
    end

