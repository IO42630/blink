package com.olexyn.blink;


import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLinkType;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Tools {



    List<Issue> parseSelectedIssues(Map<String, String[]> parameterMap,
                                    List<Issue> foundIssues) {
        List<Issue> selectedIssues = new ArrayList<Issue>();
        String checkboxesState = parameterMap.get("checkboxes_state")[0];
        String[] cbState = checkboxesState.split("\n");
        for (Issue issue : foundIssues) {
            for (String str : cbState) {
                String nameFromStr = str.split("=is=")[0];
                String key = issue.getKey();
                if (str.split("=is=")[1].startsWith("true") ) {
                    if(key.equals(nameFromStr)){
                        selectedIssues.add(issue);
                    }

                    // skip, do not jql again, instead  filter the issues you jqld before
                }
            }
        }


        return selectedIssues;
    }



    void updateMemoryForm(Map<String,String> map , String key , HttpServletRequest req){
        String value = Optional.ofNullable(req.getParameter(key)).orElse(null);

        if (value != null){
            if (key.equals("recursion") && Integer.parseInt(value)> Parameters.MAX_RECURSION){
                value = ""+Parameters.MAX_RECURSION;
            }
            map.put(key,value);
        }
    }





}


