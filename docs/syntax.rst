######
Syntax
######

The syntax of Oasis is very simple.
What is commonly the dot operator in other programming languages is the colon in Oasis.
For example:
.. code-block::

    // Other languages:
    foo.bar // property bar of foo

    // oasis:
    foo:bar // property bar of foo

For most block statements, a marker for the beginning of a block is not necessary. All blocks must end with the `end` keyword.
.. code-block::

    if 1 == 1
        io:print("woah! 1 is equal to 1!!")
    end

