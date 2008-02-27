package com.danga.jmemcached.util;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

/**
 * @author <A href="mailto:abashev at gmail dot com">Alexey Abashev</A>
 * @version $Id$
 */
public class StopTokenChecker {
    public static final List<String> STOP_TOKENS = Arrays.asList(
            "END\r\n",
            "NOT_FOUND\r\n",
            "DELETED\r\n",
            "STORED\r\n",
            "NOT_STORED\r\n"
    );

    public static final List<String> START_TOKENS = Arrays.asList(
            "ERROR\r\n",
            "CLIENT_ERROR ",
            "SERVER_ERROR "
    );

    private static final byte FILL_CHAR = (byte) 0;
    
    private static final byte[][] stopTokens;
    private static final int maxStopLength;

    private static final int maxStartLength;

    static {
        int maxLen = 0;
        
        for (String s : STOP_TOKENS) {
            final int len = s.getBytes().length;
            
            if (maxLen < len) {
                maxLen = len;
            }
        }
        
        maxStopLength = maxLen;
        stopTokens = new byte[STOP_TOKENS.size()][maxStopLength];
        
        for (int i = 0; i < STOP_TOKENS.size(); i++) {
            byte[] dest = stopTokens[i];
            
            Arrays.fill(dest, FILL_CHAR);
            
            byte[] bytes = STOP_TOKENS.get(i).getBytes();
            
            System.arraycopy(bytes, 0, dest, maxLen - bytes.length, bytes.length);
        }
        
        maxLen = 0;
        
        for (String s : START_TOKENS) {
            final int len = s.getBytes().length;
            
            if (maxLen < len) {
                maxLen = len;
            }
        }
        
        maxStartLength = maxLen;
    }
    
    /**
     * Check is buffer finish with stop token.
     *  
     * @param buffer
     * @return
     */
    public static boolean finishWithToken(ByteBuffer buffer) {
        int position = buffer.position() - 1;
        byte[] lastChars = new byte[maxStopLength];

        for (int i = position, j = maxStopLength - 1; (i >= 0) && (j >= 0); i--, j--) {
            lastChars[j] = buffer.get(i);
        }
        
        for (byte[] token : stopTokens) {
            boolean equals = true;
            
            for (int i = maxStopLength - 1; i >= 0; i--) {
                if ((token[i] != 0) && (token[i] != lastChars[i])) {
                    equals = false;
                }
            }
            
            if (equals) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean startWithToken(ByteBuffer buffer) {
        int position = buffer.position() - 1;
        byte[] startChars = new byte[maxStartLength];

        for (int i = 0; (i < maxStartLength) && (i < position); i++) {
            startChars[i] = buffer.get(i);
        }
        
        String s = new String(startChars);
        
        for (String token : START_TOKENS) {
            if (s.startsWith(token)) {
                return true;
            }
        }
        
        return false;
    }
}
