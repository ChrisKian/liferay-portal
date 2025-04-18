package com.liferay.oauth2.provider.sample;

import com.liferay.oauth2.provider.scope.RequiresScope;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Application;
import java.util.Collections;
import java.util.Set;
import org.osgi.service.component.annotations.Component;

@Component(
    property = {"oauth2.scopechecker.type=annotations", "osgi.jaxrs.application.base=/sample-app", "osgi.jaxrs.name=sample-app"},
    service = {Application.class}
)
public class SampleApp extends Application {
    public Set<Class<?>> getClasses() {
        return Collections.singleton(SampleApp.class);
    }

    @RequiresScope({"everything.read"})
    @GET
    public String getTheThing() {
        return "The Thing";
    }
}