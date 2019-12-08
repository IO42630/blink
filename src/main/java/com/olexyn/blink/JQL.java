package com.olexyn.blink;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

import java.util.List;


public class JQL {

    private Core core;

    public JQL(Core core) {
        this.core = core;
    }


    Issue getIssue(String issueName) {
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();

        ApplicationUser user = core.authenticationContext.getLoggedInUser();
        Query query = jqlClauseBuilder.issue(issueName).buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();


        SearchResults searchResults = null;
        Issue issue = null;
        try {
            searchResults = core.searchService.search(user, query, pagerFilter);
            if (searchResults.getTotal() != 0) {
                issue = searchResults.getIssues().get(0);
            }
        } catch (SearchException e) {
            e.printStackTrace();
        }
        return issue;
    }


    List<Issue> getIssues(String projectName) {
        ApplicationUser user = core.authenticationContext.getLoggedInUser();
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        Query query = jqlClauseBuilder.project(projectName).buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();

        SearchResults searchResults = null;
        List<Issue> issues = null;
        try {
            searchResults = core.searchService.search(user, query, pagerFilter);
            issues = searchResults.getIssues();
        } catch (SearchException e) {
            e.printStackTrace();
        }
        return issues != null ? issues : null;
    }


}