# Category Classification
  
Contains the implementation of a category-based classification framework developed for the KRISTINA project ( http://kristina-project.eu/en/ ). The framework relies on the Random Forests (RF) machine learning method and a late fusion strategy that is based on the operational capabilities of RF. The classification process is applied on Twitter posts stored in a MongoDB database. This project includes the code for both the experimental setup and the service itself which updates the category field of the recently crawled Twitter posts. The code has been developed and tested in Python, version 3.6.1, 64-bit.

The most important libraries used by this code are:
- numpy (1.13.0)
- scikit-learn (0.18.1)
- gensim (3.0.0)
- nltk (3.2.4)
- pymongo (3.5.1)


# Version
1.0.0