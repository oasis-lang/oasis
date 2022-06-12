######
Syntax
######

The syntax of Oasis is very simple.

To define a variable, use the `let` keyword.

.. code-block:: oasis

    let foo = 1

To assign a new value to a defined variable, no keyword is needed.

.. code-block:: oasis

    let foo = 1
    foo = 2

To get a property of something, use `:`.
For example:

.. code-block:: oasis

    // Other languages:
    foo.bar // property bar of foo

    // oasis:
    foo:bar // property bar of foo

For most block statements, a marker for the beginning of a block is not necessary. All blocks must end with the `end` keyword.

.. code-block:: oasis

    if 1 == 1
        io:print("woah! 1 is equal to 1!!")
    end

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

You can clone a prototype with the `clone` keyword.

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

