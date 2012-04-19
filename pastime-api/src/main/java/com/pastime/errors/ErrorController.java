package com.pastime.errors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pastime.util.ErrorBody;

@Controller
public class ErrorController {

    @RequestMapping(value = "/error",
            method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE },
            produces = "application/json")
    public @ResponseBody ErrorBody error(HttpServletRequest request) {
        String message = (String) request.getAttribute("javax.servlet.error.message");
        if (message != null && message.length() > 0) {
            return new ErrorBody(message);
        } else {
            return null;
        }
    }

}
