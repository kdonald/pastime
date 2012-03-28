package com.pastime.prelaunch;

public interface ReferralCodeGenerator {

    String generateKey();

    boolean meetsSyntax(String string);

}