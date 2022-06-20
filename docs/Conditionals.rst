######
Conditionals and Loops
######

********
If Statements
********

An if statement in Oasis is pretty traditional.

.. code-block:: oasis

    if condition
        //do something
    end

    if condition
        //do something
    else
        //do something else
    end

    if condition
        //do something
    else if condition
        //do something else
    else
        //do something else
    end

These can also be inlined.

.. code-block:: oasis

    // You might do this:
    let a = nil
    if 1 == 1
        a = 25
    else
        a = 42
    end

    // However, this is more readable:
    let a = if 1 == 1 => 25 else 42

********
'Is' Statements
********

An ``is`` statement is similar to a switch statement in other languages.

.. code-block:: oasis

    let x = 3
    is x
        1 =>
            io:print("x is 1")
        end
        2 =>
            io:print("x is 2")
        end
        3 =>
            io:print("x is 3")
        end
    end

    // prints "x is 3"

********
While Loops
********

While loops are also pretty simple.

.. code-block:: oasis

    let i = 0
    while i < 10
        io:print(i)
        i = i + 1
    end

    // prints 0 1 2 3 4 5 6 7 8 9

You can use break and continue to control the loop.

.. code-block:: oasis

    let i = 0
    while i < 10
        if i == 5
            break
        end
        io:print(i)
        i = i + 1
    end

    // prints 0 1 2 3 4

********
For Loops
********

The for loop has two forms.

.. code-block:: oasis

    for i in range(0, 10)
        io:print(i)
    end

    // prints 0 1 2 3 4 5 6 7 8 9

    for let i = 0 | i < 10 | i = i + 1
        io:print(i)
    end

    // prints 0 1 2 3 4 5 6 7 8 9

In the first form, the loop iterates over a prototype that implements ``__iterator``. The second form is a more traditional for loop.
