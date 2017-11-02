'''
Created on Feb 26, 2016

@author: spyridons
'''
import warnings
warnings.filterwarnings(action='ignore', category=UserWarning, module='gensim')
import nltk, re, pickle, time, sys
import numpy as np
from nltk.corpus import stopwords
from gensim.models import Word2Vec
from nltk import PorterStemmer
from sklearn.feature_extraction.text import TfidfTransformer
from scipy.sparse import hstack


class DocumentCategorization:

    def categorize(self, text, language, dataset_version="v1"):

        # CONSTANTS
        w2v_vector_size = 400

        # already saved files
        w2v_weights = pickle.load(open("files/" + dataset_version + "/w2v_weights_" + language + ".p", "rb"))
        w2v_model = Word2Vec.load("files/" + dataset_version + "/word2vecModel_" + language +".out")
        w2v_forest = pickle.load(open("files/" + dataset_version + "/forest_w2v_" + language + ".p", "rb"))
        ngrams_forest = pickle.load(open("files/" + dataset_version + "/forest_ngrams_" + language + ".p", "rb"))
        ngram_vectorizers = pickle.load(open("files/" + dataset_version + "/vectorizers_" + language + ".p", "rb"))

        tokenizer = nltk.data.load('tokenizers/punkt/' + language + '.pickle')

        # w2v format
        w2v_input = list()
        sentences = self.review_to_sentences(text, tokenizer, True)
        # convert list of sentences (where each sentence is a list of words) to an unique list of document words
        for sentence in sentences:
            w2v_input.extend(sentence)

        # n-gram format
        text = re.sub(r"http\S+", '', text, flags=re.MULTILINE)
        text = re.sub("[\W_\d]", " ", text)
        text = re.sub(' +', ' ', text).strip()
        ngrams_input = list()
        ngrams_input.append(text)

        ngram_weights = [1 - w for w in w2v_weights]

        # make w2v_vector and predict class
        w2v_vector = self.makeFeatureVec(w2v_input, w2v_model, w2v_vector_size)
        del w2v_model
        w2v_probas = w2v_forest.predict_proba(w2v_vector.reshape(1, w2v_vector_size))
        del w2v_forest

        # make ngram vector and predict class
        unigrams = ngram_vectorizers[0].transform(ngrams_input)
        tf_unigrams = TfidfTransformer(norm='l1', use_idf=False, smooth_idf=False).fit_transform(unigrams)
        bigrams = ngram_vectorizers[1].transform(ngrams_input)
        tf_bigrams = TfidfTransformer(norm='l1', use_idf=False, smooth_idf=False).fit_transform(bigrams)
        tf_total = hstack([tf_unigrams, tf_bigrams]).toarray()
        del ngram_vectorizers
        ngrams_probas = ngrams_forest.predict_proba(tf_total)
        del ngrams_forest

        # fuse probabilities and predict class

        # get weighted average probabilities (FOR NEW DOCUMENT)
        final_probas = list()
        for idx2, w2v_val in enumerate(w2v_probas[0]):
            ngram_val = ngrams_probas[0][idx2]
            final_val = w2v_val * w2v_weights[idx2] + ngram_val * ngram_weights[idx2]
            #         print(final_val)
            final_probas.append(final_val)

        final_probas = np.array(final_probas)
        print("\nFused probabilities:")
        print("Economy\tHealth\tLifestyle\tNature\tPolitics\tScience")
        print(final_probas)

        # get final fused prediction
        classes = ["Economy", "Health", "Lifestyle", "Nature", "Politics",
                   "Science"]
        # get index of max probability
        max_value = max(final_probas)
        index = np.where(final_probas == max_value)[0][0]
        # get predicted label
        predicted_class = classes[index]
        print("Predicted class:")
        print(predicted_class)

        return predicted_class

    def review_to_wordlist(self, review, remove_stopwords=False):
        # Function to convert a document to a sequence of words,
        # optionally removing stop words.  Returns a list of words.
        #
        # 1. Remove a) urls b) non-letters c) unnecessary blank characters
        review_text = re.sub(r"http\S+", '', review, flags=re.MULTILINE)
        review_text = re.sub("[\W_\d]", " ", review_text)
        review_text = re.sub(' +', ' ', review_text).strip()
        #
        # 2. Convert words to lower case and split them
        words = review_text.lower().split()
        #
        # 3. Optionally remove stop words (false by default)
        if remove_stopwords:
            stops = set(stopwords.words("english"))
            words = [w for w in words if not w in stops]
        #
        # 4. Return a list of words
        return (words)

    def review_to_sentences(self, review, tokenizer, remove_stopwords=False):
        # Function to split a review into parsed sentences. Returns a
        # list of sentences, where each sentence is a list of words
        #
        # 1. Use the NLTK tokenizer to split the paragraph into sentences
        raw_sentences = tokenizer.tokenize(review.strip())
        #
        # 2. Loop over each sentence
        sentences = []
        for raw_sentence in raw_sentences:
            # If a sentence is empty, skip it
            if len(raw_sentence) > 0:
                # Otherwise, call review_to_wordlist to get a list of words
                sentences.append(self.review_to_wordlist(raw_sentence, \
                                                    remove_stopwords))
        #
        # Return the list of sentences (each sentence is a list of words,
        # so this returns a list of lists
        return sentences

    def makeFeatureVec(self, words, model, num_features):
        # Function to average all of the word vectors in a given
        # paragraph
        #
        # Pre-initialize an empty numpy array (for speed)
        featureVec = np.zeros((num_features,), dtype="float32")
        #
        nwords = 0.
        #
        # Index2word is a list that contains the names of the words in
        # the model's vocabulary. Convert it to a set, for speed
        index2word_set = set(model.wv.index2word)
        #
        # Loop over each word in the review and, if it is in the model's
        # vocaublary, add its feature vector to the total
        for word in words:
            if word in index2word_set:
                nwords = nwords + 1.
                featureVec = np.add(featureVec, model[word])
        #
        # Divide the result by the number of words to get the average
        if nwords == 0:
            return featureVec
        featureVec = np.divide(featureVec, nwords)
        return featureVec

