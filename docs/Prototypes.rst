######
Prototypes
######

Prototypes are similar to classes, but they are not types, they are literal instances. A prototype is somewhat equivalent to a static class.
A common misconception is that prototypes are classes and that they are types.
For example:

.. code-block:: oasis

    let foo = proto
        a = 1
        b = "hello!"
        c = fn(x)
            return this:a + x
        end
    end

    io:print(foo:a) // prints 1
    io:print(foo:b) // prints "hello!"
    io:print(foo:c(2)) // prints 3

This prototype contains three members: the number 1, the string "hello!", and a function that adds the value of the member "a" to the argument.

********
Inheritance
********

Prototypes can be inherited from. For example:

.. code-block:: oasis

    let bar = proto : foo
        d = "world!"
    end

    io:print(bar:a) // prints 1
    io:print(bar:b) // prints "hello!"
    io:print(bar:c(2)) // prints 3
    io:print(bar:d) // prints "world!"

Now, let's change something in foo:

.. code-block:: oasis

    foo:a = 2
    io:print(bar:a) // prints 2

This is because foo is the prototype of bar. Bar is a child of foo, so it inherits the changes made to foo.
Bar is not an instance of foo, as prototypes cannot be instantiated, because they are not types.

You can change the prototype of a prototype. Let's say we have 3 prototypes, A, B, and C.

.. code-block:: oasis

    let A = proto
        a_value = 1
    end

    let B = proto
        b_value = 2
    end

    let C = proto
        c_value = 3
    end

All of these prototypes are currently independent of each other.

We can change the prototype of C to A:

.. code-block:: oasis

    prototype:setPrototypeOf(C, A)

    io:print(C:a_value) // prints 1
    io:print(C:b_value) // undefined, as B is not the prototype of C
    io:print(C:c_value) // prints 3

And, the changes made to A are also applied to C.

.. code-block:: oasis

    A:another_value = 4
    io:print(C:another_value) // prints 4

Now, we can change the prototype of C to B:

.. code-block:: oasis

    prototype:setPrototypeOf(C, B)

    io:print(C:another_value) // undefined, as A is no longer the prototype of C
    io:print(C:a_value) // undefined, as A is no longer the prototype of C
    io:print(C:b_value) // prints 2
    io:print(C:c_value) // prints 3

The rule is that if a prototype is modified, all of its children are also modified. However, if a child is modified, its parent is not modified.
When the parent of a prototype is changed, all of the values in the parent are removed from the child, and all of the values in the new parent are added to the child.

********
Cloning
********

Prototypes can be cloned. The clone of a prototype is entirely detached from the original. Any changes made to the clone will not affect the original, and vice versa.

.. code-block:: oasis

    let foo = proto
        a = 1
        b = "hello!"
    end

    let bar = clone foo
    bar:a = 2
    io:print(bar:a) // prints 2

    io:print(foo:a) // prints 1

********
Overloads
********

Prototypes can have overloaded functions. For example:

.. code-block:: oasis

    let foo = proto
        a = 5
        __add = fn(x) => this:a + x
    end

    io:print(foo + 2) // prints 7

********
Overloadable Members
********

**__add, __sub, __mul, __div, __mod, __and, __or**

Add, subtract, multiply, divide, modulus, logical and, and logical or.

**__serialize**

This function is called when the prototype is serialized. It must return a hashmap.

.. code-block:: oasis

    let foo = proto
        a = 5
        __serialize = fn
            return {
                "a" | this:a,
                "b" | this:a + 2
            }
        end
    end

    io:print(json:dump(foo)) // prints "{ "a": 5, "b": 7 }"

**__index**

This function is called when the prototype is indexed.

.. code-block:: oasis

    let foo = proto
        a = [1, 2, 3]
        __index = fn(x)
            return this:a:(x)
        end
    end

    io:print(foo:a) // prints "[1, 2, 3]"
    io:print(foo:(2)) // prints "3"

**__setIndex**

This function is called when an index of the prototype is assigned.

.. code-block:: oasis

    let foo = proto
        a = [1, 2, 3]
        __setIndex = fn(x, y)
            this:a:(x) = y
        end
    end

    io:print(foo:a) // prints "[1, 2, 3]"
    foo:(2) = 4
    io:print(foo:a) // prints "[1, 2, 4]"

**__iterator**

This function is called when the prototype is iterated. It accepts a numeric value for index.
Once the index is out of range, the function must call the `iteratorExhausted` function.

.. code-block:: oasis

    let foo = proto
        a = [1, 2, 3]
        __iterator = fn(x)
            if x < this:a:size()
                return this:a:(x)
            else
                iteratorExhausted()
            end
        end
    end

    for i in foo
        io:print(i)
    end

**toString**

This function is called when the prototype is converted to a string.
It must return a string.

.. code-block:: oasis

    let foo = proto
        a = 5
        toString = fn
            return "a value is: " + this:a
        end
    end

    io:print(foo) // prints "a value is: 5"