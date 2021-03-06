// Copyright 2018 Yahoo Holdings. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.security.tls;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author bjorncs
 */
public class TransportSecurityOptionsTest {

    private static final Path TEST_CONFIG_FILE = Paths.get("src/test/resources/transport-security-options.json");
    private static final TransportSecurityOptions OPTIONS = new TransportSecurityOptions.Builder()
            .withCertificates(Paths.get("certs.pem"), Paths.get("myhost.key"))
            .withCaCertificates(Paths.get("my_cas.pem"))
            .build();

    @Test
    public void can_read_options_from_json_file() {
        TransportSecurityOptions actualOptions =  TransportSecurityOptions.fromJsonFile(TEST_CONFIG_FILE);
        assertEquals(OPTIONS, actualOptions);
    }

    @Test
    public void can_read_options_from_json() throws IOException {
        String tlsJson = new String(Files.readAllBytes(TEST_CONFIG_FILE), StandardCharsets.UTF_8);
        TransportSecurityOptions actualOptions = TransportSecurityOptions.fromJson(tlsJson);
        assertEquals(OPTIONS, actualOptions);
    }

}
