package com.siva.shopsphere.security;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Iterator;

import org.springframework.stereotype.Service;

@Service
public class OAuth2CodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    // Maps code -> CodeEntry
    private final Map<String, CodeEntry> codeMap = new ConcurrentHashMap<>();

    private static class CodeEntry {
        final UUID userId;
        final Instant expiry;

        CodeEntry(UUID userId, Instant expiry) {
            this.userId = userId;
            this.expiry = expiry;
        }
    }

    public String generateCode(UUID userId) {
        cleanUpExpiredCodes();
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        
        // Code expires in 1 minute
        codeMap.put(code, new CodeEntry(userId, Instant.now().plus(1, ChronoUnit.MINUTES)));
        return code;
    }

    public UUID exchangeCode(String code) {
        cleanUpExpiredCodes();
        CodeEntry entry = codeMap.remove(code);
        if (entry != null && Instant.now().isBefore(entry.expiry)) {
            return entry.userId;
        }
        return null;
    }

    private void cleanUpExpiredCodes() {
        Instant now = Instant.now();
        Iterator<Map.Entry<String, CodeEntry>> iterator = codeMap.entrySet().iterator();
        while (iterator.hasNext()) {
            if (now.isAfter(iterator.next().getValue().expiry)) {
                iterator.remove();
            }
        }
    }
}
