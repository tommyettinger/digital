# digital

![digital](docs/logo.png)

Utilities for handling math and showing numbers in Java.

## What is it?

There are only a few classes here, and any given application
only is likely to use even fewer of them. Still, they're
useful as a package, since they often depend on each other.

BitConversion allows converting float and double values to
int and long versions of their underlying bits (and it does
this in a way that works on GWT efficiently). It also has
some methods that convert float-int and double-long with
reversed byte order (using a fast intrinsic on desktop JDKs
and a special trick on GWT), and others that get only the low
or high half of a double's bits as an int. It is modeled after
the NumberUtils class from libGDX, but offers extra methods.
It also has, purely for GWT purposes, an `imul()` method that
acts just like integer multiplication on most platforms, but
compiles down to a call to the JavaScript `Math.imul()`
function on GWT, which automatically handles overflow in the
same way a desktop JVM would.

Base is much larger, and allows converting any Java primitive
number type to a specific base/radix/number-system. Here,
Bases are flexible enough to be able to be generated randomly
for obfuscation purposes (such as to mangle a high score in a
save file). There are several Base constants already defined.
Each one can write numbers as signed (variable-length) or
unsigned (fixed-length), as well as read back either type from
one method. Parsing is significantly more relaxed than in the
JDK, and invalid numbers tend to be returned as 0 rather than
requiring an Exception to be caught. Base has some options
when writing (or reading) a `float` or `double`. Like other
numeric types, you can use `signed()` or `unsigned()` to write
using the digits the Base usually uses, though here it writes
the bits that compose the float or double for higher accuracy.
Unlike other numeric types, there are base-10 `decimal()`,
`scientific()`, `general()`, and `friendly()` methods to write
floats/doubles with different rules for when to switch to
scientific notation, if at all. These can be read back with
`readFloat()` and `readDouble()`, while the signed/unsigned
output needs `readFloatExact()` or `readDoubleExact()`. If you
use a scrambled base (a random one, as mentioned before), then
you need to use `signed()`/`unsigned()`/`readFloatExact()`/
`readDoubleExact()` to use the right scrambled digits. Base
can also combine the String representations of an array of
primitives (or part of such an array, since 0.3.1) using
`join()` or `appendJoined()`, and split apart Strings into 
arrays of primitives with methods like `longSplit()`,
`intSplit()`, and `floatSplit()`.

TrigTools tries to be as complete as possible at covering
trigonometric functions, offering sin, cos, tan, asin, acos,
atan, and atan2 in radians, degrees, and turns. It also allows
access to the lookup table used by sin and cos. Much of
TrigTools can be seen as similar to what libGDX's MathUtils
class offers, but allowing access to the lookup table permits
a few novel features (see its docs). It supports float and
double arguments/returns for all functions. It also provides
"smooth" sin and cos approximations that aren't table-based,
and "smoother" sin/cos/tan versions that interpolate between
entries in the lookup table for very high precision.

MathTools offers a wild grab bag of math functions and
constants, from simple lerp, floor, ceil, and clamp methods to
an optimized cube root function and parameterized splines. It
is also based on MathUtils from libGDX. It supports float and
double arguments/returns for most functions; some make sense
for float only, like the optimized cube root. There's also a
lot of commonly-defined constants, such as the square root of
2 and the golden ratio, as floats and doubles. Some methods
here are useful in other mathematical code, like gamma and
greatestCommonDivisor; others are more esoteric, like
modularMultiplicativeInverse. There are also a few functions
here, "sway" and relatives, that look like sine waves when
graphed, but are simpler to calculate internally.

ArrayTools provides common code for dealing with 2D arrays, and
also sometimes 1D or 3D arrays. It allows copying, inserting,
and filling 2D arrays, and creating ranges of 1D arrays. It also
has a lot of methods for shuffling 1D arrays, 2D arrays, and
sections of 1D arrays, for all primitive types and for objects.
There are also some "filler supplies" in ArrayTools -- methods
for filling up char, int, or String arrays with contents that
are guaranteed to be distinct up to a certain limit. The same
data that can be used to fill up arrays with ArrayTools also
gets used by Hasher for seeding its predefined instances.

Hasher is... large. It provides fast, high-quality hashing
functions for primitive arrays (and arrays of objects, if they
implement hashCode()), and has 64-bit and 32-bit variants. The
specific hashing algorithm it uses is a somewhat-hardened
version of [wyhash](https://github.com/wangyi-fudan/wyhash) that
doesn't use 128-bit math. While it can hash all types of 1D
primitive array and most types of 2D primitive array, it can't
do much with 3D or higher sizes at the moment. However, the only
change that would be needed to add a `hash()` method for, say, a
`float[][][]` is to copy the `hash(float[][])` method and change
the parameter to be a `float[][][]`, so if you have the source,
you can add this yourself with a quick copy and paste. Hasher
allows you to specify the seed when you call `hash()` or
`hash64()` (as static methods), but it also has a fairly-large
array of `predefined` hash functions with instance methods for
`hash()` and `hash64()`. Starting in 0.3.0, Hasher can either
process a whole input array or part of one, specified by a start
index and a length. Hasher also has a few unary hashes that
can be used as quick and dirty random number generators when
applied to numbers in a sequence. The unary hashes can output
longs, bounded ints, floats, and doubles, so they may be useful in
a lot of cases. They are named like `randomize1()`, `randomize2()`,
`randomize3()`, and so on, with higher numbers being typically
slightly slower but also higher-quality (and more permissive of
sets of inputs with atypical patterns).

AlternateRandom is a quick micro-port of a random number generator
from the closely-related [juniper](https://github.com/tommyettinger/juniper)
library. It is used only in ArrayTools here, as the default when
no `Random` object is specified. The alternative would be to use
a `java.util.Random` object, but that can't produce as many
possible shuffles of mid-size arrays, and is slower, both of which
AlternateRandom solves to some extent. If you don't use juniper,
then AlternateRandom is a pretty good replacement for `Random`;
if you do use juniper, then its `WhiskerRandom` or `PasarRandom`
generators are similar to or the same as AlternateRandom's
algorithm, and offer many more features.

ShapeTools provides some predefined mathematical constants for
the vertices and faces of 3D polyhedra (currently, just the 5
Platonic solids). It could be useful for code that needs 3D shapes
in code for something like continuous noise, but doesn't have
access to 3D models.

## How do I get it?

With Gradle, add this to your dependencies (in your core module's
`build.gradle`, for libGDX projects):

```groovy
api "com.github.tommyettinger:digital:0.3.2"
```

If you target GWT using libGDX, you will also need this in your
html module's `build.gradle`:

```groovy
api "com.github.tommyettinger:digital:0.3.2:sources"
```

GWT needs to be told about these changes in your `GdxDefinition.gwt.xml`
file. For digital 0.1.7 and later, use:

```xml
<inherits name="com.github.tommyettinger.digital" />
```

If you are using 0.1.6 or older, **there are probably some GWT
compatibility issues**, but you can try using this, or preferably
updating to 0.1.7 or later:

```xml
<inherits name="digital" />
```

You can also use JitPack to get a recent commit; in that case,
follow [its instructions here](https://jitpack.io/#tommyettinger/digital/).
This also has instructions for Maven and other build tools.

## License

[Apache 2.0](LICENSE). This includes some modified code from
[libGDX](https://github.com/libgdx/libgdx), 
[SquidLib](https://github.com/yellowstonegames/SquidLib),
[SquidSquad](https://github.com/yellowstonegames/SquidSquad),
[Uncommon Maths](https://maths.uncommons.org/),
[wyhash](https://github.com/wangyi-fudan/wyhash),
[Apache Commons Lang](https://github.com/apache/commons-lang),
and [Ryu](https://github.com/ulfjack/ryu). More code
is not from a particular repository (for example, some is from
Wikipedia); see each file for specific author credits. The Ryu
code is more substantial than the other projects, and even
though its license is also Apache 2.0, I included its license
here as [LICENSE-RYU.txt](LICENSE-RYU.txt). The Ryu code that
is relevant to what we use here is also replicated with only
minor compatibility changes in the `src/test` folder.