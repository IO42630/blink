package com.olexyn.blink;


import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;


import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.jira.security.JiraAuthenticationContext;


import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;


@Scanned
public class Core extends HttpServlet {

    @JiraImport
    SearchService searchService;
    @JiraImport
    TemplateRenderer templateRenderer;
    @JiraImport
    JiraAuthenticationContext authenticationContext;
    @JiraImport
    IssueLinkManager issueLinkManager;
    @JiraImport
    IssueLinkTypeManager issueLinkTypeManager;


    private Tools tools;
    private JQL jql;
    private Routines routines;

    private List<IssueLinkType> issueLinkTypes;
    // operational fields

    private IssueLinkType linkType;
    private List<Issue> foundIssues = new ArrayList<>();
    private List<Issue> selectedIssues = new ArrayList<>();
    private final String[] formKeys = {"root-issue",
                                       "find-link-type",
                                       "recursion",
                                       "target-issue",
                                       "connect-link-type"};

    // memorize the form contents, so the user doesn't have to retype everything.
    private Map<String, String> memoryForm = new HashMap<>();
    // e.g.
    // K: "blocks" V: ["outward", "Blocks"]
    // K: "is blocked by" V: ["inward", "Blocks"]
    Map<String, String[]> readableLinkMap = new HashMap<>();


    public Core(SearchService searchService,
                TemplateRenderer templateRenderer,
                JiraAuthenticationContext authenticationContext,
                IssueLinkManager issueLinkManager,
                IssueLinkTypeManager issueLinkTypeManager) {


        this.searchService = searchService;
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.issueLinkManager = issueLinkManager;
        this.issueLinkTypeManager = issueLinkTypeManager;

        this.tools = new Tools();

        this.jql = new JQL(this);
        this.routines = new Routines(this, this.jql);

        this.issueLinkTypes = new ArrayList<>(issueLinkTypeManager.getIssueLinkTypes());

        for (IssueLinkType iLT : this.issueLinkTypes) {
            this.readableLinkMap.put(iLT.getInward(), new String[]{"inward",
                                                                   iLT.getName()});
            this.readableLinkMap.put(iLT.getOutward(), new String[]{"outward",
                                                                    iLT.getName()});

        }


    }


    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws IOException {

        String action = Optional.ofNullable(req.getParameter("action")).orElse("");


        for (String key : formKeys) {
            tools.updateMemoryForm(memoryForm, key, req);
        }


        List<String> availableLinks = new ArrayList<>();
        for (IssueLinkType issueLinkType : issueLinkTypes) {
            availableLinks.add(issueLinkType.getInward());
            availableLinks.add(issueLinkType.getOutward());
        }


        Map<String, Object> context = new HashMap<>();


        switch (action) {
            case "search":
                foundIssues = routines.findIssues(this, memoryForm.get("root-issue"), memoryForm.get("find-link-type"), memoryForm.get("recursion"));
                if (foundIssues != null) {
                    context.put("issues", foundIssues);
                }

                break;

            case "link":
                String linkDirection = null;
                String targetIssueStr = memoryForm.get("target-issue");
                String connectLinkTypeStr = memoryForm.get("connect-link-type");

                if (targetIssueStr != null) {
                    for (IssueLinkType issueLinkType : issueLinkTypes) {
                        if (issueLinkType.getOutward().equals(connectLinkTypeStr)) {
                            linkType = issueLinkType;
                            linkDirection = "outward";
                        } else if (issueLinkType.getInward().equals(connectLinkTypeStr)) {
                            linkType = issueLinkType;
                            linkDirection = "inward";
                        }
                    }

                    Map<String, String[]> parameterMap = req.getParameterMap();
                    selectedIssues = tools.parseSelectedIssues(parameterMap, foundIssues);
                    for (Issue selectedIssue : selectedIssues) {
                        Issue targetIssue = jql.getIssue(targetIssueStr);
                        routines.linkTwoIssues(targetIssue, selectedIssue, linkType, linkDirection);
                    }
                }
                break;

            default:
                ;


        }
        context.put("links", availableLinks);
        context.put("title", Parameters.TITLE);

        for (String key : formKeys) {
            context.put(key, memoryForm.get(key));
        }

        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render(Parameters.LIST_ISSUES_TEMPLATE, context, resp.getWriter());

    }


    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);

    }


    @Override
    protected void doDelete(HttpServletRequest req,
                            HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }


}