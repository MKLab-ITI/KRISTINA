# This Python file uses the following encoding: utf-8
from pymongo import MongoClient
import urllib


class MongoDataHandler:
    def __init__(self, ip, port, username, password, auth_db, db_name):
        self.ip = ip
        self.port = port
        self.username = username
        self.password = password
        self.auth_db = auth_db
        self.db_name = db_name
        self.client = None

    def open_connection(self):
        self.client = MongoClient('mongodb://' +
                                  self.username + ':' + urllib.parse.quote_plus(self.password) + '@' +
                                  self.ip + ':' + self.port + "/" + self.auth_db)
        print("Opened connection at " + self.ip + ":" + self.port)

    def get_content_by_id(self, collection_name, id, data_field):
        if self.client is None:
            print("A connection is not open!!!")
            return
        tweets = []
        db_conn = self.client[self.db_name]
        collection_conn = db_conn[collection_name]
        tweet = collection_conn.find_one({"_id": id})
        content = tweet[data_field]
        return content

    def retrieve_data(self, collection_name, data_field):
        if self.client is None:
            print("A connection is not open!!!")
            return
        tweets = []
        db_conn = self.client[self.db_name]
        collection_conn = db_conn[collection_name]
        for result in collection_conn.find():
            tweet = result[data_field]
            tweets.append(tweet)
        return tweets

    def save_dataset(self, data_collection, data_field, annotation_collection, annotation_field):
        folder = "dataset/v1/german/"
        category_dict = {"h":"Health", "ebf":"Economy", "ll":"Lifestyle", "p":"Politics", "ne":"Nature", "st": "Science"}
        if self.client is None:
            print("A connection is not open!!!")
            return
        db_conn = self.client[self.db_name]
        data_collection_conn = db_conn[data_collection]
        annotation_collection_conn = db_conn[annotation_collection]
        for annotation_doc in annotation_collection_conn.find(): # annotation collection must not have predicted instances, only the training set
            tweet_id = annotation_doc["_id"]
            annotation = annotation_doc[annotation_field]
            tweet_data = data_collection_conn.find_one({"_id":tweet_id})
            tweet = tweet_data[data_field]
            with open(folder + category_dict[annotation] + "/" + tweet_id + ".txt", "w+", encoding="utf-8") as f:
                f.write(tweet)

    def get_unannotated_data(self, data_collection_name, ann_collection_name):
        if self.client is None:
            print("A connection is not open!!!")
            return
        data = []
        db_conn = self.client[self.db_name]
        data_collection_conn = db_conn[data_collection_name]
        ann_collection_conn = db_conn[ann_collection_name]
        for result in data_collection_conn.find():
            result_id = result['_id']
            annotation = ann_collection_conn.find_one({'_id':result_id})
            if annotation is None or "predicted_category" not in annotation.keys():
                data.append(result)
        return data

    def annotate(self, collection_name, id, creationDate, category):
        if self.client is None:
            print("A connection is not open!!!")
            return
        db_conn = self.client[self.db_name]
        collection_conn = db_conn[collection_name]
        if collection_conn.find_one({"_id": id}) is None:
            collection_conn.insert_one({"_id": id, "creationDate": creationDate, "predicted_category": category})
        else:
            collection_conn.update_one({"_id": id}, {"$set": {"creationDate": creationDate, "predicted_category": category}})
