######
Syntax
######

The syntax of Oasis is very simple.

********
Variable Definitons
********

To define a variable, use the ``let`` keyword.

.. code-block:: oasis

    let foo = 1

To assign a new value to a defined variable, no keyword is needed.

.. code-block:: oasis

    let foo = 1
    foo = 2

You can also modify a variable's value by using the different forms of the ``=`` operator.

.. code-block:: oasis

    let foo = 1
    foo = 2
    foo += 1
    foo -= 1

**Relative Expressions**
A relative expression is an expression that is evaluated whenever it's used.
It's titled so because it's an expression relative to another.

.. code-block:: oasis

    let foo = 12
    rel bar = foo * 2

    io:print(bar) // 24
    foo = 13
    io:print(bar) // 26

A relative expression cannot share a name with a variable.
Relative expressions are

********
Indexing
********

To get a property of something, use ``:``.
For example:

.. code-block:: oasis

    // Other languages:
    foo.bar // property bar of foo

    // oasis:
    foo:bar // property bar of foo

To get the index of an array, use ``:(index)``.
For example:

.. code-block:: oasis

    // Other languages:
    foo[12] // element at index 12 of foo

    // oasis:
    foo:(12) // element at index 12 of foo

********
Block Statements
********

For most block statements, a marker for the beginning of a block is not necessary. All blocks must end with the ``end`` keyword.

.. code-block:: oasis

    if 1 == 1
        io:print("woah! 1 is equal to 1!!")
    end

    if 2 == 2
        io:print("woah! 2 is equal to 2!!")
    else
        io:print("woah! 2 is not equal to 2!!")
    end

********
Literals
********

Oasis has string literals, number literals, boolean literals, list literals, dictionary literals, and char literals.

.. code-block:: oasis

    let foo = "hello"
    let bar = 1
    let baz = true
    let qux = [1, 2, 3]
    let quux = {"foo" | "hello", "bar" | 1}
    let corge = 'a'

********
Functions
********

Functions only exist in the form of `function literals.` These are practically lambdas.

.. code-block:: oasis

    let foo = fn(x)
        return x * x
    end

    foo(2) // 4

    // You can also pass functions to functions!

    let bar = fn(x, y)
        return x(y)
    end

    bar(fn(n) return 1 / n end, 5) // 1/5
    // alternatively
    bar(fn(n) => 1 / n, 5)

    // There is a function shorthand, for single-expression functions.
    let square = fn(x) => x * x

********
Prototypes
********

Prototypes also only exist in literal form.

.. code-block:: oasis

    let foo = proto
        x = 2
        y = fn(n)
            return this:x * n
        end
    end

    io:print(foo:x) // 2
    io:print(foo:n(4)) // 8

    // Prototypes can also inherit

    let bar = proto > foo
        z = 5
    end

    io:print(bar:x) // 2
    io:print(bar:y(3)) // 6
    io:print(bar:z) // 5

You can clone a prototype with the ``clone`` keyword.

.. code-block:: oasis

    let foo = proto
        x = 1
    end

    let bar = foo
    foo:x = 3
    io:print(bar:x) // 3

    let baz = clone foo
    foo:x = 5
    io:print(foo:x) // 5
    io:print(bar:x) // 5
    io:print(baz:x) // 3

********
Exceptions
********

To run a block of code and catch any exceptions, use the ``test`` keyword.
The catch code goes in the ``error`` block.

.. code-block:: oasis

    test
        let foo = 1 / 0
    error(e) // you can use '_' to ignore the exception
        io:print("woah! I caught an exception!")
    end

********
Loops
********

For loops have two different forms.
The first one is the traditional for loop.

.. code-block:: oasis

    for let i = 0 | i < 10 | i += 1
        io:print(i)
    end

The second one is the iterator for loop. This is a more modern form of for loop.

.. code-block:: oasis

    for i in range(0, 10)
        io:print(i)
    end

There is also the while loop.

.. code-block:: oasis

    while true
        io:print("woah! I'm in a loop!")
    end

You can also use the ``break`` keyword to break out of a loop.

.. code-block:: oasis

    while true
        io:print("woah! I'm in a loop!")
        break
    end

You can also use the ``continue`` keyword to skip the rest of the loop.

.. code-block:: oasis

    while true
        if true
            continue
        end
        io:print("woah! I'm in a loop!") // this will never print
    end

********
List Comprehensions
********

List comprehensions are syntatic sugar for mapping a function over a list.

.. code-block:: oasis

        let foo = [1, 2, 3, 4, 5]
        let bar = {fn(i) => i * 2 of foo} // [2, 4, 6, 8, 10]

********
Operators
********

Here's a rundown of all of Oasis's operators.

**Arithmetic**

.. code-block:: oasis

    1 + 2 // addition: 3
    1 - 2 // subtraction: -1
    1 * 2 // multiplication: 2
    1 / 2 // division: 0.5
    1 % 2 // modulus: 1

**Directional evaluation**

These are the directional evaluation operators.
They are used to evaluate expressions in a specific direction.
They are always evaluated left-to-right, but depending on the direction of the arrow, it will return the first or last expression.
These are identical to the comma operator in C, but with direction.

.. code-block:: oasis

    1 |> 2 |> 3 // right evaluation: 3
    1 <| 2 <| 3 // left evaluation: 1

**Comparison**

.. code-block:: oasis

    1 == 2 // equality: false
    1 != 2 // inequality: true
    1 < 2 // less than: true
    1 > 2 // greater than: false
    1 <= 2 // less than or equal to: true
    1 >= 2 // greater than or equal to: false

    true and true // logical and: true
    true or false // logical or: true
    not true // logical not: false

    null ? 1 // null coalescing: 1
