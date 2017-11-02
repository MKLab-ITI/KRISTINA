# -*- coding: utf-8 -*-
import pickle
import re

from nltk.corpus import stopwords

from mongo.parse_mongo_data import MongoDataHandler

# CONSTANTS
USERNAME = 'user'
PASSWORD = 'pass'
AUTH_DB = 'db'
DB_NAME = 'db'
COLLECTION = 'coll'
DATA_FIELD = "field" # field in collection where tweet content is stored
IP = "X.Y.Z.W"
PORT = "27017"
CORPUS_FILE = "corpus/corpus_german.p"
LANGUAGE = "german"


def review_to_wordlist(review, language, remove_stopwords=False):
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
        stops = set(stopwords.words(language))
        words = [w for w in words if not w in stops]
    #
    # 4. Return a list of words
    return words


def save_corpus():
    data_retriever = MongoDataHandler(IP, PORT, USERNAME, PASSWORD, AUTH_DB, DB_NAME)
    data_retriever.open_connection()
    twitter_data = data_retriever.retrieve_data(COLLECTION, DATA_FIELD)
    tweet_corpus = []
    for tweet in twitter_data:
        # If a sentence is empty, skip it
        if len(tweet) > 0:
            # Otherwise, call review_to_wordlist to get a list of words
            tweet_corpus.append(review_to_wordlist(tweet, LANGUAGE, True))
    pickle.dump(tweet_corpus, open(CORPUS_FILE, "wb"))



def save_dataset():
    data_retriever = MongoDataHandler(IP, PORT, USERNAME, PASSWORD, AUTH_DB, DB_NAME)
    data_retriever.open_connection()
    data_retriever.save_dataset(COLLECTION, DATA_FIELD, "Annotation", "category")


save_corpus()
save_dataset()
