package com.example;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.security.Policy;

@WebServlet("/trigger")
public class EstimatorServlet extends HttpServlet {

    @Override
    public void init() {
        setupSecurityPolicies();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        String drl = request.getParameter("drl");
        Resource drlResource = ResourceFactory.newClassPathResource(drl + ".drl", this.getClass()).setResourceType(ResourceType.DRL);
        kieFileSystem.write(drlResource);


        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        try {
            kieBuilder.buildAll();
        } catch (Exception e) {
            throw new ServletException(e);
        }

        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new ServletException("Build Errors:\n" + kieBuilder.getResults().toString());
        }

        response.getWriter().println(drl + ".drl Compilation Success");
        response.getWriter().flush();
        response.flushBuffer();
    }

    private void setupSecurityPolicies() {
        System.setProperty("java.security.policy", getAbsolutePathOfClasspathFile("global.policy"));
        System.setProperty("kie.security.policy", getAbsolutePathOfClasspathFile("rules.policy"));
        Policy.getPolicy().refresh();
        System.setSecurityManager(new SecurityManager());
    }

    private String getAbsolutePathOfClasspathFile(String path) {
        return new File(this.getClass().getResource(path).getPath()).getAbsolutePath();
    }
}
