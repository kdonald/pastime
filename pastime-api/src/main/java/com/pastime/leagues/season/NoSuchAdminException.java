package com.pastime.leagues.season;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="player not a team admin")
public class NoSuchAdminException extends RuntimeException {

}
