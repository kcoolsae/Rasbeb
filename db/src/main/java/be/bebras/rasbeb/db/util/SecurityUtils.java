/* SecurityUtils.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright (C) 2015 Universiteit Gent
 * 
 * This file is part of the Rasbeb project, an interactive web
 * application for Bebras competitions.
 * 
 * Corresponding author:
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Rasbeb Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Rasbeb Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE in the
 * distribution).  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package be.bebras.rasbeb.db.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Some utility functions related to security.
 */
public final class SecurityUtils {

    // see https://www.cigital.com/justice-league-blog/2009/08/14/proper-use-of-javas-securerandom/
    // see https://www.owasp.org/index.php/Hashing_Java

    private static SecureRandom secureRandom;
    private static MessageDigest digest;

    private static int useCount = 0;

    static {
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            digest = MessageDigest.getInstance("SHA-1");

        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Could not obtain a secure random generator or hash", ex);
        }
    }

    private static byte[] parseHexBinary(String str) {
        // replaces call to javax.xml.bin.DataConverter.parseHexBinary (since Java 9)
        byte[] result = new byte[str.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int total = 0;
            for (int j = 0; j < 2; j++) {
                char ch = str.charAt(2 * i + j);
                if (ch >= '0' && ch <= '9') {
                    total = 16 * total + ch - '0';
                } else if (ch >= 'A' && ch <= 'F') {
                    total = 16 * total + ch - 'A' + 10;
                } else if (ch >= 'a' && ch <= 'f') {
                    total = 16 * total + ch - 'a' + 10;
                }
            }
            result[i] = (byte)total;
        }
        return result;
    }

    /**
     * Convert byte array to hex.
     */
    private static String printHexBinary(byte[] bytes) {
        // replaces call to javax.xml.bin.DataConverter.printHexBinary (since Java 9)
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            builder.append(String.format("%02x", b));
        return builder.toString();
    }


    private static void nextBytes(byte[] bytes) {
        secureRandom.nextBytes(bytes);
        useCount++;
        if (useCount > 1000) {
            useCount = 0;
            secureRandom.setSeed(secureRandom.generateSeed(8));
        }
    }

    /**
     * Return a random magic string of the given (even) size, which includes the
     * given identifier.
     */
    public static String getMagicNumber(int size, int id) {
        byte[] result = new byte[size / 2];
        nextBytes(result);
        result[0] = (byte) (id >> 24);
        result[1] = (byte) (id >> 16);
        result[2] = (byte) (id >> 8);
        result[3] = (byte) (id);

        return printHexBinary(result);
    }

    /**
     * Return a random token of the given (even) size.
     */
    public static String getToken (int size) {
        byte[] result = new byte[size / 2];
        nextBytes(result);
        return printHexBinary(result);
    }

    /**
     * Create a random salt and hash for the given password
     */
    public static String[] saltAndHash(String password) {
        byte[] result = new byte[8];
        nextBytes(result);
        String salt = printHexBinary(result);
        String hash = printHexBinary(getHash (password, result));
        return new String[] { salt, hash };

    }

    /**
     * Check whether the given password correctly corresponds to salt and hash.
     */
    public static boolean isCorrectPassword (String salt, String hash, String password) {
        byte[] binSalt = parseHexBinary(salt);
        byte[] binHash = parseHexBinary(hash);
        byte[] computedHash = getHash(password, binSalt);
        return Arrays.equals(binHash, computedHash);
    }

    // borrowed from  https://www.owasp.org/index.php/Hashing_Java

    public static final int NR_ITERATIONS = 500;

    private static byte[] getHash(String password, byte[] salt)  {
        digest.reset();
        digest.update(salt);
        byte[] input = digest.digest(password.getBytes());
        for (int i = 0; i < NR_ITERATIONS; i++) {
            digest.reset();
            input = digest.digest(input);
        }
        return input;
    }
}
