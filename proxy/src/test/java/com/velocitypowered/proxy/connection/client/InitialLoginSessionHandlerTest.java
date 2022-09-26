package com.velocitypowered.proxy.connection.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InitialLoginSessionHandlerTest {

    @Test
    void GetRealMinecraftProfile() {
        assertTrue(InitialLoginSessionHandler.isValidAccount("Notch", null));
    }

    @Test
    void GetUnRealMinecraftProfile() {
        assertFalse(InitialLoginSessionHandler.isValidAccount("PlEaSEED0NT_CRE_18sdad9", null));
    }
}