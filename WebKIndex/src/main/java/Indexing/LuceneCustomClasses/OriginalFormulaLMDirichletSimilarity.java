package Indexing.LuceneCustomClasses;


import java.util.List;
import java.util.Locale;

import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

/**
 * Copied and lightly modified score function<br>
 *     by Thodoris Tsompanidis
 *
 * Bayesian smoothing using Dirichlet priors. From Chengxiang Zhai and John
 * Lafferty. 2001. A study of smoothing methods for language models applied to
 * Ad Hoc information retrieval. In Proceedings of the 24th annual international
 * ACM SIGIR conference on Research and development in information retrieval
 * (SIGIR '01). ACM, New York, NY, USA, 334-342.
 * <p>
 * The formula as defined the paper assigns a negative score to documents that
 * contain the term, but with fewer occurrences than predicted by the collection
 * language model. The Lucene implementation returns {@code 0} for such
 * documents.
 * </p>
 *
 * @lucene.experimental
 */
public class OriginalFormulaLMDirichletSimilarity extends LMSimilarity {
  /** The &mu; parameter. */
  private final float mu;

  /** Instantiates the similarity with the provided &mu; parameter. */
  public OriginalFormulaLMDirichletSimilarity(CollectionModel collectionModel, float mu) {
    super(collectionModel);
    this.mu = mu;
  }

  /** Instantiates the similarity with the provided &mu; parameter. */
  public OriginalFormulaLMDirichletSimilarity(float mu) {
    this.mu = mu;
  }

  /** Instantiates the similarity with the default &mu; value of 2000. */
  public OriginalFormulaLMDirichletSimilarity(CollectionModel collectionModel) {
    this(collectionModel, 2000);
  }

  /** Instantiates the similarity with the default &mu; value of 2000. */
  public OriginalFormulaLMDirichletSimilarity() {
    this(2000);
  }

  @Override
  protected float score(BasicStats stats, float freq, float docLen) {
	  float score = 0.0f;
	  float collectionProbability = (float)((LMStats)stats).getCollectionProbability();
	  if (collectionProbability > 0 ) {
		  score = stats.getTotalBoost() * (float)Math.log( (freq + mu * collectionProbability) / (docLen + mu) );
	  }
	  return Math.abs(score);


	  // /float score = stats.getTotalBoost() * (float)(Math.log(1 + freq /
    //    (mu * ((LMStats)stats).getCollectionProbability())) +
    //    Math.log(mu / (docLen + mu)));
    //return score > 0.0f ? score : 0.0f;
  }

  @Override
  protected void explain(List<Explanation> subs, BasicStats stats, int doc,
      float freq, float docLen) {
    if (stats.getTotalBoost() != 1.0f) {
      subs.add(Explanation.match(stats.getTotalBoost(), "boost"));
    }

    subs.add(Explanation.match(mu, "mu"));
    Explanation weightExpl = Explanation.match(
        (float)Math.log(1 + freq /
        (mu * ((LMStats)stats).getCollectionProbability())),
        "term weight");
    subs.add(weightExpl);
    subs.add(Explanation.match(
        (float)Math.log(mu / (docLen + mu)), "document norm"));
    super.explain(subs, stats, doc, freq, docLen);
  }

  /** Returns the &mu; parameter. */
  public float getMu() {
    return mu;
  }

  @Override
  public String getName() {
    return String.format(Locale.ROOT, "Dirichlet(%f)", getMu());
  }
}


