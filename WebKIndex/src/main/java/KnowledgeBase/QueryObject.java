package KnowledgeBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by spyridons on 10/12/2016.
 */
public class QueryObject {

    private String queryLine;
    private List<String> terms;
    public enum Operator {AND, OR, NONE} ;
    private Operator operator;
    private int maxResults;

    public String getQueryLine() {
        return queryLine;
    }

    public void setQueryLine(String queryLine) {
        this.queryLine = queryLine;
    }
    public QueryObject(){
        terms = new ArrayList<>();
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public void addTerm(String term) {
        this.terms.add(term);
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

}
