package com.pastime.leagues.season;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Roster is frozen")
public class RosterFrozenException extends RuntimeException {

}
