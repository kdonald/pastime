package com.pastime.util;

public interface ReferralCodeGenerator {

    String generateKey();

    boolean meetsSyntax(String string);

}