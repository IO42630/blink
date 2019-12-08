package com.olexyn.blink;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.LinkCollection;


import java.util.*;

public class Routines {


    private Core core;
    private JQL jql;


    public Routines(Core core,
                    JQL jql) {
        this.core = core;
        this.jql = jql;
    }


    List<Issue> findIssues(Core core,
                           String issueNameStr,
                           String linkTypeStr,
                           String recursionStr) {

        int recursion = Integer.parseInt(recursionStr);
        if (issueNameStr == null) {
            return null;
        }


        Issue root_issue = jql.getIssue(issueNameStr);
        if (root_issue == null) {
            return null;
        }

        LinkCollection linkCollection = core.issueLinkManager.getLinkCollectionOverrideSecurity(root_issue);


        List<Issue> output = new ArrayList<>();

        if (linkTypeStr.equalsIgnoreCase("Alle")) {
            Collection<Issue> collection = linkCollection.getAllIssues();
            if (collection instanceof List) {
                output = (List) collection;
            } else {
                output = new ArrayList<>(collection);
            }

        } else if (linkTypeStr != null) {
            String[] boiler = core.readableLinkMap.get(linkTypeStr);

            if (boiler[0].equals("inward")) {
                List<Issue> inwardIssues = linkCollection.getInwardIssues(boiler[1]);
                if (inwardIssues != null) {
                    output.addAll(inwardIssues);
                }
            }
            if (boiler[0].equals("outward")) {
                List<Issue> outwardIssues = linkCollection.getOutwardIssues(boiler[1]);
                if (outwardIssues != null) {
                    output.addAll(outwardIssues);
                }
            }
        }


        List<Issue> snapshot = new ArrayList<>(output);
        if (recursion > 0) {
            for (Issue issue : snapshot) {
                List<Issue> newLinkedIssues = findIssues(core, issue.getKey(), linkTypeStr, "" + (recursion - 1));
                output.addAll(newLinkedIssues);

            }
        }

        // prune duplicates
        Set<Issue> set = new HashSet<>(output);
        output = new ArrayList<>(set);

        return output != null ? output : null;
    }


    void linkTwoIssues(Issue startIssue,
                       Issue endIssue,
                       IssueLinkType issueLinkType,
                       String direction) {
        //IssueLinkType issueLinkType = issueLinkTypeManager.getIssueLinkType(100000L);

        Long sequence = (long) 1;

        // if direction is inward instead of outward, then swap the end and start issues.
        if (direction.equals("inward")) {
            Issue swap = startIssue;
            startIssue = endIssue;
            endIssue = swap;
        }

        try {

            core.issueLinkManager.createIssueLink(startIssue.getId(), endIssue.getId(), issueLinkType.getId(), sequence, core.authenticationContext.getLoggedInUser());

        } catch (CreateException e) {
            e.printStackTrace();
        }
    }
}
