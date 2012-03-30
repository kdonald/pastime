package org.springframework.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiDispatcherServlet extends DispatcherServlet {

	@Override
	protected void noHandlerFound(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.sendError(HttpServletResponse.SC_NOT_FOUND, "resource not found");
	}

}
