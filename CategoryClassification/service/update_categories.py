import warnings
warnings.filterwarnings(action='ignore', category=UserWarning, module='gensim')
import time, sys
sys.path.append('../')
from mongo.parse_mongo_data import MongoDataHandler
from service.doc_categorizer import DocumentCategorization

def main(args):
    program_start = time.time()

    if len(args) == 2:
        language = args[1]
    else:
        print("You must give exactly one argument, the language.")
        print(sys.argv[1])
        print("Given args:" + str(len(sys.argv) - 1))
        print("Exiting...")
        exit()

    # CONSTANTS
    USERNAME = 'user'
    PASSWORD = 'pass'
    AUTH_DB = 'db'
    if language == 'german':
        DB_NAME = 'dbgerman'
    elif language == 'turkish':
        DB_NAME = 'dbturkish'
    DATA_COLLECTION = 'data_coll'
    CONTENT_FIELD = "field" # field in collection where tweet content is stored
    ANN_COLLECTION = 'ann_coll' # we keep the category annotation in a separate collection
    IP = "X.Y.Z.W"
    PORT = "27017"
    DATASET_VERSION = "v1"

    mongo = MongoDataHandler(IP, PORT, USERNAME, PASSWORD, AUTH_DB, DB_NAME)
    mongo.open_connection()
    annotator = DocumentCategorization()
    category_dict = {"h": "Health", "ebf": "Economy", "ll": "Lifestyle", "p": "Politics", "ne": "Nature",
                     "st": "Science"}
    category_dict_inv = {v: k for k, v in category_dict.items()}
    data = mongo.get_unannotated_data(DATA_COLLECTION, ANN_COLLECTION)
    for item in data:
        item_id = item['_id']
        creationDate = item['creationDate']
        content = item[CONTENT_FIELD]
        category = annotator.categorize(content, language, DATASET_VERSION)
        category_code = category_dict_inv[category]
        print(item_id)
        print(category_code)
        print()
        mongo.annotate(ANN_COLLECTION, item_id, creationDate, category_code)

    # print elapsed time
    program_elapsed = time.time() - program_start
    print()
    print("Python script execution time (seconds):", program_elapsed)


if __name__ == '__main__':
    main(sys.argv)