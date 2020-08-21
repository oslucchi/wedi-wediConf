package it.l_soft.wediConf.rest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import java.io.IOException;

public class CORSFilter implements Filter {
	private final Logger log = (Logger) Logger.getLogger(getClass());
	private final ApplicationProperties prop = ApplicationProperties.getInstance();
	@Override
	public void init(FilterConfig filterConfig) throws ServletException 
	{
		log.trace("init called");
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) 
			throws IOException, ServletException 
	{
		if (!prop.isUseCoars())
			return;
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		log.debug("Request: " + request.getMethod() + " to " + request.getRequestURI());

		HttpServletResponse resp = (HttpServletResponse) servletResponse;
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
		resp.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, Language, Edit-Mode");

		// Just ACCEPT and REPLY OK if OPTIONS
		if ( request.getMethod().equals("OPTIONS") ) {
			log.debug("Option request, returning ok: " +
					  "Allow-Origin" + resp.getHeader("Access-Control-Allow-Origin") + 
					  "Allow-Methods" + resp.getHeader("Access-Control-Allow-Methods"));
			resp.setStatus(HttpServletResponse.SC_OK);
			return;
		}
		chain.doFilter(request, servletResponse);
	}

	@Override
	public void destroy() 
	{
		;
	}
}