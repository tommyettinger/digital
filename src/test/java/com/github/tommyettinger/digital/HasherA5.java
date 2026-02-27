package com.github.tommyettinger.digital;

/**
 * A variant on Hasher that uses the a5hash32 algorithm, which is meant for small key sizes.
 * Uses the same functional interfaces as Hasher.
 */
public final class HasherA5 {
    /**
     * The seed used by all non-static hash() and hash64() methods in this class (the methods that don't take a seed).
     * You can create many different HasherA5 objects, all with different seeds, and get very different hashes as a result
     * of any calls on them. Because making this field hidden in some way doesn't meaningfully contribute to security,
     * and only makes it harder to use this class, {@code seed} is public (and final, so it can't be accidentally
     * modified, but still can if needed via reflection).
     */
    public final long seed;

    /**
     * Creates a new HasherA5 seeded, arbitrarily, with the constant 0xC4CEB9FE1A85EC53L, or -4265267296055464877L .
     */
    public HasherA5() {
        this(0xC4CEB9FE1A85EC53L);
    }

    /**
     * Initializes this HasherA5 with the given seed, verbatim; it is recommended to use {@link Hasher#randomize3(long)}
     * on the seed if you don't know if it is adequately-random. If the seed is the same for two different HasherA5
     * instances, and they are given the same inputs, they will produce the same results. If the seed is even slightly
     * different, the results of the two HasherA5s given the same input should be significantly different.
     *
     * @param seed a long that will be used to change the output of hash() and hash64() methods on the new HasherA5
     */
    public HasherA5(long seed) {
        this.seed = seed;
    }

    /**
     * Alternating 0, 1 bits.
     */
    public static final int VAL01 = 0x55555555;
    /**
     * Alternating 1, 0 bits.
     */
    public static final int VAL10 = 0xAAAAAAAA;

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hash(final int[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hash(final int[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hash(final long[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hash(final long[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        long a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        switch (len) {
            case 0:
                a = 0L;
                b = 0L;
                break;
            case 2:
                p = data[i + 1];
                c = p & 0xFFFFFFFFL;
                d = (p >>> 32);

                p = (seed3 + c) * (seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
                // intentional fallthrough
            case 1:
                p = data[i];
                a = (p & 0xFFFFFFFFL);
                b = (p >>> 32);
                break;
            default:
                val01 ^= seed1;
                val10 ^= seed2;

                do {
                    final int s1 = seed1;
                    final int s4 = seed4;

                    p = data[i];
                    p = (seed1 + (p & 0xFFFFFFFFL)) * (seed2 + (p >>> 32));
                    seed1 = (int) p;
                    seed2 = (int) (p >>> 32);
                    p = data[i+1];
                    p = (seed3 + (p & 0xFFFFFFFFL)) * (seed4 + (p >>> 32));
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);

                    len -= 2;
                    i += 2;

                    seed1 += val01;
                    seed2 += s4;
                    seed3 += s1;
                    seed4 += val10;
                } while (len > 2);

                p = data[i + len - 1];
                a = (p & 0xFFFFFFFFL);
                b = (p >>> 32);

                if (len > 1) {
                    p = data[i + len - 2];
                    c = (p & 0xFFFFFFFFL);
                    d = (p >>> 32);

                    p = (seed3 + c) * (seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
                break;
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = (seed1 + a) * (seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (p & 0xFFFFFFFFL);
        b = (p >>> 32);

        return (int) (a ^ b);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hash(final byte[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hash(final byte[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hash(final short[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hash(final short[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data input array
     * @return the 32-bit hash of data
     */
    public int hash(final char[] data) {
        if (data == null) return 0;
        return hash(data, 0, data.length);
    }

    /**
     * A hashing function that is meant for smaller input arrays.
     *
     * @param data   input array
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hash(final char[] data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length)
            return 0;
        int len = Math.min(length, data.length - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data[i];
                b = data[last];
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data[i + mo];
                    d = data[last - mo];
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data[i]) * ((long) seed2 + data[i + 1]);
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data[i + 2]) * ((long) seed4 + data[i + 3]);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data[i + len - 2];
            b = data[i + len - 1];

            if (len > 2) {
                c = data[i + len - 4];
                d = data[i + len - 3];

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input CharSequences.
     *
     * @param data input CharSequence
     * @return the 32-bit hash of data
     */
    public int hash(final CharSequence data) {
        if (data == null) return 0;
        return hash(data, 0, data.length());
    }

    /**
     * A hashing function that is meant for smaller input CharSequences.
     *
     * @param data   input CharSequence
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hash(final CharSequence data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length())
            return 0;
        int len = Math.min(length, data.length() - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data.charAt(i);
                b = data.charAt(last);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data.charAt(i + mo);
                    d = data.charAt(last - mo);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data.charAt(i)) * ((long) seed2 + data.charAt(i + 1));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data.charAt(i + 2)) * ((long) seed4 + data.charAt(i + 3));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data.charAt(i + len - 2);
            b = data.charAt(i + len - 1);

            if (len > 2) {
                c = data.charAt(i + len - 4);
                d = data.charAt(i + len - 3);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }

    /**
     * A hashing function that is meant for smaller input Strings.
     *
     * @param data input String
     * @return the 32-bit hash of data
     */
    public int hash(final String data) {
        if (data == null) return 0;
        return hash(data, 0, data.length());
    }

    /**
     * A hashing function that is meant for smaller input Strings.
     *
     * @param data   input String
     * @param start  starting index in data
     * @param length how many items to use from data
     * @return the 32-bit hash of data
     */
    public int hash(final String data, int start, int length) {
        if (data == null || start < 0 || length < 0 || start >= data.length())
            return 0;
        int len = Math.min(length, data.length() - start);
        int val01 = VAL01;
        int val10 = VAL10;

        int seed1 = 0x243F6A88 ^ len;
        int seed2 = 0x85A308D3 ^ len;
        int seed3 = 0xFB0BD3EA;
        int seed4 = 0x0F58FD47;
        int a, b, c, d;
        long p;

        p = (seed2 ^ (seed & 0xFFFFFFFFL)) * (seed1 ^ (seed >>> 32));
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);
        p = (seed3 ^ (seed & 0xFFFFFFFFL)) * (seed4 ^ (seed >>> 32));
        seed3 = (int) p;
        seed4 = (int) (p >>> 32);

        int i = start;
        if (len <= 4) {
            if (len == 0) {
                a = 0;
                b = 0;
            } else {
                int last = i + len - 1;
                a = data.charAt(i);
                b = data.charAt(last);
                if (len > 2) {
                    int mo = len >>> 1;
                    c = data.charAt(i + mo);
                    d = data.charAt(last - mo);
                    p = ((long) seed3 + c) * ((long) seed4 + d);
                    seed3 = (int) p;
                    seed4 = (int) (p >>> 32);
                }
            }
        } else {
            val01 ^= seed1;
            val10 ^= seed2;

            do {
                final int s1 = seed1;
                final int s4 = seed4;

                p = ((long) seed1 + data.charAt(i)) * ((long) seed2 + data.charAt(i + 1));
                seed1 = (int) p;
                seed2 = (int) (p >>> 32);
                p = ((long) seed3 + data.charAt(i + 2)) * ((long) seed4 + data.charAt(i + 3));
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);

                len -= 4;
                i += 4;

                seed1 += val01;
                seed2 += s4;
                seed3 += s1;
                seed4 += val10;
            } while (len > 4);

            a = data.charAt(i + len - 2);
            b = data.charAt(i + len - 1);

            if (len > 2) {
                c = data.charAt(i + len - 4);
                d = data.charAt(i + len - 3);

                p = ((long) seed3 + c) * ((long) seed4 + d);
                seed3 = (int) p;
                seed4 = (int) (p >>> 32);
            }
        }
        seed1 ^= seed3;
        seed2 ^= seed4;

        p = ((long) seed1 + a) * ((long) seed2 + b);
        seed1 = (int) p;
        seed2 = (int) (p >>> 32);

        p = ((long) seed1 ^ val01) * ((long) seed2);
        a = (int) p;
        b = (int) (p >>> 32);

        return a ^ b;
    }
}
