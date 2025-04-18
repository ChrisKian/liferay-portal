package com.liferay.oauth2.provider.scope.sample.qa;

import com.liferay.oauth2.provider.scope.RequiresNoScope;
import com.liferay.oauth2.provider.scope.RequiresScope;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

import java.util.Collections;
import java.util.Set;

import org.osgi.service.component.annotations.Component;

@Component(
	property = {
		"oauth2.scopechecker.type=annotations",
		"osgi.jaxrs.application.base=/sample-qa-app",
		"osgi.jaxrs.extension.select=(liferay.extension=OAuth2)",
		"osgi.jaxrs.name=liferay-oauth2-scope-sample-qa-app"
	},
	service = {Application.class}
)
public class SampleQAApp extends Application {

	@GET
	@Path("/allScopesNeeded")
	@RequiresScope({"example-scope-1", "example-scope-2"})
	public String allScopesNeeded() {
		return "allScopesNeeded";
	}

	@GET
	@Path("/anyScopesNeeded")
	@RequiresScope(
		allNeeded = false, value = {"example-scope-1", "example-scope-2"}
	)
	public String anyScopesNeeded() {
		return "anyScopesNeeded";
	}

	public Set<Object> getSingletons() {
		return Collections.singleton(this);
	}

	@GET
	@Path("/noScopesNeeded")
	@RequiresNoScope
	public String noScopesNeeded() {
		return "noScopesNeeded";
	}

}